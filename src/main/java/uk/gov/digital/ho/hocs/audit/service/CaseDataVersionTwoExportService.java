package uk.gov.digital.ho.hocs.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.client.info.InfoClient;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.repository.config.CaseDataFieldReader;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;
import uk.gov.digital.ho.hocs.audit.service.domain.ExportType;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.CorrespondentUuidToNameCache;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.HeaderConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.MalformedDateConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Service
@Profile("extracts")
public class CaseDataVersionTwoExportService extends CaseDataExportService {

    public CaseDataVersionTwoExportService(
        ObjectMapper objectMapper,
        AuditRepository auditRepository,
        InfoClient infoClient,
        CaseworkClient caseworkClient,
        HeaderConverter headerConverter,
        MalformedDateConverter malformedDateConverter,
        CorrespondentUuidToNameCache correspondentUuidToNameCache,
        CaseDataFieldReader caseDataFieldReader)
    {
        super(objectMapper, auditRepository, infoClient, caseworkClient, headerConverter, malformedDateConverter,
            correspondentUuidToNameCache, caseDataFieldReader);
    }

    private final static Set<String> UNIQUE_EVENT_TYPES = Set.of("CASE_CREATED");

    @Override
    public ExportType getExportType() {
        return ExportType.CASE_DATA_V2;
    }

    @Override
    protected Stream<AuditEvent> getData(LocalDate from, LocalDate to, String caseTypeCode, String[] events) {
        LocalDateTime peggedTo = to.isBefore(LocalDate.now())
            ? LocalDateTime.of(to, LocalTime.MAX)
            : LocalDateTime.now();

        String[] uniqueEvents = Arrays.stream(events).filter(UNIQUE_EVENT_TYPES::contains).toArray(String[]::new);
        String[] duplicatedEvents = Arrays.stream(events).filter(Predicate.not(UNIQUE_EVENT_TYPES::contains)).toArray(String[]::new);

        return auditRepository.findLastAuditDataByDateRangeAndEvents(
            LocalDateTime.of(from, LocalTime.MIN),
            peggedTo,
            caseTypeCode,
            uniqueEvents,
            duplicatedEvents);
    }
}
