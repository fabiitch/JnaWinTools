package com.nz.jnawintools.hook.window;

import com.sun.jna.platform.win32.WinDef;

public class WindowHwndChecker implements WindowChecker {
    private final WinDef.HWND target;

    public WindowHwndChecker(WinDef.HWND target) {
        this.target = target;
    }

    @Override
    public boolean isWindow(WinDef.HWND hwnd) {
        return hwnd != null && target != null;
    }

    @Override
    public String getWindowName() {
        return "";
    }
}
