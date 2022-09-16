package uk.gov.digital.ho.hocs.audit.client.info.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class CaseTypeActionDto {

    private UUID uuid;

    private UUID caseTypeUuid;

    private String caseType;

    private String actionType;

    private String actionLabel;

    private int maxConcurrentEvents;

    private int sortOrder;

    private boolean active;

    private String props;

}
