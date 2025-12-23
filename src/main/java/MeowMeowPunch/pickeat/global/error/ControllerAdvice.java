package MeowMeowPunch.pickeat.global.error;

import MeowMeowPunch.pickeat.global.Logging.LogType;
import MeowMeowPunch.pickeat.global.Logging.MdcKeys;
import MeowMeowPunch.pickeat.global.common.template.ResTemplate;
import MeowMeowPunch.pickeat.global.error.exception.*;
import MeowMeowPunch.pickeat.domain.auth.exception.NeedRegistrationException;
import MeowMeowPunch.pickeat.domain.auth.dto.response.NeedRegistrationResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@RestControllerAdvice
public class ControllerAdvice {

    // 400, InvalidGroupException
    @ExceptionHandler({ InvalidGroupException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResTemplate<?> handleInvalidData(RuntimeException e, HttpServletRequest request) {
        logError("VALIDATION", HttpStatus.BAD_REQUEST, e, request);
        return createErrorResponse(e, HttpStatus.BAD_REQUEST);
    }

    // 401, AuthGroupException
    @ExceptionHandler({ AuthGroupException.class })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResTemplate<?> handleAuthDate(RuntimeException e, HttpServletRequest request) {
        logError("AUTH", HttpStatus.UNAUTHORIZED, e, request);
        return createErrorResponse(e, HttpStatus.UNAUTHORIZED);
    }

    // 403, AccessDeniedGroupException
    @ExceptionHandler({ AccessDeniedGroupException.class })
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResTemplate<?> handleAccessDeniedDate(RuntimeException e, HttpServletRequest request) {
        logError("ACCESS_DENIED", HttpStatus.FORBIDDEN, e, request);
        return createErrorResponse(e, HttpStatus.FORBIDDEN);
    }

    // 404, NotFoundGroupException
    @ExceptionHandler({ NotFoundGroupException.class })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResTemplate<?> handleNotFoundDate(RuntimeException e, HttpServletRequest request) {
        if (e instanceof NeedRegistrationException ne) {
            log.info("Need Registration: tokenIssued");
            NeedRegistrationResponse response = NeedRegistrationResponse.of(ne.getRegisterToken(), ne.getSocialUserInfo());
            return new ResTemplate<>(HttpStatus.NOT_FOUND, "회원가입이 필요합니다.", response);
        }
        logError("NOT_FOUND", HttpStatus.NOT_FOUND, e, request);
        return createErrorResponse(e, HttpStatus.NOT_FOUND);
    }

    // 408, REQUEST_TIMEOUT
    @ExceptionHandler({ TimeoutGroupException.class })
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    public ResTemplate<?> requestTimeout(RuntimeException e, HttpServletRequest request) {
        logError("TIMEOUT", HttpStatus.REQUEST_TIMEOUT, e, request);
        return createErrorResponse(e, HttpStatus.REQUEST_TIMEOUT);
    }

    // 409, ConflictGroupException
    @ExceptionHandler({ ConflictGroupException.class })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResTemplate<?> handleConflict(RuntimeException e, HttpServletRequest request) {
        logError("CONFLICT", HttpStatus.CONFLICT, e, request);
        return createErrorResponse(e, HttpStatus.CONFLICT);
    }

    // 418, TeapotGroupException
    @ExceptionHandler({ TeapotGroupException.class })
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    public ResTemplate<?> handleTeapotGroupException(RuntimeException e, HttpServletRequest request) {
        logError("TEAPOT", HttpStatus.I_AM_A_TEAPOT, e, request); // 또는 "SYSTEM"
        return createErrorResponse(e, HttpStatus.I_AM_A_TEAPOT);
    }

    // 422, UnprocessableGroupException
    @ExceptionHandler({ UnprocessableGroupException.class })
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResTemplate<?> handleUnprocessable(RuntimeException e, HttpServletRequest request) {
        logError("UNPROCESSABLE", HttpStatus.UNPROCESSABLE_ENTITY, e, request);
        return createErrorResponse(e, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // 429, ManyRequestsGroupException
    @ExceptionHandler({ ManyRequestsGroupException.class })
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ResTemplate<?> handleManyRequest(RuntimeException e, HttpServletRequest request) {
        logError("RATE_LIMIT", HttpStatus.TOO_MANY_REQUESTS, e, request);
        return createErrorResponse(e, HttpStatus.TOO_MANY_REQUESTS);
    }

    // 500, InternalServerError (이메일 전송 과정에서 발생하는 오류를 위해 추가)
    // 500, InternalServerError (이메일 전송 과정에서 발생하는 오류를 위해 추가)
    @ExceptionHandler({ InternalServerErrorGroupException.class })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResTemplate<?> handleInternalServerError(RuntimeException e, HttpServletRequest request) {
        logError("SYSTEM", HttpStatus.INTERNAL_SERVER_ERROR, e, request);
        return createErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // JSON 파싱 에러 (Enum 값 불일치 등)
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResTemplate<?> handleHttpMessageNotReadableException(
            org.springframework.http.converter.HttpMessageNotReadableException e,
            HttpServletRequest request) {
        String message = "잘못된 요청 형식입니다. JSON 데이터의 구문이나 값을 확인해주세요.";

        // Enum 파싱 오류인 경우 더 구체적인 메시지 제공 시도
        if (e.getCause() instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException) {
            com.fasterxml.jackson.databind.exc.InvalidFormatException cause = (com.fasterxml.jackson.databind.exc.InvalidFormatException) e
                    .getCause();
            if (cause.getTargetType() != null && cause.getTargetType().isEnum()) {
                message = String.format("유효하지 않은 Enum 값입니다: '%s'. 허용된 값: %s",
                        cause.getValue(), java.util.Arrays.toString(cause.getTargetType().getEnumConstants()));
            }
        }

        log.warn("jsonParseFailed",
                kv("logType", LogType.ERROR.name()),
                kv("errorCategory", "VALIDATION"),
                kv("errorCode", "HttpMessageNotReadableException"),
                kv("errorMessage", message),
                kv("rawMessage", e.getMessage()),
                kv("httpStatus", HttpStatus.BAD_REQUEST.value()),
                kv("path", request.getRequestURI()),
                kv("method", request.getMethod()));

        return ResTemplate.error(HttpStatus.BAD_REQUEST, message);
    }

    // 메서드 인자 문제 생겼을 때
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResTemplate<?> handleMethodArgumentNotValidException(
            final MethodArgumentNotValidException e,
            HttpServletRequest request) {
        FieldError fieldError = Objects.requireNonNull(e.getFieldError());
        String message = String.format("%s. (%s)",
                fieldError.getDefaultMessage(), fieldError.getField());

        ResTemplate<?> errorResponse = ResTemplate.error(HttpStatus.BAD_REQUEST, message);
        // WARN 레벨로 구조화 로그
        log.warn("validationFailed",
                kv("logType", LogType.ERROR.name()),
                kv("errorCategory", "VALIDATION"),
                kv("errorCode", e.getClass().getSimpleName()),
                kv("errorMessage", message),
                kv("httpStatus", HttpStatus.BAD_REQUEST.value()),
                kv("path", request.getRequestURI()),
                kv("method", request.getMethod()),
                kv("clientIp", MDC.get(MdcKeys.CLIENT_IP)),
                kv("traceId", MDC.get(MdcKeys.TRACE_ID)));
        return errorResponse;
    }

    // 공통 에러 응답 생성
    private ResTemplate<?> createErrorResponse(RuntimeException e, HttpStatus status) {
        return ResTemplate.error(status, e.getMessage());
    }

    // 공통 에러 로그 생성
    private void logError(String errorCategory,
            HttpStatus status,
            RuntimeException e,
            HttpServletRequest request) {

        log.error("errorLog",
                kv("logType", LogType.ERROR.name()),
                kv("errorCategory", errorCategory), // VALIDATION / AUTH / ACCESS_DENIED / ...
                kv("errorCode", e.getClass().getSimpleName()), // 예외 클래스명 사용
                kv("errorMessage", e.getMessage()),
                kv("httpStatus", status.value()),
                kv("path", request.getRequestURI()),
                kv("method", request.getMethod()),
                kv("clientIp", MDC.get(MdcKeys.CLIENT_IP)),
                kv("traceId", MDC.get(MdcKeys.TRACE_ID)),
                e // stacktrace
        );
    }
}