package uk.gov.digital.ho.hocs.audit.core.utils;


import uk.gov.digital.ho.hocs.audit.service.domain.ExportType;

import java.time.LocalDate;

public class FileNameHelper {

    private FileNameHelper() {}

    public static String getFileName(String caseType, String export) {
        return String.format("%s-%s-%s.csv", caseType.toLowerCase(), export, LocalDate.now());
    }

    public static String getFilename(String export) {
        return String.format("%s-%s.csv", export, LocalDate.now());
    }

    public static String getFileName(String caseType, ExportType exportType) {
        return String.format("%s-%s-%s.csv", caseType.toLowerCase(), exportType.toString().toLowerCase(), LocalDate.now());
    }

}
