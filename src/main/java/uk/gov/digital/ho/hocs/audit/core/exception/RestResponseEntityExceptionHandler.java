package uk.gov.digital.ho.hocs.audit.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.AUDIT_EVENT_CREATION_FAILED;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.AUDIT_RECORD_NOT_FOUND;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_EXPORT_FAILURE;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EXCEPTION;

@ControllerAdvice
@Slf4j
public class RestResponseEntityExceptionHandler {

    @ExceptionHandler(EntityCreationException.class)
    public ResponseEntity handle(EntityCreationException e) {
        log.error("EntityCreationException", value(EVENT, AUDIT_EVENT_CREATION_FAILED), value(EXCEPTION, e));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity handle(EntityNotFoundException e) {
        log.error("EntityNotFoundException", value(EVENT, AUDIT_RECORD_NOT_FOUND), value(EXCEPTION, e));
        return new ResponseEntity<>(e.getMessage(), NOT_FOUND);
    }

    @ExceptionHandler(AuditExportException.class)
    public ResponseEntity handle(AuditExportException e) {
        log.error("AuditExportException", value(EVENT, CSV_EXPORT_FAILURE), value(EXCEPTION, e));
        return new ResponseEntity<>(e.getMessage(), NOT_FOUND);
    }

}
