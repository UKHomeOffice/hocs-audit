package uk.gov.digital.ho.hocs.audit.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EXCEPTION;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.INVALID_PARAMETER_SPECIFIED;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.MISSING_REQUEST_PARAMETER;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.REST_HELPER_GET_BAD_REQUEST;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.REST_HELPER_GET_FORBIDDEN;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.REST_HELPER_GET_NOT_FOUND;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.REST_HELPER_GET_UNAUTHORIZED;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.REST_HELPER_POST_FAILURE;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.UNCAUGHT_EXCEPTION;

@ControllerAdvice
@Slf4j
public class RestResponseEntityExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> handle(HttpClientErrorException e) {
        String message = "HttpClientErrorException: {}";
        switch (e.getStatusCode()) {
            case UNAUTHORIZED -> {
                log.error(message, e.getMessage(), value(EVENT, REST_HELPER_GET_UNAUTHORIZED), value(EXCEPTION, e));
                return new ResponseEntity<>(e.getMessage(), UNAUTHORIZED);
            }
            case FORBIDDEN -> {
                log.error(message, e.getMessage(), value(EVENT, REST_HELPER_GET_FORBIDDEN), value(EXCEPTION, e));
                return new ResponseEntity<>(e.getMessage(), FORBIDDEN);
            }
            case NOT_FOUND -> {
                log.error(message, e.getMessage(), value(EVENT, REST_HELPER_GET_NOT_FOUND), value(EXCEPTION, e));
                return new ResponseEntity<>(e.getMessage(), NOT_FOUND);
            }
            default -> {
                log.error(message, e.getMessage(), value(EVENT, REST_HELPER_GET_BAD_REQUEST), value(EXCEPTION, e));
                return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
            }
        }
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<String> handle(HttpServerErrorException e) {
        log.error("HttpServerErrorException: {}", e.getMessage(),value(EVENT, REST_HELPER_POST_FAILURE), value(EXCEPTION, e));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AuditExportException.class)
    public ResponseEntity<String> handle(AuditExportException e) {
        log.error("AuditExportException: {}", e.getMessage(), value(EVENT, e.getEvent()), value(EXCEPTION, e));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EntityPermissionException.class)
    public ResponseEntity<String> handle(EntityPermissionException e) {
        log.error("EntityPermissionException: {}", e.getMessage(), value(EVENT, e.getEvent()), value(EXCEPTION, e));
        return new ResponseEntity<>(e.getMessage(), UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidExportTypeException.class)
    public ResponseEntity<String> handle(InvalidExportTypeException e) {
        log.error("InvalidExportTypeException: {}", e.getMessage(), value(EVENT, e.getEvent()), value(EXCEPTION, e));
        return new ResponseEntity<>(e.getMessage(), NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handle(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException: {}", e.getMessage(), value(EVENT, BAD_REQUEST), value(EXCEPTION, e));
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<String> handle(HttpMessageConversionException e) {
        log.error("HttpMessageConversionException: {}", e.getMessage(), value(EVENT, BAD_REQUEST), value(EXCEPTION, e));
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handle(HttpMessageNotReadableException e) {
        log.error("HttpMessageNotReadableException: {}", e.getMessage(), value(EVENT, BAD_REQUEST), value(EXCEPTION, e));
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<String> handle(UnsupportedOperationException e) {
        log.error("UnsupportedOperationException: {}", e.getMessage(), value(EVENT, METHOD_NOT_ALLOWED), value(EXCEPTION, e));
        return new ResponseEntity<>(e.getMessage(), METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handle(MethodArgumentTypeMismatchException e) {
        log.error("MethodArgumentTypeMismatchException: {}", e.getMessage(), value(EVENT, INVALID_PARAMETER_SPECIFIED), value(EXCEPTION, e));
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(UnsatisfiedServletRequestParameterException.class)
    public ResponseEntity<String> handle(UnsatisfiedServletRequestParameterException e) {
        log.error("UnsatisfiedServletRequestParameterException: {}", e.getMessage(), value(EVENT, MISSING_REQUEST_PARAMETER), value(EXCEPTION, e));
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handle(Exception e) {
        log.error("Uncaught Exception: {}: {} ", e.getClass().getName(), e.getMessage(), value(EVENT, UNCAUGHT_EXCEPTION), value(EXCEPTION, e));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

}
