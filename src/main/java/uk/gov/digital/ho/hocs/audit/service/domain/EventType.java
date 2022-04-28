package uk.gov.digital.ho.hocs.audit.service.domain;

public class EventType {
    public static final String STAGE_ALLOCATED_TO_TEAM = "STAGE_ALLOCATED_TO_TEAM";
    public static final String STAGE_CREATED = "STAGE_CREATED";
    public static final String STAGE_RECREATED = "STAGE_RECREATED";
    public static final String STAGE_COMPLETED = "STAGE_COMPLETED";
    public static final String STAGE_ALLOCATED_TO_USER = "STAGE_ALLOCATED_TO_USER";
    public static final String STAGE_UNALLOCATED_FROM_USER = "STAGE_UNALLOCATED_FROM_USER";


    private String eventName;

    EventType(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }
}
