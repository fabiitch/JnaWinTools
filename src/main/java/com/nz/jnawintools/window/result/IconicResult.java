package com.nz.jnawintools.window.result;

import lombok.Getter;

@Getter
public class IconicResult extends WinApiResult {
    private final boolean iconic;
    public static IconicResult success(boolean success) {
        return new IconicResult(success);
    }

    public static IconicResult failure(int errorCode) {
        return new IconicResult(errorCode);
    }
    protected IconicResult(boolean iconic) {
        super(); // success=true
        this.iconic = iconic;
    }

    protected IconicResult(int errorCode) {
        super(errorCode); // success=false
        this.iconic = false;
    }
}
