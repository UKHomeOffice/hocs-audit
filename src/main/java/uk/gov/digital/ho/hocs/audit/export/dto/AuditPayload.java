package uk.gov.digital.ho.hocs.audit.export.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public interface AuditPayload {

    @AllArgsConstructor
    @Getter
    class CaseReference {
        private String caseReference;
    }

    @AllArgsConstructor
    @Getter
    class Case {
        private UUID caseUUID;
    }

    @AllArgsConstructor
    @Getter
    class Topic {
        private UUID topicUuid;
        private String topicName;
    }

    @AllArgsConstructor
    @Getter
    class StageTeamAllocation {
        private UUID stageUUID;
        private UUID teamUUID;
        private String stage;
    }

    @AllArgsConstructor
    @Getter
    class StageUserAllocation {
        private UUID stageUUID;
        private UUID userUUID;
        private String stage;
    }

    @AllArgsConstructor
    @Getter
    class CreateCaseRequest {

        @JsonProperty("uuid")
        private UUID uuid;

        @JsonProperty("created")
        private LocalDateTime created;

        @JsonProperty("type")
        private String type;

        @JsonProperty("reference")
        private String reference;

        @JsonProperty("caseDeadline")
        private LocalDate caseDeadline;

        @JsonProperty("dateReceived")
        private LocalDate dateReceived;

    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    class CaseData {

        @JsonProperty("uuid")
        private UUID uuid;

        @JsonProperty("created")
        private LocalDateTime created;

        @JsonProperty("type")
        private String type;

        @JsonProperty("reference")
        private String reference;

        @JsonProperty("data")
        private Map<String, String> data;

        @JsonProperty("primaryTopic")
        private UUID primaryTopic;

        @JsonProperty("primaryCorrespondent")
        private UUID primaryCorrespondent;

        @JsonProperty("caseDeadline")
        private LocalDate caseDeadline;

        @JsonProperty("dateReceived")
        private LocalDate dateReceived;

    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    class Correspondent {

        @JsonProperty("uuid")
        private UUID uuid;

        @JsonProperty("created")
        private LocalDateTime created;

        @JsonProperty("type")
        private String type;

        @JsonProperty("caseUUID")
        private UUID caseUUID;

        @JsonProperty("fullname")
        private String fullname;

        @JsonProperty("address")
        private AddressDto address;

        @JsonProperty("telephone")
        private String telephone;

        @JsonProperty("email")
        private String email;

        @JsonProperty("reference")
        private String reference;

    }

}
