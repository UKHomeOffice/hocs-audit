package uk.gov.digital.ho.hocs.audit.export.adapter;

public interface ExportViewFieldAdapter {

    String getAdapterType();

    String convert(Object input);
}
