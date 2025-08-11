package com.nz.jnawintools.window.result;

import com.sun.jna.platform.win32.WinDef;
import lombok.Getter;

@Getter
public class HwndResult extends WinApiResult {
    private final WinDef.HWND hwnd;
    public static HwndResult success( WinDef.HWND hwnd) {
        return new HwndResult(hwnd);
    }
    public static HwndResult failure(int errorCode) {
        return new HwndResult(errorCode);
    }

    protected HwndResult(WinDef.HWND hwnd) {
        super();
        this.hwnd = hwnd;
    }

    protected HwndResult(int errorCode) {
        super(errorCode);
        this.hwnd = null;
    }
}
