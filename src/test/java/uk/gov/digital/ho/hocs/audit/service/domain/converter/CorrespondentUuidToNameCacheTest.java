package uk.gov.digital.ho.hocs.audit.service.domain.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.digital.ho.hocs.audit.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.client.casework.dto.GetCorrespondentOutlineResponse;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.AuditPayload;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles({"local", "extracts"})
public class CorrespondentUuidToNameCacheTest {
    @Mock
    CaseworkClient caseworkClient;

    @Mock
    AuditRepository auditRepository;

    @Autowired
    ObjectMapper objectMapper;

    private static final String[] CORRESPONDENT_EVENT_TYPES = {
        "CORRESPONDENT_CREATED",
        "CORRESPONDENT_DELETED",
        "CORRESPONDENT_UPDATED"
    };

    @Test
    public void whenCacheIsConstructed_thenCurrentSnapshotIsRequestedFromCasework() {
        setupCache();

        verify(caseworkClient, times(1)).getAllCorrespondents();
        verifyNoMoreInteractions(caseworkClient, auditRepository);
    }

    @Test
    public void whenCacheIsRequested_thenLatestEventsAreRequestedFromAuditRepository() {
        when(auditRepository.findAuditDataByDateRangeAndEvents(any(), any(), eq(CORRESPONDENT_EVENT_TYPES)))
            .thenReturn(Stream.of());

        CorrespondentUuidToNameCache cache = setupCache();

        Map<String, String> uuidToNameLookup = cache.getUuidToNameLookup();

        verify(auditRepository, times(1)).findAuditDataByDateRangeAndEvents(any(), any(), eq(CORRESPONDENT_EVENT_TYPES));
        verifyNoMoreInteractions(auditRepository);

        assertEquals(2, uuidToNameLookup.size());
        assertEquals("Correspondent One", uuidToNameLookup.get("2398f672-aed7-42d4-b7ba-5a86d942c34c"));
        assertEquals("Correspondent Two", uuidToNameLookup.get("c5c67130-43ee-4e0f-914e-2844f2b54623"));
    }

    @Test
    public void whenCacheIsRequestedAndNewEventsAreFound_thenTheCacheIsUpdatedFromTheEvents() throws JsonProcessingException {
        when(auditRepository.findAuditDataByDateRangeAndEvents(any(), any(), eq(CORRESPONDENT_EVENT_TYPES)))
            .thenReturn(Stream.of(
                stubAuditEvent("CORRESPONDENT_CREATED", UUID.fromString("4343d51a-f815-4797-b957-b19975dbdf4d"), "Correspondent Three"),
                stubAuditEvent("CORRESPONDENT_UPDATED", UUID.fromString("2398f672-aed7-42d4-b7ba-5a86d942c34c"), "Correspondent Updated"),
                stubAuditEvent("CORRESPONDENT_DELETED", UUID.fromString("c5c67130-43ee-4e0f-914e-2844f2b54623"), "Correspondent Two")
            ));

        CorrespondentUuidToNameCache cache = setupCache();

        Map<String, String> uuidToNameLookup = cache.getUuidToNameLookup();

        verify(auditRepository, times(1)).findAuditDataByDateRangeAndEvents(any(), any(), eq(CORRESPONDENT_EVENT_TYPES));
        verifyNoMoreInteractions(auditRepository);

        assertEquals(2, uuidToNameLookup.size());
        assertEquals("Correspondent Updated", uuidToNameLookup.get("2398f672-aed7-42d4-b7ba-5a86d942c34c"));
        assertEquals("Correspondent Three", uuidToNameLookup.get("4343d51a-f815-4797-b957-b19975dbdf4d"));
    }

    @Test
    public void whenCaseworkIsNotAvailableDuringBootstrap_theErrorIsIgnoredAndTheCacheLoadedOnFirstAccess() {
        when(caseworkClient.getAllCorrespondents()).thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));

        CorrespondentUuidToNameCache cache = new CorrespondentUuidToNameCache(
            caseworkClient,
            auditRepository,
            objectMapper
        );

        assertThrows(HttpServerErrorException.class, cache::getUuidToNameLookup);
        verify(caseworkClient, times(2)).getAllCorrespondents();
        verifyNoMoreInteractions(caseworkClient, auditRepository);
    }

    private CorrespondentUuidToNameCache setupCache() {
        when(caseworkClient.getAllCorrespondents()).thenReturn(Set.of(
            new GetCorrespondentOutlineResponse(UUID.fromString("2398f672-aed7-42d4-b7ba-5a86d942c34c"), "Correspondent One"),
            new GetCorrespondentOutlineResponse(UUID.fromString("c5c67130-43ee-4e0f-914e-2844f2b54623"), "Correspondent Two")
        ));

        return new CorrespondentUuidToNameCache(
            caseworkClient,
            auditRepository,
            objectMapper
        );
    }

    private AuditEvent stubAuditEvent(String type, UUID corresponentUUID, String fullname) throws JsonProcessingException {
        return new AuditEvent(
            UUID.randomUUID(),
            null,
            UUID.randomUUID().toString(),
            "casework",
            objectMapper.writeValueAsString(new AuditPayload.Correspondent(
                corresponentUUID,
                LocalDateTime.now(),
                "CORRESPONDENT",
                UUID.randomUUID(),
                fullname,
                null,
                null,
                null,
                null,
                null,
                null
            )),
            "local",
            LocalDateTime.now(),
            type,
            UUID.randomUUID().toString()
        );
    }
}
