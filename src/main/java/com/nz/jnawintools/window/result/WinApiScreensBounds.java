package com.nz.jnawintools.window.result;

import java.awt.*;
import java.util.List;

public class WinApiScreensBounds extends WinApiResultExtended<List<Rectangle>>{
    public WinApiScreensBounds(List<Rectangle> value) {
        super(value);
    }
    public WinApiScreensBounds(int errorCode) {
        super(errorCode);
    }
}
