package com.nz.utils;


import com.nz.call.Dwmapi;
import com.nz.call.User32Extended;
import com.nz.structure.DEVMODE;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import javax.swing.*;

public class WindowMonitorUtils {
    static final int DEVMODE_SIZE = 220;

    private final static User32Extended USER_32_EXTENDED = User32Extended.INSTANCE;
    static int ENUM_CURRENT_SETTINGS = -1;
    static int ENUM_REGISTRY_SETTINGS = -2;


    public static boolean isDwmCompositionEnabled() {
        WinDef.BOOLByReference pfEnabled = new WinDef.BOOLByReference();
        int hr = Dwmapi.INSTANCE.DwmIsCompositionEnabled(pfEnabled);
        if (hr == 0) {
            return pfEnabled.getValue().booleanValue();
        }
        return false; // En cas d'erreur, on considère DWM désactivé
    }

    public static boolean hasResolutionChangedForWindow(WinDef.HWND hwnd) {
        WinUser.HMONITOR hMonitor = User32.INSTANCE.MonitorFromWindow(hwnd, User32.MONITOR_DEFAULTTONEAREST);
        if (hMonitor == null) {
            return false; // impossible de détecter
        }
        return hasResolutionChangedForMonitor(hMonitor);
    }

    public static boolean hasResolutionChangedForMonitor(WinUser.HMONITOR hMonitor) {
        // Récupère le moniteur de la fenêtre

        // Récupère le nom du display (\\.\DISPLAYx)
        User32.MONITORINFOEX info = new User32.MONITORINFOEX();
        info.cbSize = info.size();
        WinDef.BOOL bool = User32.INSTANCE.GetMonitorInfo(hMonitor, info);
        if (bool == null || !bool.booleanValue())
            return false;

        String displayName = Native.toString(info.szDevice);

        // Prépare les structures DEVMODE
        DEVMODE desktopMode = new DEVMODE();
        desktopMode.dmSize = (short) desktopMode.size();
        DEVMODE currentMode = new DEVMODE();
        currentMode.dmSize = (short) currentMode.size();

        // Charge les résolutions
        boolean gotDesktop = User32Extended.INSTANCE.EnumDisplaySettingsW(displayName, ENUM_REGISTRY_SETTINGS, desktopMode);
        boolean gotCurrent = User32Extended.INSTANCE.EnumDisplaySettingsW(displayName, ENUM_CURRENT_SETTINGS, currentMode);
        if (!gotDesktop || !gotCurrent) {
            return false;
        }

        return currentMode.dmPelsWidth != desktopMode.dmPelsWidth ||
            currentMode.dmPelsHeight != desktopMode.dmPelsHeight ||
            currentMode.dmDisplayFrequency != desktopMode.dmDisplayFrequency;
    }
    public static boolean canStayOnTop(WinDef.HWND gameHwnd) {
        // Crée une petite fenêtre
        WinDef.HWND dummy = createTestWindow();

        if (dummy == null) {
            return false; // Impossible de créer
        }

        // Tente de la mettre en TOPMOST
        boolean ok =  User32.INSTANCE.SetWindowPos(
            dummy,
            new WinDef.HWND(new Pointer(-1)), // HWND_TOPMOST
            0, 0, 0, 0,
            WinUser.SWP_NOMOVE | WinUser.SWP_NOSIZE | WinUser.SWP_NOACTIVATE| WinUser.SWP_SHOWWINDOW
        );

        // Vérifie si le jeu reste au premier plan
        WinDef.HWND foreground = User32.INSTANCE.GetForegroundWindow();

        // Détruit la fenêtre après test
        USER_32_EXTENDED.DestroyWindow(dummy);

        return !foreground.equals(gameHwnd);
        // Si NOT foreground = notre fenêtre a réussi à passer par-dessus, donc PAS exclusif
        // Si foreground = le jeu bloque tout = exclusif
    }
    public static WinDef.HWND createTestWindow() {
        // Crée une petite fenêtre Swing invisible
        JFrame frame = new JFrame();
        frame.setSize(1, 1);
        frame.setUndecorated(true); // pas de bordure
        frame.setAlwaysOnTop(false); // on gère nous-mêmes
        frame.setVisible(true); // doit être visible pour avoir un HWND

        // Récupère le HWND natif
        WinDef.HWND hwnd = new WinDef.HWND();
        hwnd.setPointer(com.sun.jna.Native.getComponentPointer(frame));

        return hwnd;
    }

    private static WinDef.HWND createDummyWindow() {
        // HWND_MESSAGE = -3 → fenêtre "message only"
        WinDef.HWND hwndMessageParent = new WinDef.HWND(Pointer.createConstant(-3));

        // Crée une "fenêtre fantôme" directement (pas besoin de RegisterClassEx ici)
        WinDef.HWND dummy = USER_32_EXTENDED.INSTANCE.CreateWindowExW(
            0,
            "Message",   // classe système existante
            "DummyWindow",
            WinUser.WS_POPUP,
            0, 0, 1, 1,
            hwndMessageParent, // parent message-only
            null,
            null,
            null);

        if (dummy == null) {
            System.err.println("Impossible de créer la fenêtre message-only");
        }

        // Crée la fenêtre
        return dummy;
    }

    public static boolean isExclusiveFullscreenLight(WinDef.HWND hwnd) {
        // Si pas foreground → pas exclusif
        if (!User32.INSTANCE.GetForegroundWindow().equals(hwnd)) return false;

        // Pas de décorations → mode plein écran
        BaseTSD.LONG_PTR style = User32.INSTANCE.GetWindowLongPtr(hwnd, WinUser.GWL_STYLE);
        if ((style.longValue() & WinUser.WS_OVERLAPPEDWINDOW) != 0) return false;

        return true; // on considère ça "fullscreen" (optimisé)
    }
//    private static boolean isExclusiveFullscreenByStyle(WinDef.HWND hwnd) {
//        BaseTSD.LONG_PTR exStyle = User32.INSTANCE.GetWindowLongPtr(hwnd, WinUser.GWL_EXSTYLE);
//        int error = Kernel32.INSTANCE.GetLastError();
//        if (exStyle.longValue() == 0 && error != 0) {
//            return false; // impossible à lire, on considère non exclusif
//        }
//        return (exStyle.longValue() & WinUser.WS_EX_TOPMOST) != 0;
//    }
}
