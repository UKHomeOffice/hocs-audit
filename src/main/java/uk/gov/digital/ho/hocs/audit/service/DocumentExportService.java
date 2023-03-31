package uk.gov.digital.ho.hocs.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.audit.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.client.casework.dto.GetCorrespondentOutlineResponse;
import uk.gov.digital.ho.hocs.audit.client.info.InfoClient;
import uk.gov.digital.ho.hocs.audit.client.info.dto.CaseTypeDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.UserDto;
import uk.gov.digital.ho.hocs.audit.core.utils.ZonedDateTimeConverter;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.AuditPayload;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;
import uk.gov.digital.ho.hocs.audit.service.domain.ExportType;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.ExportDataConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.HeaderConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.MalformedDateConverter;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DocumentExportService extends DynamicExportService {

    private static final String[] EVENTS = { "DOCUMENT_CREATED" };

    public DocumentExportService(ObjectMapper objectMapper,
                                 AuditRepository auditRepository,
                                 InfoClient infoClient,
                                 CaseworkClient caseworkClient,
                                 HeaderConverter headerConverter,
                                 MalformedDateConverter malformedDateConverter) {
        super(objectMapper, auditRepository, infoClient, caseworkClient, headerConverter, malformedDateConverter);
    }

    @Override
    public ExportType getExportType() {
        return ExportType.DOCUMENTS;
    }

    @Override
    protected String[] parseData(AuditEvent audit,
                                 ZonedDateTimeConverter zonedDateTimeConverter,
                                 ExportDataConverter exportDataConverter) throws JsonProcessingException {
        AuditPayload.Document documentData = objectMapper.readValue(audit.getAuditPayload(),
            AuditPayload.Document.class);

        List<String> data = new ArrayList<>();
        data.add(exportDataConverter.convertCaseUuid(audit.getCaseUUID()));
        data.add(zonedDateTimeConverter.convert(audit.getAuditTimestamp()));
        data.add(documentData.getDocumentTitle());
        data.add(documentData.getDocumentType());

        return data.toArray(new String[0]);
    }

    @Override
    protected Stream<AuditEvent> getData(LocalDate from, LocalDate to, String caseTypeCode, String[] events) {
        LocalDateTime peggedTo = to.isBefore(LocalDate.now())
            ? LocalDateTime.of(to, LocalTime.MAX)
            : LocalDateTime.now();

        return auditRepository.findAuditDataByDateRangeAndEvents(LocalDateTime.of(from, LocalTime.MIN), peggedTo,
            events, caseTypeCode);
    }

    @Override
    @Transactional(readOnly = true)
    public void export(LocalDate from,
                       LocalDate to,
                       OutputStream outputStream,
                       String caseType,
                       boolean convert,
                       boolean convertHeader,
                       ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        var caseTypeDto = getCaseTypeCode(caseType);

        var data = getData(from, to, caseTypeDto.getShortCode(), EVENTS);
        var dataConverter = getDataConverter(convert, caseTypeDto);

        printData(outputStream, zonedDateTimeConverter, dataConverter, convertHeader, data);
    }

    @Override
    protected String[] getHeaders() {
        return new String[] { "caseUuid", "timestamp", "documentTitle", "documentType" };
    }

    @Override
    protected ExportDataConverter getDataConverter(boolean convert, CaseTypeDto caseType) {
        if (!convert) {
            return new ExportDataConverter();
        }

        Map<String, String> uuidToName = new HashMap<>();

        uuidToName.putAll(
            infoClient.getUsers().stream().collect(Collectors.toMap(UserDto::getId, UserDto::getUsername)));
        uuidToName.putAll(caseworkClient.getAllCorrespondents().stream().collect(
            Collectors.toMap(corr -> corr.getUuid().toString(), GetCorrespondentOutlineResponse::getFullname)));

        return new ExportDataConverter(uuidToName, Collections.emptyMap(), caseType.getShortCode(), auditRepository);
    }

}
