package com.nz.result;

import java.awt.*;

public class WindowBoundsResult extends WinApiResultExtended<Rectangle>{

    public WindowBoundsResult(Rectangle value) {
        super(value);
    }

    public WindowBoundsResult(int errorCode) {
        super(errorCode);
    }
}
