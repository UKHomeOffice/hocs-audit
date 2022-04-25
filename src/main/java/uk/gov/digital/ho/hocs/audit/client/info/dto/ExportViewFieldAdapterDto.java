package uk.gov.digital.ho.hocs.audit.client.info.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExportViewFieldAdapterDto {

    private Long id;
    private Long parentExportViewFieldId;
    private Long sortOrder;
    private String type;
}
