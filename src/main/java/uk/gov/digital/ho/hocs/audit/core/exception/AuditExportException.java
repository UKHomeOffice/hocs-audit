package uk.gov.digital.ho.hocs.audit.core.exception;

import uk.gov.digital.ho.hocs.audit.core.LogEvent;

public class AuditExportException extends RuntimeException {

    private final LogEvent event;

    private final LogEvent exception;

    public AuditExportException(String msg, LogEvent event, Object... args) {
        super(String.format(msg, args));
        this.event = event;
        this.exception = null;
    }

    public AuditExportException(String msg, LogEvent event, LogEvent exception, Object... args) {
        super(String.format(msg, args));
        this.event = event;
        this.exception = exception;
    }

    public LogEvent getEvent() {
        return event;
    }

    public LogEvent getException() {
        return exception;
    }

}
