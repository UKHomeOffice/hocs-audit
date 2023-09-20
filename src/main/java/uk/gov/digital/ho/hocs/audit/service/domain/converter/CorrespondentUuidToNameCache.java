package uk.gov.digital.ho.hocs.audit.service.domain.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.client.casework.dto.GetCorrespondentOutlineResponse;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.AuditPayload;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@Profile("extracts")
public class CorrespondentUuidToNameCache {

    private final CaseworkClient caseworkClient;

    private final AuditRepository auditRepository;

    private final ObjectMapper objectMapper;

    private Map<String, String> lookup;

    private LocalDateTime lastUpdate;

    private static final String[] CORRESPONDENT_EVENT_TYPES = {
        "CORRESPONDENT_CREATED",
        "CORRESPONDENT_DELETED",
        "CORRESPONDENT_UPDATED"
    };

    private record CorrespondentEvent(String eventType, LocalDateTime timestamp, AuditPayload.Correspondent payload) {}

    public CorrespondentUuidToNameCache(
        CaseworkClient caseworkClient,
        AuditRepository auditRepository,
        ObjectMapper objectMapper
    ) {
        this.caseworkClient = caseworkClient;
        this.auditRepository = auditRepository;
        this.objectMapper = objectMapper;

        try {
            refreshCacheFromCasework();
        } catch (Exception e) {
            log.error("Failed to cache correspondents from casework", e);
            lookup = null;
        }
    }

    public Map<String, String> getUuidToNameLookup() {
        if (lookup == null) {
            refreshCacheFromCasework();
        }

        updateLookupFromRecentAuditEvents();

        return Collections.unmodifiableMap(lookup);
    }

    public void refreshCacheFromCasework() {
        lastUpdate = LocalDateTime.now();

        lookup =
            caseworkClient
                .getAllCorrespondents().stream()
                .collect(Collectors.toMap(
                    res -> res.getUuid().toString(),
                    GetCorrespondentOutlineResponse::getFullname
                ));

    }

    private Stream<CorrespondentEvent> tryParseEvent(AuditEvent event) {
        try {
            return Stream.of(
                new CorrespondentEvent(
                    event.getType(),
                    event.getAuditTimestamp(),
                    objectMapper.readValue(
                        event.getAuditPayload(),
                        AuditPayload.Correspondent.class
                    )
                )
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to parse correspondent audit event {}", event.getUuid(), e);
            return Stream.of();
        }
    }

    private void updateLookupFromRecentAuditEvents() {
        auditRepository
            .findAuditDataByDateRangeAndEvents(lastUpdate, LocalDateTime.now(), CORRESPONDENT_EVENT_TYPES)
            .flatMap(this::tryParseEvent)
            .forEach(event -> {
                if (Objects.equals(event.eventType, "CORRESPONDENT_DELETED")) {
                    lookup.remove(event.payload.getUuid().toString());
                } else {
                    lookup.put(event.payload.getUuid().toString(), event.payload.getFullname());
                }

                lastUpdate = event.timestamp;
            });
    }

}
