package uk.gov.digital.ho.hocs.audit.service;

import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.digital.ho.hocs.audit.client.info.dto.TeamDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.TopicTeamDto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.BDDMockito.given;

public class StaticTopicAndTeamServiceTest extends BaseExportServiceTest{

    @Autowired
    private StaticTopicAndTeamService staticTopicAndTeamService;

    @Test
    public void shouldReturnExport() throws IOException {
        TeamDto teamDto = new TeamDto("TEST1", UUID.randomUUID(), true, UUID.randomUUID().toString());
        TopicTeamDto topicTeamDto = new TopicTeamDto("TEST", UUID.randomUUID(), Set.of(teamDto));

        given(infoClient.getTopicsWithTeams("TEST")).willReturn(Set.of(topicTeamDto));

        staticTopicAndTeamService.export(printWriter, "TEST",false);

        var result = outputStream.toString(StandardCharsets.UTF_8);
        Assertions.assertNotNull(result);

        var headers = getCsvHeaderRow(result);
        Assertions.assertEquals(5, headers.length);

        var rows = getCsvDataRows(result)
                .stream()
                .map(CSVRecord::toList).collect(Collectors.toList());
        var expectedRows = List.of(
                List.of("TEST", topicTeamDto.getUuid().toString(), topicTeamDto.getDisplayName(), teamDto.getUuid().toString(), teamDto.getDisplayName()));

        Assertions.assertEquals(2, rows.size());
        Assertions.assertTrue(rows.containsAll(expectedRows));
    }

}
