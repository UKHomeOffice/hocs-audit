package uk.gov.digital.ho.hocs.audit.export.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
    class SomuItem {
        private UUID uuid;
        private UUID somuTypeUUID;
        private Map<String, String> data;
    }

    @AllArgsConstructor
    @Getter
    class Topic {
        private UUID topicUuid;
        private String topicName;
    }

    @AllArgsConstructor
    @Getter
    class StageAllocation {
        private UUID stageUUID;
        private UUID allocatedToUUID;
        private String stage;
        private LocalDate deadline;
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

    @NoArgsConstructor
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

        @Setter
        @JsonProperty("somuItems")
        private Set<SomuItem> allSomuItems;
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    class CaseNote {

        @JsonProperty("caseNoteType")
        private String caseNoteType;

        @JsonProperty("text")
        private String text;
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

        @JsonProperty("externalKey")
        private String externalKey;

    }

}
