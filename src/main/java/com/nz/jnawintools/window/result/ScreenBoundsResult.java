package com.nz.jnawintools.window.result;

import java.awt.*;

public class ScreenBoundsResult extends WinApiResultExtended<Rectangle> {
    public static ScreenBoundsResult success(Rectangle value) {
        return new ScreenBoundsResult(value);
    }

    public static ScreenBoundsResult failure(int errorCode) {
        return new ScreenBoundsResult(errorCode);
    }

    protected ScreenBoundsResult(Rectangle value) {
        super(value);
    }

    protected ScreenBoundsResult(int errorCode) {
        super(errorCode);
    }
}
