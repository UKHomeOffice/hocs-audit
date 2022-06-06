package uk.gov.digital.ho.hocs.audit.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.client.info.InfoClient;
import uk.gov.digital.ho.hocs.audit.core.exception.AuditExportException;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.HeaderConverter;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_EXPORT_COMPLETE;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_EXPORT_START;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.TOPIC_TEAM_ROW_EXPORT_FAILURE;


@Slf4j
@Service
public class StaticTopicAndTeamService {

    private final HeaderConverter headerConverter;
    private final InfoClient infoClient;

    public StaticTopicAndTeamService(HeaderConverter headerConverter, InfoClient infoClient) {
        this.headerConverter = headerConverter;
        this.infoClient = infoClient;
    }

    public void export(OutputStream outputStream, String caseType, boolean convertHeader) throws IOException {
        log.info("Exporting topics and teams to CSV", value(EVENT, CSV_EXPORT_START));
        try (var buffer = new BufferedOutputStream(outputStream);
             var outputWriter = new OutputStreamWriter(buffer, StandardCharsets.UTF_8);
             var printer = new CSVPrinter(outputWriter, CSVFormat.Builder.create()
                     .setHeader(getHeaders(convertHeader))
                     .setAutoFlush(true)
                     .build())) {
            var topicTeams = infoClient.getTopicsWithTeams(caseType);

            topicTeams.forEach(topic -> {
                topic.getTeams().forEach(team -> {
                    try {
                        printer.printRecord(caseType, topic.getUuid(), topic.getDisplayName(), team.getUuid(), team.getDisplayName());
                    } catch (IOException e) {
                        throw new AuditExportException(e, TOPIC_TEAM_ROW_EXPORT_FAILURE, "Unable to export topic team row for topic %s team %s", topic.getUuid(), team.getUuid() );
                    }
                });
            });
        }
        log.info("Completed export of topics and teams to CSV", value(EVENT, CSV_EXPORT_COMPLETE));
    }

    private String[] getHeaders(boolean convertHeaders) {
        String[] headers = new String[] {
                "caseType", "topicUUID", "topicName", "teamUUID", "teamName"
        };

        if (convertHeaders) {
            return headerConverter.substitute(headers);
        }

        return headers;
    }

}
