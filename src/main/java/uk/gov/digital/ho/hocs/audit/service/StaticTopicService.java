package uk.gov.digital.ho.hocs.audit.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.client.info.InfoClient;
import uk.gov.digital.ho.hocs.audit.client.info.dto.TopicDto;
import uk.gov.digital.ho.hocs.audit.core.exception.AuditExportException;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.HeaderConverter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_EXPORT_FAILURE;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_RECORD_EXPORT_FAILURE;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EVENT;

@Slf4j
@Service
public class StaticTopicService {

    private final HeaderConverter headerConverter;
    private final InfoClient infoClient;

    public StaticTopicService(HeaderConverter headerConverter, InfoClient infoClient) {
        this.headerConverter = headerConverter;
        this.infoClient = infoClient;
    }

    public void export(PrintWriter writer, boolean convertHeader) throws IOException {
        var headers = getHeaders(convertHeader);

        try (var printer =
                     new CSVPrinter(writer, CSVFormat.Builder.create()
                             .setHeader(headers)
                             .setAutoFlush(true)
                             .build())) {

            Set<TopicDto> topics = infoClient.getTopics();
            topics.forEach(topic -> {
                try {
                    printer.printRecord(topic.getValue(), topic.getLabel(), topic.isActive());
                } catch (IOException e) {
                    throw new AuditExportException("Unable to parse record for audit {} for reason {}", CSV_RECORD_EXPORT_FAILURE, audit.getUuid(), e.getMessage());
                }
            });
        } catch (IOException e) {
            log.error("Unable to export record for reason {}", e.getMessage(), value(EVENT, CSV_EXPORT_FAILURE));
        }
    }

    private String[] getHeaders(boolean convertHeaders) {
        String[] headers = new String[] {
                "topicUUID", "topicName", "active"
        };

        if (convertHeaders) {
            return headerConverter.substitute(headers);
        }

        return headers;
    }

}
