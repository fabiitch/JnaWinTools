package com.nz.result;

import com.nz.utils.WindowsErrorMap;
import lombok.Getter;

@Getter
public class WinApiResult {

    private final int errorCode;
    private final boolean success;

    public static WinApiResult success() {
        return new WinApiResult();
    }

    public static WinApiResult failure(int errorCode) {
        return new WinApiResult(errorCode);
    }

    protected WinApiResult(int errorCode) {
        this.errorCode = errorCode;
        this.success = false;
    }

    protected WinApiResult() {
        this.success = true;
        this.errorCode = 0;
    }

    public boolean isFailure() {
        return !success;
    }


    public String getErrorMessage() {
        return "Error code=" + errorCode+" ," + WindowsErrorMap.getErrorMessage(errorCode);
    }
}
