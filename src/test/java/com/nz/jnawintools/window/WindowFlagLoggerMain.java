package com.nz.jnawintools.window;

import org.junit.jupiter.api.Test;

public class WindowFlagLoggerMain {
    private static final String WINDOW_NAME_PROPERTY = "windowName";
    private static final String WINDOW_NAME_ENV = "WINDOW_NAME";
    private static final String DEFAULT_WINDOW_NAME = "Calculatrice";

    static void main() {
        WindowFlagLogger.printWindowFlagsByName("ANGLE D3D11 MPO Candidate");

    }
}
