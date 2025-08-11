package com.nz.result;

public class ForeGroundWindowNameResult extends WinApiResultExtended<String> {

    public static ForeGroundWindowNameResult success(String name) {
        return new ForeGroundWindowNameResult(name);
    }

    public static ForeGroundWindowNameResult failure(int errorCode) {
        return new ForeGroundWindowNameResult(errorCode);
    }


    protected ForeGroundWindowNameResult(String name) {
        super(name);
    }

    protected ForeGroundWindowNameResult(int errorCode) {
        super(errorCode);
    }

}
