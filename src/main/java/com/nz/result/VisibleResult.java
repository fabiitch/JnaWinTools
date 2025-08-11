package com.nz.result;


import com.sun.jna.platform.win32.WinDef;

public class VisibleResult extends WinApiResultExtended<Boolean> {
    public static VisibleResult success(boolean success) {
        return new VisibleResult(success);
    }

    public static VisibleResult failure(int errorCode) {
        return new VisibleResult(errorCode);
    }

    protected VisibleResult(Boolean value) {
        super(value);
    }

    protected VisibleResult(int errorCode) {
        super(errorCode);
    }
}
