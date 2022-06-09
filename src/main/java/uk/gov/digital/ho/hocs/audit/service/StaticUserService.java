package uk.gov.digital.ho.hocs.audit.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.client.info.InfoClient;
import uk.gov.digital.ho.hocs.audit.core.exception.ApplicationExceptions;
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
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EXPORT_FAILURE_USER_ROW;

@Slf4j
@Service
public class StaticUserService {

    private final HeaderConverter headerConverter;
    private final InfoClient infoClient;

    public StaticUserService(HeaderConverter headerConverter, InfoClient infoClient) {
        this.headerConverter = headerConverter;
        this.infoClient = infoClient;
    }

    public void export(OutputStream outputStream, boolean convertHeader) throws IOException {
        log.info("Exporting users to CSV", value(EVENT, CSV_EXPORT_START));
        try (var buffer = new BufferedOutputStream(outputStream);
             var outputWriter = new OutputStreamWriter(buffer, StandardCharsets.UTF_8);
             var printer = new CSVPrinter(outputWriter, CSVFormat.Builder.create()
                             .setHeader(getHeaders(convertHeader))
                             .setAutoFlush(true)
                             .build())) {
            var users = infoClient.getUsers();

            users.forEach(user -> {
                try {
                    printer.printRecord(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getEmail());
                } catch (IOException e) {
                    throw new ApplicationExceptions.AuditExportException(e, EXPORT_FAILURE_USER_ROW, "Unable to export user row for %s", user.getId());
                }
            });
        }
        log.info("Completed export of users to CSV", value(EVENT, CSV_EXPORT_COMPLETE));
    }

    private String[] getHeaders(boolean convertHeaders) {
        String[] headers = new String[] {
                "userUUID", "username", "firstName", "lastName", "email"
        };

        if (convertHeaders) {
            return headerConverter.substitute(headers);
        }

        return headers;
    }

}
