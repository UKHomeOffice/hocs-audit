package uk.gov.digital.ho.hocs.audit.export.infoclient;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.EntityDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UserDto;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class InfoClientSupplier {

    private final InfoClient infoClient;

    public InfoClientSupplier(InfoClient infoClient) {
        this.infoClient = infoClient;
    }

    public Supplier<Map<String,String>> getUsers() {
        return () -> infoClient.getUsers().stream()
                    .collect(Collectors.toMap(UserDto::getId, UserDto::getUsername));
    }

    public Supplier<Map<String, String>> getEntityList(String listName) {
        return () -> infoClient.getEntitiesForList(listName).stream()
                .collect(Collectors.toMap(EntityDto::getSimpleName, entity ->  entity.getData().getTitle()));
    }

}
