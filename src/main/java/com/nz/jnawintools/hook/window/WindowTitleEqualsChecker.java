package com.nz.jnawintools.hook.window;

import com.nz.jnawintools.window.Window64Utils;
import com.nz.jnawintools.window.result.WinApiResultExtended;
import com.sun.jna.platform.win32.WinDef;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WindowTitleEqualsChecker implements WindowChecker {
    private final String expected;

    public static WindowTitleEqualsChecker get(String expected) {
        return new WindowTitleEqualsChecker(expected);
    }

    @Override
    public boolean isWindow(WinDef.HWND hwnd) {
        WinApiResultExtended<String> nameResult = Window64Utils.getName(hwnd);
        return nameResult.isSuccess() && nameResult.getResult().equals(expected);
    }

    @Override
    public String getWindowName() {
        return expected;
    }
}
