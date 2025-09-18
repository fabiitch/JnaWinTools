package com.nz.jnawintools.win32;

import com.nz.jnawintools.window.structure.DEVMODE;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;

public interface User32Extended extends StdCallLibrary {
    User32Extended INSTANCE = Native.load("user32", User32Extended.class);

    boolean IsWindow(WinDef.HWND hWnd);

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

    /**
     * BOOL ScreenToClient(HWND hWnd, LPPOINT lpPoint);
     * Convertit des coords écran -> coords client (logiques) de la fenêtre.
     */
    boolean ScreenToClient(WinDef.HWND hWnd, WinDef.POINT lpPoint);

    /**
     * BOOL ClientToScreen(HWND hWnd, LPPOINT lpPoint);
     * L’inverse : coords client -> coords écran.
     */
    boolean ClientToScreen(WinDef.HWND hWnd, WinDef.POINT lpPoint);

    /**
     * BOOL GetClientRect(HWND hWnd, LPRECT lpRect);
     * Récupère la taille de la zone client (sans bordures/titres) en coords client.
     */
    boolean GetClientRect(WinDef.HWND hWnd, WinDef.RECT lpRect);
}
