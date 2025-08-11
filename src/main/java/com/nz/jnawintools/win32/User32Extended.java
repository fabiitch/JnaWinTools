package com.nz.jnawintools.win32;

import com.nz.jnawintools.window.structure.DEVMODE;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;

public interface User32Extended extends StdCallLibrary {
    User32Extended INSTANCE = Native.load("user32", User32Extended.class);

    boolean IsIconic(WinDef.HWND hWnd);

    int GetDpiForWindow(WinDef.HWND hWnd);

    boolean EnumDisplaySettingsW(String deviceName, int modeNum, DEVMODE devMode);



    boolean RegisterClassExW(WinUser.WNDCLASSEX lpwcx);
    WinDef.HWND CreateWindowExW(
        int dwExStyle,
        String lpClassName,
        String lpWindowName,
        int dwStyle,
        int x,
        int y,
        int nWidth,
        int nHeight,
        WinDef.HWND hWndParent,
        WinDef.HMENU hMenu,
        WinDef.HINSTANCE hInstance,
        WinDef.LPVOID lpParam);

    boolean DestroyWindow(WinDef.HWND hWnd);
}
