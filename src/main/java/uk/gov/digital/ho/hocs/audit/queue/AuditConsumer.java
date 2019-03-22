package uk.gov.digital.ho.hocs.audit.queue;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws.sqs.SqsConstants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.audit.AuditDataService;
import uk.gov.digital.ho.hocs.audit.auditdetails.dto.CreateAuditDto;

import static uk.gov.digital.ho.hocs.audit.application.RequestData.transferHeadersToMDC;

@Component
public class AuditConsumer extends RouteBuilder {

    private final AuditDataService auditDataService;
    private final String auditQueue;
    private final String dlq;
    private final int maximumRedeliveries;
    private final int redeliveryDelay;
    private final int backOffMultiplier;

    @Autowired
    public AuditConsumer(AuditDataService auditDataService,
                         @Value("${audit.queue}") String auditQueue,
                         @Value("${audit.queue.dlq}") String dlq,
                         @Value("${audit.queue.maximumRedeliveries}") int maximumRedeliveries,
                         @Value("${audit.queue.redeliveryDelay}") int redeliveryDelay,
                         @Value("${audit.queue.backOffMultiplier}") int backOffMultiplier) {
        this.auditDataService = auditDataService;
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
                .retryAttemptedLogLevel(LoggingLevel.WARN)
                .useOriginalMessage()
                .maximumRedeliveries(maximumRedeliveries)
                .redeliveryDelay(redeliveryDelay)
                .backOffMultiplier(backOffMultiplier)
                .asyncDelayedRedelivery()
                .logRetryStackTrace(false)
                .onPrepareFailure(exchange -> {
                    exchange.getIn().setHeader("FailureMessage", exchange.getProperty(Exchange.EXCEPTION_CAUGHT,
                            Exception.class).getMessage());
                    exchange.getIn().setHeader(SqsConstants.RECEIPT_HANDLE, exchangeProperty(SqsConstants.RECEIPT_HANDLE));
                }));

        from(auditQueue)
                .setProperty(SqsConstants.RECEIPT_HANDLE, header(SqsConstants.RECEIPT_HANDLE))
                .process(transferHeadersToMDC())
                .log(LoggingLevel.INFO, "Audit request received")
                .unmarshal().json(JsonLibrary.Jackson, CreateAuditDto.class)
                .log(LoggingLevel.DEBUG,  "Audit unmarshalled")
                .bean(auditDataService, "createAudit(${body.caseUUID}, ${body.stageUUID}, ${body.correlationID}, ${body.raisingService}, ${body.auditPayload}, ${body.namespace}, ${body.auditTimestamp}, ${body.type}, ${body.userID})")
                .log(LoggingLevel.INFO, "Audit request processed")
                .setHeader(SqsConstants.RECEIPT_HANDLE, exchangeProperty(SqsConstants.RECEIPT_HANDLE));
    }
}