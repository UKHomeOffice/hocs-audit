package uk.gov.digital.ho.hocs.audit.application;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.text.SimpleDateFormat;

@Configuration
public class HocsAuditServiceConfiguration {


    @Autowired
    public HocsAuditServiceConfiguration() {
    }

    @Bean
    public ObjectMapper initialiseObjectMapper() {
        ObjectMapper m = new ObjectMapper();
        m.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        m.registerModule(new JavaTimeModule());
        m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        m.enable(SerializationFeature.INDENT_OUTPUT);
        m.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        m.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return m;
    }

    @Bean(name = "json-jackson")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public JacksonDataFormat jacksonDataFormat(ObjectMapper objectMapper) {
        return new JacksonDataFormat(objectMapper, Object.class);
    }


}