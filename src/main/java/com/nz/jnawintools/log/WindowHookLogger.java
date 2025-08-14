package com.nz.jnawintools.log;

public class WindowHookLogger implements JWTLogger {
    private String windowName;
    private JWTLogger logger;

    public WindowHookLogger(String windowName, JWTLogger logger) {
        this.windowName = "[" + windowName + "]";
        this.logger = logger;
    }

    @Override
    public void debug(String message) {
        logger.debug(windowName+message);
    }

    @Override
    public void log(String message) {
        logger.log(windowName+message);
    }

    @Override
    public void error(String message) {
        logger.error(windowName+message);
    }
}
