package uk.gov.digital.ho.hocs.audit.client.info.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ExportViewFieldDto {

    private Long id;
    private Long parentExportViewId;
    private Long sortOrder;
    private String displayName;
    private List<ExportViewFieldAdapterDto> adapters;
}
