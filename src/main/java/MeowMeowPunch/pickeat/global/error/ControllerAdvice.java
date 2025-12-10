package MeowMeowPunch.pickeat.global.error;

import MeowMeowPunch.pickeat.global.Logging.LogType;
import MeowMeowPunch.pickeat.global.Logging.MdcKeys;
import MeowMeowPunch.pickeat.global.common.template.ResTemplate;
import MeowMeowPunch.pickeat.global.error.exception.*;
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

    // 204, NoContentGroupException
    @ExceptionHandler({ NoContentGroupException.class })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResTemplate<?> handleNoContent(RuntimeException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NO_CONTENT;
        logError("NO_CONTENT", status, e, request);
        return createErrorResponse(e, status);
    }

    // 400, InvalidGroupException
    @ExceptionHandler({ InvalidGroupException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResTemplate<?> handleInvalidData(RuntimeException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logError("VALIDATION", status, e, request);
        return createErrorResponse(e, status);
    }

    // 401, AuthGroupException
    @ExceptionHandler({ AuthGroupException.class })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResTemplate<?> handleAuthDate(RuntimeException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        logError("AUTH", status, e, request);
        return createErrorResponse(e, status);
    }

    // 403, AccessDeniedGroupException
    @ExceptionHandler({ AccessDeniedGroupException.class })
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResTemplate<?> handleAccessDeniedDate(RuntimeException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        logError("ACCESS_DENIED", status, e, request);
        return createErrorResponse(e, status);
    }

    // 404, NotFoundGroupException
    @ExceptionHandler({ NotFoundGroupException.class })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResTemplate<?> handleNotFoundDate(RuntimeException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        logError("NOT_FOUND", status, e, request);
        return createErrorResponse(e, status);
    }

    // 408, REQUEST_TIMEOUT
    @ExceptionHandler({ TimeoutGroupException.class })
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    public ResTemplate<?> requestTimeout(RuntimeException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.REQUEST_TIMEOUT;
        logError("TIMEOUT", status, e, request);
        return createErrorResponse(e, status);
    }

    // 409, ConflictGroupException
    @ExceptionHandler({ ConflictGroupException.class })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResTemplate<?> handleConflict(RuntimeException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        logError("CONFLICT", status, e, request);
        return createErrorResponse(e, status);
    }

    // 418, TeapotGroupException
    @ExceptionHandler({ TeapotGroupException.class })
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    public ResTemplate<?> handleTeapotGroupException(RuntimeException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.I_AM_A_TEAPOT;
        logError("TEAPOT", status, e, request); // 또는 "SYSTEM"
        return createErrorResponse(e, status);
    }

    // 422, UnprocessableGroupException
    @ExceptionHandler({ UnprocessableGroupException.class })
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResTemplate<?> handleUnprocessable(RuntimeException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        logError("UNPROCESSABLE", status, e, request);
        return createErrorResponse(e, status);
    }

    // 429, ManyRequestsGroupException
    @ExceptionHandler({ ManyRequestsGroupException.class })
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ResTemplate<?> handleManyRequest(RuntimeException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;
        logError("RATE_LIMIT", status, e, request);
        return createErrorResponse(e, status);
    }

    // 500, InternalServerError (이메일 전송 과정에서 발생하는 오류를 위해 추가)
    @ExceptionHandler({ InternalServerErrorGroupException.class })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResTemplate<?> handleInternalServerError(RuntimeException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        logError("SYSTEM", status, e, request);
        return createErrorResponse(e, status);
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