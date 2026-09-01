package com.nz.jnawintools.window.result;

import lombok.Getter;

@Getter
public class HwndResult extends WinApiResult {
    private final long hwnd;

    public static HwndResult success(long hwnd) {
        return new HwndResult(hwnd);
    }

    public static HwndResult failure(int errorCode) {
        return new HwndResult(errorCode);
    }

    protected HwndResult(long hwnd) {
        super();
        this.hwnd = hwnd;
    }

    protected HwndResult(int errorCode) {
        super(errorCode);
        this.hwnd = 0L;
    }
}
