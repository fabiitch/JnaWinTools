package com.nz.jnawintools.window.result;

import java.awt.*;
import java.util.List;

public class WinApiScreensBounds extends WinApiResultExtended<List<Rectangle>>{
    public static WinApiScreensBounds success(List<Rectangle> values) {
        return new WinApiScreensBounds(values);
    }

    public static WinApiScreensBounds failure(int errorCode) {
        return new WinApiScreensBounds(errorCode);
    }


    protected WinApiScreensBounds(List<Rectangle> value) {
        super(value);
    }
    protected WinApiScreensBounds(int errorCode) {
        super(errorCode);
    }
}
