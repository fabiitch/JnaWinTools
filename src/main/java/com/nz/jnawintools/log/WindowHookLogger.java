package com.nz.jnawintools.log;

public class WindowHookLogger implements JnaWinToolsLogger {
    private String windowName;
    private JnaWinToolsLogger logger;

    public WindowHookLogger(String windowName, JnaWinToolsLogger logger) {
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
