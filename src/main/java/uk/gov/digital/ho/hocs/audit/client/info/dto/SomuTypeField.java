package uk.gov.digital.ho.hocs.audit.client.info.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class SomuTypeField {
    String name;
    String extractColumnLabel;
    List<String> extractChoices;
}
