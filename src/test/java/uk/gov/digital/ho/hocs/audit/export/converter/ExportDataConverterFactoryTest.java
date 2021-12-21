package uk.gov.digital.ho.hocs.audit.export.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.EntityDataDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.EntityDto;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExportDataConverterFactoryTest {

    @Mock
    private InfoClient infoClient;

    @Mock
    private CaseworkClient caseworkClient;

    private ExportDataConverterFactory converterFactory;

    @BeforeEach
    public void before() {

        when(infoClient.getUsers()).thenReturn(Set.of());
        when(infoClient.getAllTeams()).thenReturn(Set.of());
        when(infoClient.getUnits()).thenReturn(Set.of());
        when(caseworkClient.getAllCaseTopics()).thenReturn(Set.of());
        when(caseworkClient.getAllActiveCorrespondents()).thenReturn(Set.of());

        converterFactory = new ExportDataConverterFactory(infoClient, caseworkClient);
    }

    @Test
    public void shouldGenerateExportDataConverter() {
        ExportDataConverter exportDataConverter = converterFactory.getInstance();

        assertThat(exportDataConverter).isNotNull();
    }

    @Test
    public void shouldHandleDuplicateSimpleName() {
        Set<EntityDto> entityDtos = Set.of(new EntityDto(1L, UUID.randomUUID(), "Name1", new EntityDataDto("title1"), UUID.randomUUID(), true),
                new EntityDto(1L, UUID.randomUUID(), "Name1", new EntityDataDto("title1"), UUID.randomUUID(), true));
        when(infoClient.getEntitiesForList("MPAM_ENQUIRY_SUBJECTS")).thenReturn(entityDtos);

        ExportDataConverter exportDataConverter = converterFactory.getInstance();

        assertThat(exportDataConverter).isNotNull();
    }
}
