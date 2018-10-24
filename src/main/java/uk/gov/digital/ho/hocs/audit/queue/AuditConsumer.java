package uk.gov.digital.ho.hocs.audit.queue;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws.sqs.SqsConstants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.audit.domain.HocsAuditDomain;
import uk.gov.digital.ho.hocs.audit.domain.HocsCommand;

import static uk.gov.digital.ho.hocs.audit.application.RequestData.transferHeadersToMDC;

@Component
public class AuditConsumer extends RouteBuilder {

    private final HocsAuditDomain hocsAuditDomain;
    private final String auditQueue;
    private final String dlq;
    private final int maximumRedeliveries;
    private final int redeliveryDelay;
    private final int backOffMultiplier;

    //
    @Autowired
    public AuditConsumer(HocsAuditDomain hocsAuditDomain,
                         @Value("${audit.queue}") String auditQueue,
                         @Value("${audit.queue.dlq}") String dlq,
                         @Value("${audit.queue.maximumRedeliveries}") int maximumRedeliveries,
                         @Value("${audit.queue.redeliveryDelay}") int redeliveryDelay,
                         @Value("${audit.queue.backOffMultiplier}") int backOffMultiplier) {
        this.hocsAuditDomain = hocsAuditDomain;
        this.auditQueue = auditQueue;
        this.dlq = dlq;
        this.maximumRedeliveries = maximumRedeliveries;
        this.redeliveryDelay = redeliveryDelay;
        this.backOffMultiplier = backOffMultiplier;
    }

    @Override
    public void configure() {

        errorHandler(deadLetterChannel(dlq)
                .loggingLevel(LoggingLevel.ERROR)
                .log("Failed to add document after configured back-off.")
                .useOriginalMessage()
                .retryAttemptedLogLevel(LoggingLevel.WARN)
                .maximumRedeliveries(maximumRedeliveries)
                .redeliveryDelay(redeliveryDelay)
                .backOffMultiplier(backOffMultiplier)
                .asyncDelayedRedelivery()
                .logRetryStackTrace(true));

        from(auditQueue)
                .setProperty(SqsConstants.RECEIPT_HANDLE, header(SqsConstants.RECEIPT_HANDLE))
                .process(transferHeadersToMDC())
                .log("Command received: ${body}")
                .unmarshal().json(JsonLibrary.Jackson, HocsCommand.class)
                .log("Command unmarshalled")
                .bean(hocsAuditDomain, "executeCommand")
                .log("Command processed")
                .setHeader(SqsConstants.RECEIPT_HANDLE, exchangeProperty(SqsConstants.RECEIPT_HANDLE));
    }

}