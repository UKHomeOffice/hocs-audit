package uk.gov.digital.ho.hocs.audit.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Base64;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.INVALID_AUDIT_PAYLOAD_STORED;

@Component
@Slf4j
public class JsonValidator {

    private final ObjectMapper objectMapper;

    public JsonValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String validateAuditPayload(String correlationID, String raisingService, String auditPayload, String namespace, LocalDateTime auditTimestamp, String type, String userID) {
        if (!StringUtils.hasText(auditPayload)) {
            return "{}";
        }

        try {
            objectMapper.readTree(auditPayload);
        } catch (JsonProcessingException e) {
            log.warn("Created audit with invalid json in payload - Correlation ID: {}, Raised by: {}, Namespace: {}, Timestamp: {}, EventType: {}, User: {}",
                    correlationID,
                    raisingService,
                    namespace,
                    auditTimestamp,
                    type,
                    userID, value(EVENT, INVALID_AUDIT_PAYLOAD_STORED));

            // Encode invalid json to base 64, otherwise it can be seen as nested invalid json
            return "{\"invalid_json\":\"" + Base64.getEncoder().encodeToString(auditPayload.getBytes()) + "\"}";
        }
        return auditPayload;
    }

}
