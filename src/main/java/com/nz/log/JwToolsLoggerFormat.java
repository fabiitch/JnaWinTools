package com.nz.log;

public class JwToolsLoggerFormat {
    private JwToolsLoggerFormat() {
    }

    static String format(String msg, Object... params) {
        for (Object param : params) {
            String safe = java.util.regex.Matcher.quoteReplacement(
                    param != null ? param.toString() : "null"
            );
            msg = msg.replaceFirst("\\{}", safe);
        }
        return msg;
    }
}
