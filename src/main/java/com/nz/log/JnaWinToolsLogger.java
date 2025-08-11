package com.nz.log;

import static com.nz.log.JwToolsLoggerFormat.format;

public interface JnaWinToolsLogger {

    default void error(String message, Object... params) {
        error(format(message, params));
    }
    void error(String message);

    default void debug(String message, Object... params) {
        debug(format(message, params));
    }
    void debug(String message);

}
