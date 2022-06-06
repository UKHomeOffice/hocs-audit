package uk.gov.digital.ho.hocs.audit.core.exception;

import uk.gov.digital.ho.hocs.audit.core.LogEvent;

public class AuditExportException extends RuntimeException {

    private final LogEvent event;

    public AuditExportException(Throwable throwable, LogEvent event, String msg, Object... args) {
        super(String.format(msg, args), throwable);
        this.event = event;
    }

    public AuditExportException(LogEvent event, String msg, Object... args) {
        super(String.format(msg, args));
        this.event = event;
    }

    public LogEvent getEvent() {
        return event;
    }

}
