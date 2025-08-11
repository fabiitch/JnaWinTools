package com.nz.jnawintools.window;

import com.nz.jnawintools.enums.WindowDisplayMode;
import com.nz.jnawintools.win32.User32Extended;
import com.nz.jnawintools.window.result.*;
import com.nz.jnawintools.window.utils.WindowMonitorUtils;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Window64Utils {

    private final static User32Extended USER_32_EXTENDED = User32Extended.INSTANCE;

    private final static User32 USER_32 = User32.INSTANCE;
    private final static Kernel32 KERNEL_32 = Kernel32.INSTANCE;

    private static final int WS_EX_APPWINDOW = 0x00040000;
    private final static int WS_EX_NOACTIVATE = 0x08000000 ;
    private final static int WS_EX_LAYERED = 0x00080000;

    private static Pointer longToPointer(long value) {
        return new Pointer(value);
    }

    /** ------------------ HANDLE ET STYLE ------------------ */

    public static HwndResult getHwnd(String windowName) {
        KERNEL_32.SetLastError(0);
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowName);
        if (hwnd != null && hwnd.getPointer() != null) {
            return HwndResult.success(hwnd);
        }
        return HwndResult.failure(KERNEL_32.GetLastError());
    }

    public static WinApiResultExtended<String> getName(WinDef.HWND hwnd) {
        char[] buffer = new char[1024];
        // Lecture du titre
        Kernel32.INSTANCE.SetLastError(0);
        int copied = User32.INSTANCE.GetWindowText(hwnd, buffer, buffer.length);
        int error = Kernel32.INSTANCE.GetLastError();

        if (copied == 0) {
            if (error != 0) {
                return WinApiResultExtended.failureValue(error);
            }
            return WinApiResultExtended.success(""); // Pas de titre mais pas d’erreur
        }

        String title = Native.toString(buffer);
        return WinApiResultExtended.success(title);
    }

    public static boolean isActive(String windowName) {
        return getHwnd(windowName).isSuccess();
    }

    public static WindowStyleResult getStyle(WinDef.HWND hwnd, int index) {
        KERNEL_32.SetLastError(0);
        BaseTSD.LONG_PTR stylePtr = USER_32.GetWindowLongPtr(hwnd, index);
        int error = KERNEL_32.GetLastError();
        long styleValue = stylePtr.longValue();

        if (styleValue == 0 && error != 0) {
            return WindowStyleResult.failure(error);
        }
        return WindowStyleResult.success(styleValue);
    }

    public static WinApiResult setStyle(WinDef.HWND hwnd, int index, long styleValue) {
        KERNEL_32.SetLastError(0);
        Pointer result = USER_32.SetWindowLongPtr(hwnd, index, longToPointer(styleValue));
        int error = KERNEL_32.GetLastError();
        if (error != 0) {
            return WinApiResult.failure(error);
        }
        return WinApiResult.success();
    }

    /**
     * Ajoute et/ou retire des flags au style de la fenêtre.
     * Ne modifie rien si le style final est identique.
     */
    public static WinApiResult setStyleIf(WinDef.HWND hwnd, int index, long flagsToAdd, long flagsToRemove) {
        WindowStyleResult currentStyle = getStyle(hwnd, index);
        if (currentStyle.isFailure()) {
            return WinApiResult.failure(currentStyle.getErrorCode());
        }

        long current = currentStyle.getStyle();
        long newStyle = (current | flagsToAdd) & ~flagsToRemove;

        if (newStyle == current) {
            return WinApiResult.success(); // Aucun changement
        }
        return setStyle(hwnd, index, newStyle);
    }

    public static WindowStyleResult getNormalStyle(WinDef.HWND hwnd) {
        return getStyle(hwnd, WinUser.GWL_STYLE);
    }

    public static WindowStyleResult getExStyle(WinDef.HWND hwnd) {
        return getStyle(hwnd, WinUser.GWL_EXSTYLE);
    }

    // Helpers lisibles
    public static WinApiResult setNormalStyleIf(WinDef.HWND hwnd, long flagsToAdd, long flagsToRemove) {
        return setStyleIf(hwnd, WinUser.GWL_STYLE, flagsToAdd, flagsToRemove);
    }

    public static WinApiResult setExStyleIf(WinDef.HWND hwnd, long flagsToAdd, long flagsToRemove) {
        return setStyleIf(hwnd, WinUser.GWL_EXSTYLE, flagsToAdd, flagsToRemove);
    }

    // Raccourcis simples pour ajouter/retirer uniquement
    public static WinApiResult addNormalStyle(WinDef.HWND hwnd, long flagsToAdd) {
        return setNormalStyleIf(hwnd, flagsToAdd, 0);
    }

    public static WinApiResult addExStyle(WinDef.HWND hwnd, long flagsToAdd) {
        return setExStyleIf(hwnd, flagsToAdd, 0);
    }

    public static WinApiResult removeNormalStyle(WinDef.HWND hwnd, long flagsToRemove) {
        return setNormalStyleIf(hwnd, 0, flagsToRemove);
    }

    public static WinApiResult removeExStyle(WinDef.HWND hwnd, long flagsToRemove) {
        return setExStyleIf(hwnd, 0, flagsToRemove);
    }

    /** ------------------ TRANSPARENCE ------------------ */
    public static WinApiResult enableTransparency(WinDef.HWND hwnd) {
        // Étape 1 : ajouter WS_EX_LAYERED si nécessaire (dans EXSTYLE, pas STYLE)
        WinApiResult addResult = addExStyle(hwnd, WS_EX_LAYERED);
        if (addResult.isFailure()) {
            return addResult;
        }

        // Étape 2 : appliquer l'opacité totale (255)
        return applyLayeredOpacity(hwnd, (byte) 255);
    }

    /**
     * Applique une opacité à une fenêtre ayant WS_EX_LAYERED.
     * alpha = 0 (transparent) à 255 (opaque)
     */
    private static WinApiResult applyLayeredOpacity(WinDef.HWND hwnd, byte alpha) {
        KERNEL_32.SetLastError(0);
        boolean ok = USER_32.SetLayeredWindowAttributes(hwnd, 0, alpha, 2); // LWA_ALPHA
        int error = KERNEL_32.GetLastError();
        if (!ok && error != 0) {
            return WinApiResult.failure(error);
        }
        return WinApiResult.success();
    }
    /** ------------------ MODES DE FENÊTRE ------------------ */

    public static WinApiResult setBorderless(WinDef.HWND hwnd) {
        WinApiResultExtended<Integer> screenIndexResult = getScreenIndex(hwnd);
        if (screenIndexResult.isFailure()) {
            return WinApiResult.failure(screenIndexResult.getErrorCode());
        }
        return setBorderlessOnScreen(hwnd, screenIndexResult.getResult());
    }

    public static WinApiResult setBorderlessOnScreen(WinDef.HWND hwnd, int screenIndex) {
        ScreenBoundsResult screenBoundsRes = getScreenBounds(screenIndex);
        if (!screenBoundsRes.isSuccess()) {
            return WinApiResult.failure(screenBoundsRes.getErrorCode());
        }
        Rectangle bounds = screenBoundsRes.getResult();

        // Borderless = supprime WS_OVERLAPPEDWINDOW
        WinApiResult styleRes = setStyleIf(
            hwnd,
            WinUser.GWL_STYLE,
            0,
            WinUser.WS_OVERLAPPEDWINDOW
        );
        if (styleRes.isFailure()) {
            return styleRes;
        }

        // S'assurer qu’elle reste dans la barre des tâches
        WinApiResult exStyleRes = addExStyle(hwnd, WS_EX_APPWINDOW);
        if (exStyleRes.isFailure()) {
            return exStyleRes;
        }

        return resize(hwnd, bounds, true);
    }

    public static WinApiResult setFullScreen(WinDef.HWND hwnd) {
        WinApiResultExtended<Integer> screenIndexResult = getScreenIndex(hwnd);
        if (screenIndexResult.isFailure()) {
            return WinApiResult.failure(screenIndexResult.getErrorCode());
        }
        return setFullScreenOnScreen(hwnd, screenIndexResult.getResult());
    }

    public static WinApiResult setFullScreenOnScreen(WinDef.HWND hwnd, int screenIndex) {
        ScreenBoundsResult screenBoundsRes = getScreenBounds(screenIndex);
        if (!screenBoundsRes.isSuccess()) {
            return WinApiResult.failure(screenBoundsRes.getErrorCode());
        }
        Rectangle bounds = screenBoundsRes.getResult();

        WinApiResult styleRes = setStyleIf(
            hwnd,
            WinUser.GWL_STYLE,
            WinUser.WS_POPUP,   // Fullscreen = WS_POPUP
            WinUser.WS_OVERLAPPEDWINDOW
        );
        if (styleRes.isFailure()) {
            return styleRes;
        }
        return resize(hwnd, bounds, true);
    }

    /** Décorée (bordures) */
    public static WinApiResult setWindowDecorated(WinDef.HWND hwnd) {
        WinApiResultExtended<Boolean> decoratedRes = isWindowDecorated(hwnd);
        if (decoratedRes.isSuccess() && decoratedRes.getResult()) {
            return WinApiResult.success();
        }
        WindowBoundsResult windowBoundsRes = getWindowBounds(hwnd);
        if (windowBoundsRes.isFailure()) {
            return WinApiResult.failure(windowBoundsRes.getErrorCode());
        }
        Rectangle bounds = adjustBoundsToScreen(windowBoundsRes.getResult(), hwnd);
        WinApiResult styleRes = addNormalStyle(hwnd, WinUser.WS_OVERLAPPEDWINDOW);
        if (styleRes.isFailure()) {
            return styleRes;
        }
        return resize(hwnd, bounds, false);
    }

    /** Non décorée (sans bordures ni barre de titre) */
    public static WinApiResult setWindowUnDecorated(WinDef.HWND hwnd) {
        WindowBoundsResult windowBoundsRes = getWindowBounds(hwnd);
        if (windowBoundsRes.isFailure()) {
            return WinApiResult.failure(windowBoundsRes.getErrorCode());
        }
        Rectangle bounds = adjustBoundsToScreen(windowBoundsRes.getResult(), hwnd);
        WinApiResult styleRes = removeNormalStyle(hwnd, WinUser.WS_OVERLAPPEDWINDOW);
        if (styleRes.isFailure()) {
            return styleRes;
        }
        return resize(hwnd, bounds, false);
    }

    /** ------------------ AUTRES UTILS ------------------ */
    private static WinApiResult resize(WinDef.HWND hwnd, Rectangle bounds, boolean showWindow) {
        int flags = WinUser.SWP_FRAMECHANGED | WinUser.SWP_NOZORDER;
        if (showWindow) {
            flags |= WinUser.SWP_SHOWWINDOW;
        }
        KERNEL_32.SetLastError(0);
        boolean ok = USER_32.SetWindowPos(
            hwnd,
            null,
            bounds.x, bounds.y,
            bounds.width, bounds.height,
            flags
        );
        int error = KERNEL_32.GetLastError();
        if (!ok) {
            return WinApiResult.failure(error != 0 ? error : 0x71000);
        }
        return WinApiResult.success();
    }

    /** ------------------ INFOS ------------------ */

    public static WinApiResultExtended<Boolean> isWindowDecorated(WinDef.HWND hwnd) {
        WindowStyleResult styleResult = getNormalStyle(hwnd);
        if (styleResult.isFailure()) {
            return WinApiResultExtended.failureValue(styleResult.getErrorCode());
        }
        long style = styleResult.getStyle();
        boolean decorated = (style & WinUser.WS_OVERLAPPEDWINDOW) != 0;
        return WinApiResultExtended.success(decorated);
    }

    public static Rectangle adjustBoundsToScreen(Rectangle bounds, WinDef.HWND hwnd) {
        WinApiResultExtended<Integer> screenIndexRes = getScreenIndex(hwnd);
        if (!screenIndexRes.isSuccess()) return bounds;
        ScreenBoundsResult screenRes = getScreenBounds(screenIndexRes.getResult());
        if (!screenRes.isSuccess()) return bounds;
        Rectangle screen = screenRes.getResult();

        int x = Math.max(screen.x, bounds.x);
        int y = Math.max(screen.y, bounds.y);
        int width = Math.min(bounds.width, screen.width);
        int height = Math.min(bounds.height, screen.height);
        if (x + width > screen.x + screen.width) x = screen.x + screen.width - width;
        if (y + height > screen.y + screen.height) y = screen.y + screen.height - height;
        return new Rectangle(x, y, width, height);
    }

    /** ------------------ Utils ------------------ */
    public static WinApiResult setClickThrough(WinDef.HWND hwnd) {
        return addExStyle(hwnd, WinUser.WS_EX_LAYERED | WinUser.WS_EX_TRANSPARENT);
    }

    public static WinApiResult setClickThroughReceiver(WinDef.HWND hwnd) {
        return removeExStyle(hwnd, WinUser.WS_EX_TRANSPARENT);
    }

    public static WinApiResult makeNoActivate(WinDef.HWND hwnd) {
        return addExStyle(hwnd, WS_EX_NOACTIVATE);
    }
    public static WinApiResult setAlwaysOnTop(WinDef.HWND hwnd) {
        final WinDef.HWND HWND_TOPMOST = new WinDef.HWND(new Pointer(-1));

        boolean ok = USER_32.SetWindowPos(
            hwnd,
            HWND_TOPMOST,
            0, 0, 0, 0,
            WinUser.SWP_NOMOVE | WinUser.SWP_NOSIZE | WinUser.SWP_NOACTIVATE | WinUser.SWP_SHOWWINDOW
        );

        int error = KERNEL_32.GetLastError();
        if (!ok && error != 0) {
            return WinApiResult.failure(error);
        }
        return WinApiResult.success();
    }

    public static WinApiResult setForegroundWindow(WinDef.HWND hwnd) {
        KERNEL_32.SetLastError(0);

        // Récupère le thread de la fenêtre active
        WinDef.HWND foreground = USER_32.GetForegroundWindow();
        WinDef.DWORD currentThread = new WinDef.DWORD(Kernel32.INSTANCE.GetCurrentThreadId());
        WinDef.DWORD foregroundThread = new WinDef.DWORD(USER_32.GetWindowThreadProcessId(foreground, null));

        // Attache les threads pour forcer le focus
        USER_32.AttachThreadInput(foregroundThread, currentThread, true);

        // Montre et met en avant
        USER_32.BringWindowToTop(hwnd);
        USER_32.ShowWindow(hwnd, WinUser.SW_SHOW);
        boolean ok = USER_32.SetForegroundWindow(hwnd);

        // Détache après usage
        USER_32.AttachThreadInput(foregroundThread, currentThread, false);

        int error = KERNEL_32.GetLastError();
        if (!ok) {
            return WinApiResult.failure(error != 0 ? error : 0x71002);
        }
        return WinApiResult.success();
    }


    public static IconicResult isIconic(WinDef.HWND hwnd) {
        KERNEL_32.SetLastError(0);
        boolean iconic = USER_32_EXTENDED.IsIconic(hwnd);
        int error = KERNEL_32.GetLastError();

        // Si la fonction échoue (rare, mais possible si hwnd est invalide)
        if (!iconic && error != 0) {
            return IconicResult.failure(error);
        }
        return IconicResult.success(iconic);
    }

    public static WinApiResultExtended<Integer> getScreenIndex(WinDef.HWND hwnd) {
        // Trouve le moniteur associé à la fenêtre
        WinUser.HMONITOR targetMonitor = USER_32.MonitorFromWindow(hwnd, WinUser.MONITOR_DEFAULTTONEAREST);
        if (targetMonitor == null || targetMonitor.getPointer() == null) {
            int error = KERNEL_32.GetLastError();
            return WinApiResultExtended.failureValue(error != 0 ? error : 0x91000);
        }

        // Énumère tous les moniteurs pour trouver l’index du moniteur cible
        final List<WinUser.HMONITOR> monitors = new ArrayList<>();
        boolean ok = User32.INSTANCE.EnumDisplayMonitors(
            null, null,
            (hMonitor, hdc, rect, data) -> {
                monitors.add(hMonitor);
                return 1; // continue enumeration
            },
            new WinDef.LPARAM(0)
        ).booleanValue();

        if (!ok || monitors.isEmpty()) {
            int error = KERNEL_32.GetLastError();
            return WinApiResultExtended.failureValue(error != 0 ? error : 0x90000);
        }

        // Cherche l'index correspondant
        for (int i = 0; i < monitors.size(); i++) {
            if (monitors.get(i).getPointer().equals(targetMonitor.getPointer())) {
                return WinApiResultExtended.success(i);
            }
        }
        return WinApiResultExtended.failureValue(0x92000); // Moniteur non trouvé
    }

    public static DisplayModeResult getDisplayMode(WinDef.HWND hwnd) {
        // Si la fenêtre est invisible
        if (!USER_32.IsWindowVisible(hwnd)) {
            return DisplayModeResult.success(WindowDisplayMode.Windowed);
        }
        // Si la fenêtre est minimisée
        IconicResult iconic = isIconic(hwnd);
        if (iconic.isFailure()) {
            return DisplayModeResult.failure(iconic.getErrorCode());
        }
        if (iconic.isIconic()) {
            return DisplayModeResult.success(WindowDisplayMode.Windowed);
        }

        // Obtenir le rectangle de la fenêtre
        WindowBoundsResult windowBounds = getWindowBounds(hwnd);
        if (windowBounds.isFailure()) {
            return DisplayModeResult.failure(windowBounds.getErrorCode());
        }
        Rectangle windowRect = windowBounds.getResult();

        // Screen rect
        ScreenBoundsResult screenBoundsResult = getScreenBoundsForWindow(hwnd);
        if (screenBoundsResult.isFailure()) {
            return DisplayModeResult.failure(screenBoundsResult.getErrorCode());
        }
        Rectangle screenBounds = screenBoundsResult.getResult();

        boolean fullScreen = screenBounds.equals(windowRect);
        if (!fullScreen) {
            return DisplayModeResult.success(WindowDisplayMode.Windowed);
        }

        // Vérifie le style de la fenêtre
        WinApiResultExtended<Boolean> windowDecorated = isWindowDecorated(hwnd);

        boolean hasBorder;
        if (windowDecorated.isSuccess()) {
            hasBorder = windowDecorated.getResult();
        } else {
            return DisplayModeResult.failure(windowDecorated.getErrorCode());
        }
        if (hasBorder) {
            // Elle a encore des bordures, donc c'est une fenêtre maximisée
            return DisplayModeResult.success(WindowDisplayMode.Windowed);
        }
        // --- Fullscreen exclusif ? ---
        boolean resolutionChanged = WindowMonitorUtils.hasResolutionChangedForWindow(hwnd);
        boolean dwmDisabled = !WindowMonitorUtils.isDwmCompositionEnabled();

        if (resolutionChanged || dwmDisabled) {
            return DisplayModeResult.success(WindowDisplayMode.Fullscreen);
        }
        boolean exclusive = WindowMonitorUtils.isExclusiveFullscreenLight(hwnd);
//        boolean exclusive = WindowMonitorUtils.canStayOnTop(hwnd);
        System.out.println("exclusive=" + exclusive);

        if (exclusive) {
            return DisplayModeResult.success(WindowDisplayMode.Fullscreen);
        }

        // Sinon c’est du Borderless
        return DisplayModeResult.success(WindowDisplayMode.Borderless);
    }

    public static ScreenBoundsResult getScreenBoundsForWindow(WinDef.HWND hwnd) {
        // Récupérer le moniteur de la fenêtre
        KERNEL_32.SetLastError(0);
        WinUser.HMONITOR hMonitor = USER_32.MonitorFromWindow(hwnd, WinUser.MONITOR_DEFAULTTONEAREST);
        int error = KERNEL_32.GetLastError();
        if (hMonitor == null || hMonitor.getPointer() == null) {
            return new ScreenBoundsResult(error != 0 ? error : 0x30000); // code "no monitor"
        }

        // Récupérer les infos du moniteur (coords écran)
        User32.MONITORINFOEX info = new User32.MONITORINFOEX();
        info.cbSize = info.size();

        KERNEL_32.SetLastError(0);
        WinDef.BOOL ok = User32.INSTANCE.GetMonitorInfo(hMonitor, info);
        error = KERNEL_32.GetLastError();
        if (ok == null || !ok.booleanValue()) {
            return new ScreenBoundsResult(error != 0 ? error : 0x40000); // code "monitor info failed"
        }

        Rectangle bounds = new Rectangle(
            info.rcMonitor.left,
            info.rcMonitor.top,
            info.rcMonitor.right - info.rcMonitor.left,
            info.rcMonitor.bottom - info.rcMonitor.top
        );
        return new ScreenBoundsResult(bounds);
    }

    /***
     * visible means window is in task bar
     * true if minimized
     * true if in background (non visible , other window in front)
     */
    public static VisibleResult isVisible(WinDef.HWND hwnd) {
        KERNEL_32.SetLastError(0);
        boolean visible = USER_32.IsWindowVisible(hwnd);
        int error = KERNEL_32.GetLastError();

        // Si l'appel échoue (rare mais possible si hwnd invalide)
        if (!visible && error != 0) {
            return VisibleResult.failure(error);
        }

        return VisibleResult.success(visible);
    }

    public static HwndResult getForegroundWindow() {
        KERNEL_32.SetLastError(0);
        WinDef.HWND hwnd = USER_32.GetForegroundWindow();
        int error = KERNEL_32.GetLastError();
        if (hwnd == null || hwnd.getPointer() == null) {
            // Code 0x10000 = code personnalisé "no active window"
            return HwndResult.failure(error != 0 ? error : 0x10000);
        }
        return HwndResult.success(hwnd);
    }

    public static ForeGroundWindowNameResult getForegroundWindowName() {
        char[] buffer = new char[1024];

        // Étape 1 : récupérer la fenêtre active
        HwndResult hwndResult = getForegroundWindow();
        WinDef.HWND hwnd = hwndResult.getHwnd();

        // Étape 2 : lire le titre de la fenêtre
        KERNEL_32.SetLastError(0);
        int copied = USER_32.GetWindowText(hwnd, buffer, 1024);
        int error = KERNEL_32.GetLastError();

        // Vérifier le résultat de GetWindowText
        if (copied == 0) {
            // Peut vouloir dire "pas de texte" OU une erreur
            if (error != 0) {
                return ForeGroundWindowNameResult.failure(error);
            }
        }

        String title = Native.toString(buffer);
        return  ForeGroundWindowNameResult.success(!title.isEmpty() ? title : "");
    }

    public static WinApiScreensBounds getAllScreenBounds() {
        final List<Rectangle> monitors = new ArrayList<>();

        User32.MONITORENUMPROC enumProc = new User32.MONITORENUMPROC() {
            @Override
            public int apply(WinUser.HMONITOR hMonitor, WinDef.HDC hdc, WinDef.RECT rect, WinDef.LPARAM data) {
                User32.MONITORINFOEX info = new User32.MONITORINFOEX();
                info.cbSize = info.size();
                KERNEL_32.SetLastError(0);
                WinDef.BOOL ok = User32.INSTANCE.GetMonitorInfo(hMonitor, info);
                int error = KERNEL_32.GetLastError();

                if (ok != null && ok.booleanValue()) {
                    monitors.add(new Rectangle(
                        info.rcMonitor.left,
                        info.rcMonitor.top,
                        info.rcMonitor.right - info.rcMonitor.left,
                        info.rcMonitor.bottom - info.rcMonitor.top
                    ));
                }
                return 1; // continuer l'énumération
            }
        };

        KERNEL_32.SetLastError(0);
        WinDef.BOOL ok = User32.INSTANCE.EnumDisplayMonitors(null, null, enumProc, new WinDef.LPARAM(0));
        int error = KERNEL_32.GetLastError();

        if (ok == null || !ok.booleanValue() || monitors.isEmpty()) {
            return new WinApiScreensBounds(error != 0 ? error : 0x90000);
        }

        return new WinApiScreensBounds(monitors);
    }

    public static WindowBoundsResult getWindowBounds(WinDef.HWND hwnd) {
        // Lire les coordonnées de la fenêtre
        KERNEL_32.SetLastError(0);
        WinDef.RECT rect = new WinDef.RECT();
        boolean ok = USER_32.GetWindowRect(hwnd, rect);
        int error = KERNEL_32.GetLastError();
        if (!ok && error != 0) {
            return new WindowBoundsResult(error);
        }

        int width = rect.right - rect.left;
        int height = rect.bottom - rect.top;

        return new WindowBoundsResult(new Rectangle(rect.left, rect.top, width, height));
    }

    public static ScreenBoundsResult getScreenBounds(int screenIndex) {
        WinApiScreensBounds screenBoundsResult = getAllScreenBounds();

        if (!screenBoundsResult.isSuccess()) {
            return new ScreenBoundsResult(screenBoundsResult.getErrorCode());
        }
        List<Rectangle> screensBounds = screenBoundsResult.getResult();
        if (screenIndex < 0 || screenIndex >= screensBounds.size()) {
            return new ScreenBoundsResult(0x20000); // code personnalisé : index invalide
        }
        return new ScreenBoundsResult(screensBounds.get(screenIndex));
    }

    public static WinApiResult setWindowPosition(WinDef.HWND hwnd, Rectangle posSize) {
        // Étape 2 : déplacer/redimensionner la fenêtre
        KERNEL_32.SetLastError(0);
        boolean ok = User32.INSTANCE.SetWindowPos(
            hwnd,
            null,
            posSize.x,
            posSize.y,
            posSize.width,
            posSize.height, WinUser.SWP_NOZORDER | WinUser.SWP_NOACTIVATE
        );
        int error = KERNEL_32.GetLastError();
        if (!ok) {
            return WinApiResult.failure(error != 0 ? error : 0x50000); // code personnalisé "SetWindowPos failed"
        }
        return WinApiResult.success();
    }

    public static WinApiResultExtended<Integer> getDpiForWindow(WinDef.HWND hwnd) {

        KERNEL_32.SetLastError(0);
        int dpi = USER_32_EXTENDED.GetDpiForWindow(hwnd);
        int error = KERNEL_32.GetLastError();

        if (dpi == 0 && error != 0) {
            return WinApiResultExtended.failureValue(error); // échec : renvoie l'erreur
        }

        return WinApiResultExtended.success(dpi); // succès
    }

}
