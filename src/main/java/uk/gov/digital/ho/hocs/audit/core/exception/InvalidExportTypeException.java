package uk.gov.digital.ho.hocs.audit.core.exception;

import uk.gov.digital.ho.hocs.audit.core.LogEvent;

public class InvalidExportTypeException extends RuntimeException {

    private final LogEvent event;

    public InvalidExportTypeException(LogEvent event, String msg, Object... args) {
        super(String.format(msg, args));
        this.event = event;
    }

    public LogEvent getEvent() {
        return event;
    }


}
