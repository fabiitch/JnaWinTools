package com.nz.jnawintools.window.result;

public class WindowDpiResult extends WinApiResultExtended<Integer>{

    public WindowDpiResult(int errorCode) {
        super(errorCode);
    }

    public WindowDpiResult(int dpi, boolean success) {
        super(dpi);
    }


}
