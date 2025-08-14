package com.nz.jnawintools.hook.window;

import com.sun.jna.platform.win32.WinDef;

public interface WindowChecker {
    boolean isWindow(WinDef.HWND hwnd);

    public String getWindowName();
}
