package uk.gov.digital.ho.hocs.audit.core.exception;

public class StreamBrokenException extends RuntimeException {
    private final Throwable throwable;

    public StreamBrokenException(Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
