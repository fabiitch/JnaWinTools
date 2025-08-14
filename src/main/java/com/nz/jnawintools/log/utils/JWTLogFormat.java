package com.nz.jnawintools.log.utils;

public class JWTLogFormat {
    private JWTLogFormat() {
    }

    public static String format(String msg, Object... params) {
        for (Object param : params) {
            String safe = java.util.regex.Matcher.quoteReplacement(
                    param != null ? param.toString() : "null"
            );
            msg = msg.replaceFirst("\\{}", safe);
        }
        return msg;
    }
}
