package uk.gov.digital.ho.hocs.audit.service.domain.converter;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

@Slf4j
@Service
public class HeaderConverter {

    private static final String HEADERS_PROPERTIES_FILE = "headers.config";
    private final Properties headerProperties = new Properties();

    public HeaderConverter() {
        try (InputStream headersInputStream = new ClassPathResource(HEADERS_PROPERTIES_FILE).getInputStream()) {
            headerProperties.load(headersInputStream);
        } catch (IOException ex) {
            log.error("Cannot open header substitutions file for reading {}", HEADERS_PROPERTIES_FILE);
        }
    }

    public String[] substitute(@NonNull String[] headers) {
        return Arrays.stream(headers)
                .map(header -> headerProperties.getProperty(header, header))
                .toArray(String[]::new);
    }

}
