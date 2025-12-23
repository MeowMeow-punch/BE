package MeowMeowPunch.pickeat.domain.auth.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import MeowMeowPunch.pickeat.domain.auth.dto.request.OAuthLoginRequest;
import MeowMeowPunch.pickeat.domain.auth.dto.request.SignUpRequest;
import MeowMeowPunch.pickeat.domain.auth.dto.response.AuthTokenResponse;
import MeowMeowPunch.pickeat.domain.auth.entity.User;
import MeowMeowPunch.pickeat.domain.auth.exception.AuthNotFoundException;
import MeowMeowPunch.pickeat.domain.auth.exception.DuplicateNicknameException;
import MeowMeowPunch.pickeat.domain.auth.exception.InvalidTokenException;
import MeowMeowPunch.pickeat.domain.auth.exception.TokenNotFoundException;
import MeowMeowPunch.pickeat.domain.auth.repository.RefreshTokenRepository;
import MeowMeowPunch.pickeat.domain.auth.repository.UserRepository;
import MeowMeowPunch.pickeat.global.common.entity.RefreshToken;
import MeowMeowPunch.pickeat.global.common.enums.UserStatus;
import MeowMeowPunch.pickeat.global.jwt.JwtProperties;
import MeowMeowPunch.pickeat.global.jwt.JwtTokenProvider;
import MeowMeowPunch.pickeat.welstory.entity.GroupMapping;
import MeowMeowPunch.pickeat.welstory.repository.GroupMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * [Auth][Service] AuthService
 *
 * OAuth 로그인, 회원가입, 로그아웃, 회원탈퇴 등 인증 흐름 담당.
 * <p>
 * [Auth Flow]
 * 
 * <pre>
 * 1. Login/SignUp
 *    [Request] -> [Validate] -> [User Lookup/Save] -> [Issue Tokens] -> [Response]
 *
 * 2. Token Strategy
 *    - Access Token: Short-lived (Header)
 *    - Refresh Token: Long-lived (HttpOnly Cookie, DB Saved)
 * </pre>
 * </p>
 * - 토큰 전략: Access + Refresh 발급 및 회수
 * - 검증 포인트: 닉네임 중복, 그룹 ID 유효성, 리프레시 토큰 존재 여부
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final JwtProperties jwtProperties;
	private final GroupMappingRepository groupMappingRepository;

	/**
	 * [Login] OAuth 로그인 성공 시 토큰 발급.
	 *
	 * @param request OAuth 로그인 요청 정보
	 * @return 액세스/리프레시 토큰 묶음
	 */
	public AuthTokenResponse login(OAuthLoginRequest request) {
		User user = userRepository.findByOauthProviderAndOauthId(request.oauthProvider(), request.oauthId())
				.orElseThrow(AuthNotFoundException::userNotFound);
		return issueTokens(user);
	}

	/**
	 * [Refresh] 리프레시 토큰을 이용한 토큰 재발급.
	 * 
	 * @param refreshToken 클라이언트 쿠키에서 추출한 리프레시 토큰
	 * @return 재발급된 Access/Refresh 토큰 묶음
	 */
	@Transactional
	public AuthTokenResponse refresh(String refreshToken) {
		// 1. 토큰 자체 유효성 검증 (만료, 서명 등)
		try {
			jwtTokenProvider.parseClaims(refreshToken);
		} catch (io.jsonwebtoken.ExpiredJwtException e) {
			throw InvalidTokenException.expired();
		}

		// 2. DB 저장된 토큰과 비교 (Rotation)
		User user = findUserByToken(refreshToken);

		// 3. 토큰 재발급 및 갱신
		return issueTokens(user);
	}

	private User findUserByToken(String refreshToken) {
		UUID userId;
		try {
			// UUID 형식 검증
			userId = UUID.fromString(jwtTokenProvider.parseClaims(refreshToken).getSubject());
		} catch (IllegalArgumentException e) {
			// 401 Invalid Token
			throw new InvalidTokenException("잘못된 토큰 형식입니다.");
		}

		// Redis 토큰 조회 (Rotation Check)
		RefreshToken savedToken = refreshTokenRepository.findById(Objects.requireNonNull(userId.toString()))
				.orElseThrow(() -> new InvalidTokenException("로그인이 만료되었습니다. 다시 로그인해주세요."));

		// 토큰 탈취 감지 (값 불일치)
		if (!savedToken.getTokenValue().equals(refreshToken)) {
			// 탈취된 토큰 삭제 및 재로그인 유도
			refreshTokenRepository.delete(savedToken);
			throw new InvalidTokenException("유효하지 않은 토큰입니다. 다시 로그인해주세요.");
		}

		return userRepository.findById(userId).orElseThrow(AuthNotFoundException::userNotFound);
	}

	/**
	 * [SignUp] 신규 회원 정보 저장 및 토큰 발급.
	 *
	 * @param request 회원가입 요청 정보
	 * @return 액세스/리프레시 토큰 묶음
	 */
	@Transactional
	public AuthTokenResponse signUp(SignUpRequest request) {
		validateNickname(request.nickname());
		String resolvedGroupId = resolveGroupId(request.groupId());
		validateGroup(request.status(), resolvedGroupId);

		User user = buildUser(request, resolvedGroupId);
		User savedUser = userRepository.save(user);
		return issueTokens(savedUser);
	}

	/**
	 * [Logout] 리프레시 토큰 폐기.
	 */
	@Transactional
	public void logout(UUID userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(AuthNotFoundException::userNotFound);

		refreshTokenRepository.findById(user.getId().toString())
				.ifPresentOrElse(
						refreshTokenRepository::delete,
						() -> {
							throw TokenNotFoundException.tokenNotFound();
						});
		log.info("[LOGOUT] userId={} refresh token deleted", userId);
	}

	/**
	 * [Withdraw] 회원 탈퇴 시 사용자 및 리프레시 토큰 제거.
	 */
	@Transactional
	public void deleteUser(UUID userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(AuthNotFoundException::userNotFound);

		refreshTokenRepository.deleteById(userId.toString());
		userRepository.delete(user);
		log.info("[WITHDRAW] userId={} removed", userId);
	}

	/**
	 * [Token Issuance] Access/Refresh 토큰 생성 및 리프레시 토큰 저장.
	 */
	@Transactional
	public AuthTokenResponse issueTokens(User user) {
		String accessToken = jwtTokenProvider.createAccessToken(user);
		String refreshToken = jwtTokenProvider.createRefreshToken(user);
		Instant refreshExpiry = Instant.now().plusSeconds(jwtProperties.getRefreshTokenValidityInSeconds());
		long refreshTtl = jwtProperties.getRefreshTokenValidityInSeconds();

		refreshTokenRepository.findById(user.getId().toString())
				.ifPresentOrElse(
						existing -> {
							existing.rotate(refreshToken, refreshExpiry, refreshTtl);
							refreshTokenRepository.save(existing);
						},
						() -> refreshTokenRepository.save(RefreshToken.builder()
								.id(user.getId().toString())
								.tokenValue(refreshToken)
								.expiryAt(refreshExpiry)
								.ttlSeconds(refreshTtl)
								.build()));

		return AuthTokenResponse.of(accessToken, refreshToken);
	}

	private void validateNickname(String nickname) {
		if (userRepository.existsByNickname(nickname)) {
			throw DuplicateNicknameException.duplicateNickname();
		}
	}

	private void validateGroup(UserStatus status, String groupId) {
		if (status == UserStatus.GROUP && groupId == null) {
			throw AuthNotFoundException.groupNotFound();
		}
	}

	private String resolveGroupId(String groupId) {
		if (groupId == null || groupId.trim().isEmpty()) {
			return null;
		}

		String trimmed = groupId.trim();
		if (trimmed.matches("\\d+")) {
			long mappingId = Long.parseLong(trimmed);
			return groupMappingRepository.findById(mappingId)
					.map(GroupMapping::getGroupId)
					.orElseThrow(AuthNotFoundException::groupNotFound);
		}

		return trimmed;
	}

	private User buildUser(SignUpRequest request, String resolvedGroupId) {
		List<String> allergies = request.allergies() == null ? new ArrayList<>() : request.allergies();
		List<String> diseases = request.diseases() == null ? new ArrayList<>() : request.diseases();

		return User.builder()
				.oauthProvider(request.oauthProvider())
				.oauthId(request.oauthId())
				.nickname(request.nickname())
				.isMarketing(request.isMarketing())
				.gender(request.gender())
				.height(request.height())
				.weight(request.weight())
				.age(request.age())
				.allergies(allergies)
				.diseases(diseases)
				.status(request.status())
				.groupId(resolvedGroupId)
				.focus(request.focus())
				.smokingStatus(request.smokingStatus())
				.drinkingStatus(request.drinkingStatus())
				.meals(request.meals())
				.activityLevel(request.activityLevel())
				.targetWeight(request.targetWeight())
				.build();
	}
}
