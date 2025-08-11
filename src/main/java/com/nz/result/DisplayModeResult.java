package com.nz.result;

import com.nz.enums.WindowDisplayMode;
import lombok.Getter;

@Getter
public class DisplayModeResult extends WinApiResult {
    private final WindowDisplayMode displayMode;

    public static DisplayModeResult success(WindowDisplayMode mode) {
        return new DisplayModeResult(mode);
    }

    public static DisplayModeResult failure(int errorCode) {
        return new DisplayModeResult(errorCode);
    }

    protected DisplayModeResult(WindowDisplayMode displayMode) {
        super(); // success = true
        this.displayMode = displayMode;
    }

    protected DisplayModeResult(int errorCode) {
        super(errorCode); // success = false
        this.displayMode = null;
    }

}
