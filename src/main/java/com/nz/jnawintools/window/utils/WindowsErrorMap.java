package com.nz.jnawintools.window.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WindowsErrorMap {
    private static final Map<Integer, String> WINDOWS_ERROR_MAP;
    static {
        Map<Integer, String> map = new HashMap<>();
        map.put(1400, "ERROR_INVALID_WINDOW_HANDLE");
        map.put(5, "ERROR_ACCESS_DENIED");
        map.put(2, "ERROR_FILE_NOT_FOUND");
        map.put(6, "ERROR_INVALID_HANDLE");
        map.put(87, "ERROR_INVALID_PARAMETER");
        map.put(998, "ERROR_NOACCESS");
        map.put(1008, "ERROR_INVALID_DATA");
        map.put(1004, "ERROR_INVALID_FLAGS");
        map.put(1003, "ERROR_CANNOT_MAKE");
        map.put(122, "ERROR_INSUFFICIENT_BUFFER");
        map.put(120, "ERROR_CALL_NOT_IMPLEMENTED");
        map.put(1450, "ERROR_NO_SYSTEM_RESOURCES");

        WINDOWS_ERROR_MAP = Collections.unmodifiableMap(map);
    }

    public static String getErrorMessage(int errorCode) {
        return WINDOWS_ERROR_MAP.getOrDefault(errorCode, "Unknown error code: " + errorCode);
    }

}
