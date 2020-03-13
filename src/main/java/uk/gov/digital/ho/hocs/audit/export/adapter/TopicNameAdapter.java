package uk.gov.digital.ho.hocs.audit.export.adapter;

import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetTopicResponse;
import uk.gov.digital.ho.hocs.audit.export.infoclient.ExportViewConstants;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UnitDto;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;


public class TopicNameAdapter implements ExportViewFieldAdapter {

    private Set<GetTopicResponse> topics;

    public TopicNameAdapter(Set<GetTopicResponse> topics) {
        this.topics = topics;
    }

    @Override
    public String getAdapterType() {
        return ExportViewConstants.FIELD_ADAPTER_TOPIC_NAME;
    }

    @Override
    public String convert(Object input) {
        if (input instanceof String && !StringUtils.isEmpty(input)) {
            Optional<GetTopicResponse> topicResponse = topics.stream().filter(t-> t.getUuid().compareTo((UUID.fromString((String)input))) == 0).findFirst();
            return topicResponse.isPresent() ? topicResponse.get().getTopicText() : null;
        }
        return null;
    }
}

