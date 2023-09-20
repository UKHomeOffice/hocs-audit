package uk.gov.digital.ho.hocs.audit.entrypoint.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    class SomuItem {

        private UUID uuid;

        private UUID somuTypeUuid;

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

        @JsonProperty("migratedReference")
        private String migratedReference;

    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    class CaseNote {

        @JsonProperty("caseNoteType")
        private String caseNoteType;

        @JsonProperty("text")
        private String text;

    }

    @AllArgsConstructor
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

        @JsonProperty("organisation")
        private String organisation;

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

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    class Extension {

        @JsonProperty("caseId")
        private UUID caseId;

        @JsonProperty("createTimestamp")
        private LocalDateTime created;

        @JsonProperty("caseTypeActionUuid")
        private String type;

        @JsonProperty("note")
        private String note;

    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    class Appeal {

        @JsonProperty("caseTypeActionUuid")
        private UUID type;

        private String status;

        private LocalDate dateSentRMS;

        private String outcome;

        private String complexCase;

        private String note;

        private String officerType;

        private String officerName;

        private String officerDirectorate;

        private LocalDateTime created;

    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    class Interest {

        private String partyType;

        private String interestDetails;

    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    class Suspension {

        private LocalDate dateSuspensionApplied;

        private LocalDate dateSuspensionRemoved;

    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    class Document {

        private UUID documentUUID;

        private String documentType;

        private String documentTitle;

    }

}
