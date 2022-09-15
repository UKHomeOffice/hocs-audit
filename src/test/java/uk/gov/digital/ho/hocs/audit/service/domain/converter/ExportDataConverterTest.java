package uk.gov.digital.ho.hocs.audit.service.domain.converter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.repository.entity.CaseReference;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;

@SpringBootTest
public class ExportDataConverterTest {

    @MockBean
    private AuditRepository auditRepository;

    @Test
    public void convertConstructorCallsForCaseReferences() {
        given(auditRepository.getCaseReferencesForType("TEST")).willReturn(Stream.of());

        var converter = new ExportDataConverter(Collections.emptyMap(), Collections.emptyMap(), "TEST",
            auditRepository);

        Mockito.verify(auditRepository).getCaseReferencesForType("TEST");
        Assertions.assertNotNull(converter);
    }

    @Test
    public void convertCaseUuidReturnsReference() {
        UUID caseUuid = UUID.randomUUID();

        CaseReference caseReference = new CaseReference() {
            @Override
            public String getCaseReference() {
                return "TEST_REF";
            }

            @Override
            public String getCaseUUID() {
                return caseUuid.toString();
            }
        };
        given(auditRepository.getCaseReferencesForType("TEST")).willReturn(Stream.of(caseReference));

        var converter = new ExportDataConverter(Collections.emptyMap(), Collections.emptyMap(), "TEST",
            auditRepository);

        Assertions.assertNotNull(converter);
        Assertions.assertEquals("TEST_REF", converter.convertCaseUuid(caseUuid));
    }

    @Test
    public void convertCaseUuidWithNonExistentReturnsUuid() {
        given(auditRepository.getCaseReferencesForType("TEST")).willReturn(Stream.of());

        var converter = new ExportDataConverter(Collections.emptyMap(), Collections.emptyMap(), "TEST",
            auditRepository);

        Assertions.assertNotNull(converter);

        UUID queryUuid = UUID.randomUUID();
        Assertions.assertEquals(queryUuid.toString(), converter.convertCaseUuid(queryUuid));
    }

    @Test
    public void convertValueReturnsFromUuidMap() {
        UUID randomUuid = UUID.randomUUID();
        Map<String, String> uuidMap = Map.of(randomUuid.toString(), "TEST");

        given(auditRepository.getCaseReferencesForType("TEST")).willReturn(Stream.of());

        var converter = new ExportDataConverter(uuidMap, Collections.emptyMap(), "TEST", auditRepository);

        Assertions.assertNotNull(converter);
        Assertions.assertEquals("TEST", converter.convertValue(randomUuid.toString()));
    }

    @Test
    public void convertValueReturnsFromNormalMap() {
        Map<String, String> entityMap = Map.of("TEST", "This,Test");

        given(auditRepository.getCaseReferencesForType("TEST")).willReturn(Stream.of());

        var converter = new ExportDataConverter(Collections.emptyMap(), entityMap, "TEST", auditRepository);

        Assertions.assertNotNull(converter);
        Assertions.assertEquals("ThisTest", converter.convertValue("TEST"));
    }

    @Test
    public void convertValueReturnsInputWithNonExistantValue() {
        given(auditRepository.getCaseReferencesForType("TEST")).willReturn(Stream.of());

        var converter = new ExportDataConverter(Collections.emptyMap(), Collections.emptyMap(), "TEST",
            auditRepository);

        Assertions.assertNotNull(converter);
        Assertions.assertEquals("This,Test", converter.convertValue("This,Test"));
    }

    @Test
    public void convertCaseUuidWithNonExistentReturnsReturnsUuid() {
        given(auditRepository.getCaseReferencesForType("TEST")).willReturn(Stream.of());

        var converter = new ExportDataConverter(Collections.emptyMap(), Collections.emptyMap(), "TEST",
            auditRepository);

        Assertions.assertNotNull(converter);

        UUID queryUuid = UUID.randomUUID();
        Assertions.assertEquals(queryUuid.toString(), converter.convertCaseUuid(queryUuid));
    }

    @Test
    public void convertCaseUuidWithNullReturnsUuid() {
        given(auditRepository.getCaseReferencesForType("TEST")).willReturn(Stream.of());

        var converter = new ExportDataConverter(Collections.emptyMap(), Collections.emptyMap(), "TEST",
            auditRepository);

        Assertions.assertNotNull(converter);
        Assertions.assertNull(converter.convertCaseUuid(null));
    }

    @Test
    public void convertValueWithNullReturnsUuid() {
        given(auditRepository.getCaseReferencesForType("TEST")).willReturn(Stream.of());

        var converter = new ExportDataConverter(Collections.emptyMap(), Collections.emptyMap(), "TEST",
            auditRepository);

        Assertions.assertNotNull(converter);
        Assertions.assertNull(converter.convertValue(null));
    }

    @Test
    public void nonConversionConvertCaseUuidReturnsValue() {
        var converter = new ExportDataConverter();

        Assertions.assertNotNull(converter);
        Assertions.assertEquals("TEST", converter.convertValue("TEST"));
    }

    @Test
    public void nonConversionConvertCaseUuidReturnsUuid() {
        var converter = new ExportDataConverter();
        var queryUuid = UUID.randomUUID();

        Assertions.assertNotNull(converter);
        Assertions.assertEquals(queryUuid.toString(), converter.convertCaseUuid(queryUuid));
    }

}
