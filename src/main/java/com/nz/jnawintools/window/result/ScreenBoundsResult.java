package com.nz.jnawintools.window.result;

import java.awt.*;

public class ScreenBoundsResult extends WinApiResultExtended<Rectangle>{

    public ScreenBoundsResult(Rectangle value) {
        super(value);
    }

    public ScreenBoundsResult(int errorCode) {
        super(errorCode);
    }
}
