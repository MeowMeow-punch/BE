package MeowMeowPunch.pickeat.domain.auth.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import MeowMeowPunch.pickeat.domain.auth.dto.request.OAuthLoginRequest;
import MeowMeowPunch.pickeat.domain.auth.dto.request.SignUpRequest;
import MeowMeowPunch.pickeat.domain.auth.dto.response.AuthTokenResponse;
import MeowMeowPunch.pickeat.domain.auth.entity.User;
import MeowMeowPunch.pickeat.domain.auth.exception.AuthNotFoundException;
import MeowMeowPunch.pickeat.domain.auth.exception.DuplicateNicknameException;
import MeowMeowPunch.pickeat.domain.auth.exception.TokenNotFoundException;
import MeowMeowPunch.pickeat.domain.auth.repository.RefreshTokenRepository;
import MeowMeowPunch.pickeat.domain.auth.repository.UserRepository;
import MeowMeowPunch.pickeat.global.common.entity.RefreshToken;
import MeowMeowPunch.pickeat.global.common.enums.UserStatus;
import MeowMeowPunch.pickeat.global.jwt.JwtProperties;
import MeowMeowPunch.pickeat.global.jwt.JwtTokenProvider;
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

	/**
	 * [Login] OAuth 로그인 성공 시 토큰 발급.
	 *
	 * @param request OAuth 로그인 요청 정보
	 * @return 액세스/리프레시 토큰 묶음
	 */
	@Transactional(readOnly = true)
	public AuthTokenResponse login(OAuthLoginRequest request) {
		User user = userRepository.findByOauthProviderAndOauthId(request.oauthProvider(), request.oauthId())
				.orElseThrow(AuthNotFoundException::userNotFound);
		return issueTokens(user);
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
		validateGroup(request.status(), request.groupId());

		User user = buildUser(request);
		user.initializeTimestamp();
		User savedUser = userRepository.save(user);
		return issueTokens(savedUser);
	}

	/**
	 * [Logout] 리프레시 토큰 폐기.
	 */
	@Transactional
	public void logout(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(AuthNotFoundException::userNotFound);

		refreshTokenRepository.findById(user.getId())
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
	public void deleteUser(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(AuthNotFoundException::userNotFound);

		refreshTokenRepository.deleteById(userId);
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

		refreshTokenRepository.findById(user.getId())
				.ifPresentOrElse(
						existing -> {
							existing.rotate(refreshToken, refreshExpiry, refreshTtl);
							refreshTokenRepository.save(existing);
						},
						() -> refreshTokenRepository.save(RefreshToken.builder()
								.id(user.getId())
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

	private void validateGroup(UserStatus status, Long groupId) {
		if (status == UserStatus.GROUP && groupId == null) {
			throw AuthNotFoundException.groupNotFound();
		}
	}

	private User buildUser(SignUpRequest request) {
		List<String> allergies = request.allergies() == null ? new ArrayList<>() : request.allergies();
		List<String> diseases = request.diseases() == null ? new ArrayList<>() : request.diseases();

		return User.builder()
				.oauthProvider(request.oauthProvider())
				.oauthId(request.oauthId())
				.nickname(request.nickname())
				.marketingAgreed(request.isMarket())
				.gender(request.gender())
				.height(request.height())
				.weight(request.weight())
				.age(request.age())
				.allergies(allergies)
				.diseases(diseases)
				.status(request.status())
				.groupId(request.groupId())
				.focus(request.focus())
				.smokingStatus(request.isSmoking())
				.drinkingStatus(request.isDrinking())
				.meals(request.meals())
				.activityLevel(request.activityLevel())
				.targetWeight(request.targetWeight())
				.build();
	}
}