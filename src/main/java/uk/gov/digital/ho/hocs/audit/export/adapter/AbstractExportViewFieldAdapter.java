package uk.gov.digital.ho.hocs.audit.export.adapter;

import lombok.AllArgsConstructor;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;

@AllArgsConstructor
public abstract class AbstractExportViewFieldAdapter implements ExportViewFieldAdapter {

    protected InfoClient infoClient;


}
