package com.saathisquare.societyservice.util;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

public class FilterNormalizerUtil {

    public static Integer normalizeInteger(Integer value) {
        return value == null ? null : value;
    }

    public static String normalizeString(String value) {
        return StringUtils.isBlank(value)?null:value;
    }
    
    public static UUID normalizeUUID(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return UUID.fromString(value);
    }


    public static Long normalizeLong(Long value) {
        return (value == null || value == 0L) ? null : value;
    }

    public static Boolean normalizeBoolean(Boolean value) {
        return (value != null) ? value : null;
    }
}
