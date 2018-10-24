package uk.gov.digital.ho.hocs.audit.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "command"
)
@JsonSubTypes({ // Keep this list alphabetical
})

public abstract class HocsCommand implements Command {

    @Getter
    @JsonProperty("command")
    protected String command;


    @JsonCreator
    public HocsCommand(String command) {
        this.command = command;
    }

    protected void initialiseDependencies(HocsAuditContext hocsAuditContext) {

    }

}