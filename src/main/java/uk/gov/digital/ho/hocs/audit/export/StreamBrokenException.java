package uk.gov.digital.ho.hocs.audit.export;

public class StreamBrokenException extends RuntimeException {
    Throwable throwable;
    StreamBrokenException(Throwable throwable){this.throwable = throwable;}
}
