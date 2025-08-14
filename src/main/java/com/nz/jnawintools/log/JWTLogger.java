package com.nz.jnawintools.log;

import static com.nz.jnawintools.log.utils.JWTLogFormat.format;

public interface JWTLogger {

    default void debug(String message, Object... params) {
        debug(format(message, params));
    }
    void debug(String message);


    default void log(String message, Object... params) {
        error(format(message, params));
    }
    void log(String message);

    default void error(String message, Object... params) {
        error(format(message, params));
    }
    void error(String message);
}
