package uk.gov.digital.ho.hocs.audit.service.domain.adapter;

public interface ExportViewFieldAdapter {

    String getAdapterType();

    String convert(Object input);

}
