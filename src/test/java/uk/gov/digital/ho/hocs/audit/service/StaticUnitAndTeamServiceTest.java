package uk.gov.digital.ho.hocs.audit.service;

import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.digital.ho.hocs.audit.client.info.dto.TeamDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.UnitDto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.BDDMockito.given;

public class StaticUnitAndTeamServiceTest extends BaseExportServiceTest {

    @Autowired
    private StaticUnitAndTeamService staticUnitAndTeamService;

    @Test
    public void shouldReturnExport() throws IOException {
        UnitDto unitDto = new UnitDto("TEST_UNIT", UUID.randomUUID().toString(), "1");
        UnitDto unit2Dto = new UnitDto("TEST2_UNIT", UUID.randomUUID().toString(), "2");
        TeamDto teamDto = new TeamDto("TEST1", UUID.randomUUID(), true, unitDto.getUuid());

        given(infoClient.getUnits()).willReturn(Set.of(unitDto, unit2Dto));
        given(infoClient.getTeamsForUnit(unitDto.getUuid())).willReturn(Set.of(teamDto));

        staticUnitAndTeamService.export(outputStream, false);

        var result = outputStream.toString(StandardCharsets.UTF_8);
        Assertions.assertNotNull(result);

        var headers = getCsvHeaderRow(result);
        Assertions.assertEquals(4, headers.length);

        var rows = getCsvDataRows(result).stream().map(CSVRecord::toList).collect(Collectors.toList());
        var expectedRows = List.of(List.of(unitDto.getUuid(), unitDto.getDisplayName(), teamDto.getUuid().toString(),
            teamDto.getDisplayName()), List.of(unit2Dto.getUuid(), unit2Dto.getDisplayName(), "", ""));

        Assertions.assertEquals(3, rows.size());
        Assertions.assertTrue(rows.containsAll(expectedRows));
    }

}
