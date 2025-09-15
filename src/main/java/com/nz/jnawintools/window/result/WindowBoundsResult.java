package com.nz.jnawintools.window.result;

import java.awt.*;

public class WindowBoundsResult extends WinApiResultExtended<Rectangle>{

    public static WindowBoundsResult success(Rectangle value) {
        return new WindowBoundsResult(value);
    }

    public static WindowBoundsResult failure(int errorCode) {
        return new WindowBoundsResult(errorCode);
    }


    protected WindowBoundsResult(Rectangle value) {
        super(value);
    }

    protected WindowBoundsResult(int errorCode) {
        super(errorCode);
    }
}
