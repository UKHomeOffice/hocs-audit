package uk.gov.digital.ho.hocs.audit.utils;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class UuidStringChecker {

    private static final String UUID_REGEX = "^[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}$";

    private UuidStringChecker() {}

    public static boolean isUUID(String uuid) {
        if (StringUtils.hasText(uuid)) {
            return Pattern.compile(UUID_REGEX, Pattern.CASE_INSENSITIVE).matcher(uuid).matches();
        }
        return false;
    }

}
