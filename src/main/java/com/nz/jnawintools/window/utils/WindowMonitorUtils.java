package com.nz.jnawintools.window.utils;

import com.nz.jnawintools.win32.DEVMODEW;
import com.nz.jnawintools.win32.Dwmapi;
import com.nz.jnawintools.win32.Foreign;
import com.nz.jnawintools.win32.MONITORINFOEXW;
import com.nz.jnawintools.win32.User32;
import com.nz.jnawintools.win32.WinUser;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.JAVA_INT;

public class WindowMonitorUtils {

    public static boolean isDwmCompositionEnabled() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment pfEnabled = arena.allocate(JAVA_INT);
            int hr = Dwmapi.dwmIsCompositionEnabled(pfEnabled);
            if (hr == 0) {
                return pfEnabled.get(JAVA_INT, 0) != 0;
            }
            return false; // En cas d'erreur, on considere DWM desactive
        }
    }

    public static boolean hasResolutionChangedForWindow(long hwnd) {
        long hMonitor = User32.monitorFromWindow(hwnd, WinUser.MONITOR_DEFAULTTONEAREST);
        if (hMonitor == 0L) {
            return false; // impossible de detecter
        }
        return hasResolutionChangedForMonitor(hMonitor);
    }

    public static boolean hasResolutionChangedForMonitor(long hMonitor) {
        try (Arena arena = Arena.ofConfined()) {
            // Recupere le nom du display (\\.\DISPLAYx)
            MemorySegment info = MONITORINFOEXW.allocate(arena);
            if (!User32.getMonitorInfo(hMonitor, info)) {
                return false;
            }
            String displayName = MONITORINFOEXW.device(info);

            // Prepare les structures DEVMODE
            MemorySegment desktopMode = DEVMODEW.allocate(arena);
            MemorySegment currentMode = DEVMODEW.allocate(arena);

            // Charge les resolutions
            boolean gotDesktop = User32.enumDisplaySettings(arena, displayName,
                    WinUser.ENUM_REGISTRY_SETTINGS, desktopMode);
            boolean gotCurrent = User32.enumDisplaySettings(arena, displayName,
                    WinUser.ENUM_CURRENT_SETTINGS, currentMode);
            if (!gotDesktop || !gotCurrent) {
                return false;
            }

            return DEVMODEW.pelsWidth(currentMode) != DEVMODEW.pelsWidth(desktopMode)
                    || DEVMODEW.pelsHeight(currentMode) != DEVMODEW.pelsHeight(desktopMode)
                    || DEVMODEW.displayFrequency(currentMode) != DEVMODEW.displayFrequency(desktopMode);
        }
    }

    public static boolean canStayOnTop(long gameHwnd) {
        // Cree une petite fenetre
        long dummy = createTestWindow();

        if (dummy == 0L) {
            return false; // Impossible de creer
        }

        // Tente de la mettre en TOPMOST
        User32.setWindowPos(
                dummy,
                WinUser.HWND_TOPMOST,
                0, 0, 0, 0,
                WinUser.SWP_NOMOVE | WinUser.SWP_NOSIZE | WinUser.SWP_NOACTIVATE | WinUser.SWP_SHOWWINDOW
        );

        // Verifie si le jeu reste au premier plan
        long foreground = User32.getForegroundWindow();

        // Detruit la fenetre apres test
        User32.destroyWindow(dummy);

        // Si NOT foreground = notre fenetre a reussi a passer par-dessus, donc PAS exclusif
        // Si foreground = le jeu bloque tout = exclusif
        return foreground != gameHwnd;
    }

    /**
     * Cree une petite fenetre native via {@code CreateWindowExW} en s'appuyant sur la classe
     * systeme predefinie {@code STATIC} (aucune reflexion sun.awt, aucun composant Swing).
     */
    public static long createTestWindow() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment className = Foreign.wide(arena, "STATIC");
            MemorySegment windowName = Foreign.wide(arena, "JnaWinToolsProbe");
            return User32.createWindowEx(
                    0,
                    className,
                    windowName,
                    (int) WinUser.WS_POPUP,
                    0, 0, 1, 1,
                    0, 0, 0,
                    MemorySegment.NULL);
        }
    }

    public static boolean isExclusiveFullscreenLight(long hwnd) {
        // Si pas foreground -> pas exclusif
        if (User32.getForegroundWindow() != hwnd) {
            return false;
        }

        // Pas de decorations -> mode plein ecran
        long style = User32.getWindowLongPtr(hwnd, WinUser.GWL_STYLE);
        // on considere ca "fullscreen" (optimise)
        return (style & WinUser.WS_OVERLAPPEDWINDOW) == 0;
    }
}
