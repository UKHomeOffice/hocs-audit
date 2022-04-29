package uk.gov.digital.ho.hocs.audit.service;

import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.digital.ho.hocs.audit.client.info.dto.UserDto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.BDDMockito.given;

public class StaticUserServiceTest extends BaseExportServiceTest{

    @Autowired
    private StaticUserService staticUserService;

    @Test
    public void shouldReturnExport() throws IOException {
        UserDto userDto =
                new UserDto(UUID.randomUUID().toString(), "Username", "First", "Last", "test@example.com");

        given(infoClient.getUsers()).willReturn(Set.of(userDto));

        staticUserService.export(outputStream, false);

        var result = outputStream.toString(StandardCharsets.UTF_8);
        Assertions.assertNotNull(result);

        var headers = getCsvHeaderRow(result);
        Assertions.assertEquals(5, headers.length);

        var rows = getCsvDataRows(result)
                .stream()
                .map(CSVRecord::toList).collect(Collectors.toList());
        var expectedRows = List.of(
                List.of(userDto.getId(), userDto.getUsername(), userDto.getFirstName(), userDto.getLastName(), userDto.getEmail())
        );

        Assertions.assertEquals(2, rows.size());
        Assertions.assertTrue(rows.containsAll(expectedRows));
    }

}
