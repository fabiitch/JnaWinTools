package com.nz.jnawintools.log.impl;

import com.nz.jnawintools.log.JWTLogger;

public class JWTSoutLogger implements JWTLogger {

    private static final String RESET = "\u001B[0m";
    private static final String GRAY = "\u001B[90m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";

    @Override
    public void debug(String message) {
        System.out.println(GRAY + message + RESET);
    }

    @Override
    public void log(String message) {
        System.out.println(GREEN + message + RESET);
    }

    @Override
    public void error(String message) {
        System.out.println(RED + message + RESET);
    }

}
