package com.nz.jnawintools.window.result;

import lombok.Getter;

@Getter
public class WindowStyleResult extends WinApiResult {
    private final long style;

    public static WindowStyleResult success(long style) {
        return new WindowStyleResult(style);
    }

    public static WindowStyleResult failure(int errorCode) {
        return new WindowStyleResult(errorCode);
    }

    public WindowStyleResult(long style) {
        super();
        this.style = style;
    }

    public WindowStyleResult(int errorCode) {
        super(errorCode);
        this.style=0;
    }

}
