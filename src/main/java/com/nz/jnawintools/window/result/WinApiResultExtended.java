package com.nz.jnawintools.window.result;

import lombok.Getter;


@Getter
public class WinApiResultExtended<T> extends WinApiResult {
    private final T result;

    public static <T> WinApiResultExtended<T> success(T value) {
        return new WinApiResultExtended<>(value);
    }

    public static <T> WinApiResultExtended<T> failureValue(int errorCode) {
        return new WinApiResultExtended<>(errorCode);
    }

    // Constructeur succès
    protected WinApiResultExtended(T result) {
        super();
        this.result = result;
    }

    // Constructeur échec
    protected WinApiResultExtended(int errorCode) {
        super(errorCode);
        this.result = null;
    }

}
