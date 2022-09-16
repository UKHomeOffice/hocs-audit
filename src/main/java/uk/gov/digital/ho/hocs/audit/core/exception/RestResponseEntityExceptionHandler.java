package uk.gov.digital.ho.hocs.audit.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.AUDIT_EVENT_CREATION_FAILED;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.AUDIT_RECORD_NOT_FOUND;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_EXPORT_FAILURE;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EXCEPTION;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.INVALID_PARAMETER_SPECIFIED;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.MISSING_REQUEST_PARAMETER;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.REST_CLIENT_EXCEPTION;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.UNAUTHORISED_ACCESS;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.UNCAUGHT_EXCEPTION;

@ControllerAdvice
@Slf4j
public class RestResponseEntityExceptionHandler {

    @ExceptionHandler(EntityCreationException.class)
    public ResponseEntity<String> handle(EntityCreationException e) {
        log.error("EntityCreationException", value(EVENT, AUDIT_EVENT_CREATION_FAILED), value(EXCEPTION, e.toString()));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handle(EntityNotFoundException e) {
        log.error("EntityNotFoundException", value(EVENT, AUDIT_RECORD_NOT_FOUND), value(EXCEPTION, e.toString()));
        return new ResponseEntity<>(e.getMessage(), NOT_FOUND);
    }

    @ExceptionHandler(AuditExportException.class)
    public ResponseEntity<String> handle(AuditExportException e) {
        log.error("AuditExportException", value(EVENT, CSV_EXPORT_FAILURE), value(EXCEPTION, e.toString()));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EntityPermissionException.class)
    public ResponseEntity<String> handle(EntityPermissionException e) {
        log.error("EntityPermissionException", value(EVENT, UNAUTHORISED_ACCESS), value(EXCEPTION, e.toString()));
        return new ResponseEntity<>(e.getMessage(), UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidExportTypeException.class)
    public ResponseEntity<String> handle(InvalidExportTypeException e) {
        log.error("InvalidExportTypeException", value(EVENT, INVALID_PARAMETER_SPECIFIED),
            value(EXCEPTION, e.toString()));
        return new ResponseEntity<>(e.getMessage(), NOT_FOUND);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<String> handle(RestClientException e) {
        log.error("RestClientException", value(EVENT, REST_CLIENT_EXCEPTION), value(EXCEPTION, e.toString()));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handle(MethodArgumentTypeMismatchException e) {
        log.error("MethodArgumentTypeMismatchException", value(EVENT, INVALID_PARAMETER_SPECIFIED),
            value(EXCEPTION, e.toString()));
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(UnsatisfiedServletRequestParameterException.class)
    public ResponseEntity<String> handle(UnsatisfiedServletRequestParameterException e) {
        log.error("UnsatisfiedServletRequestParameterException", value(EVENT, MISSING_REQUEST_PARAMETER),
            value(EXCEPTION, e.toString()));
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handle(Exception e) {
        log.error("Uncaught Exception: " + e.getClass().getName(), value(EVENT, UNCAUGHT_EXCEPTION),
            value(EXCEPTION, e.toString()));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

}
