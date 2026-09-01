package com.nz.jnawintools.window;

import com.nz.jnawintools.enums.WindowDisplayMode;
import com.nz.jnawintools.win32.Kernel32;
import com.nz.jnawintools.win32.MONITORINFOEXW;
import com.nz.jnawintools.win32.RECT;
import com.nz.jnawintools.win32.User32;
import com.nz.jnawintools.win32.WinUser;
import com.nz.jnawintools.window.result.*;
import com.nz.jnawintools.window.utils.WindowMonitorUtils;

import java.awt.*;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;

/**
 * Window utilities backed by the Java FFM Win32 layer.
 *
 * <p>Opaque {@code HWND}/{@code HMONITOR} handles cross the Java code as primitive {@code long}
 * ({@code 0} = null); they are only converted to a {@link MemorySegment} at the downcall boundary
 * inside {@link User32}.
 */
public class Window64Utils {

    private static final int WS_EX_TOOLWINDOW = 0x00000080;
    private static final int WS_EX_APPWINDOW = 0x00040000;
    private static final int WS_EX_NOACTIVATE = 0x08000000;
    private static final int WS_EX_LAYERED = 0x00080000;
    private static final int LWA_COLORKEY = 0x00000001;
    private static final int WS_EX_NOREDIRECTIONBITMAP = 0x00200000;

    public static boolean isValid(long hwnd) {
        return hwnd != 0L && User32.isWindow(hwnd);
    }

    /**
     * ------------------ HANDLE ET STYLE ------------------
     */

    public static HwndResult getHwnd(String windowName) {
        Kernel32.setLastError(0);
        long hwnd;
        try (Arena arena = Arena.ofConfined()) {
            hwnd = User32.findWindow(arena, windowName);
        }
        if (hwnd != 0L) {
            return HwndResult.success(hwnd);
        }
        return HwndResult.failure(Kernel32.getLastError());
    }

    public static long getHwnd(long windowHandle) {
        return windowHandle;
    }

    public static WinApiResultExtended<String> getName(long hwnd) {
        Kernel32.setLastError(0);
        String title = User32.getWindowText(hwnd);
        int error = Kernel32.getLastError();

        if (title.isEmpty()) {
            if (error != 0) {
                return WinApiResultExtended.failureValue(error);
            }
            return WinApiResultExtended.success(""); // Pas de titre mais pas d'erreur
        }
        return WinApiResultExtended.success(title);
    }

    public static boolean isActive(String windowName) {
        return getHwnd(windowName).isSuccess();
    }

    public static WindowStyleResult getStyle(long hwnd, int index) {
        Kernel32.setLastError(0);
        long styleValue = User32.getWindowLongPtr(hwnd, index);
        int error = Kernel32.getLastError();

        if (styleValue == 0 && error != 0) {
            return WindowStyleResult.failure(error);
        }
        return WindowStyleResult.success(styleValue);
    }

    public static WinApiResult setStyle(long hwnd, int index, long styleValue) {
        Kernel32.setLastError(0);
        User32.setWindowLongPtr(hwnd, index, styleValue);
        int error = Kernel32.getLastError();
        if (error != 0) {
            return WinApiResult.failure(error);
        }
        return WinApiResult.success();
    }

    /**
     * Ajoute et/ou retire des flags au style de la fenetre.
     * Ne modifie rien si le style final est identique.
     */
    public static WinApiResult setStyleIf(long hwnd, int index, long flagsToAdd, long flagsToRemove) {
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

    public static WindowStyleResult getNormalStyle(long hwnd) {
        return getStyle(hwnd, WinUser.GWL_STYLE);
    }

    public static WindowStyleResult getExStyle(long hwnd) {
        return getStyle(hwnd, WinUser.GWL_EXSTYLE);
    }

    // Helpers lisibles
    public static WinApiResult setNormalStyleIf(long hwnd, long flagsToAdd, long flagsToRemove) {
        return setStyleIf(hwnd, WinUser.GWL_STYLE, flagsToAdd, flagsToRemove);
    }

    public static WinApiResult setExStyleIf(long hwnd, long flagsToAdd, long flagsToRemove) {
        return setStyleIf(hwnd, WinUser.GWL_EXSTYLE, flagsToAdd, flagsToRemove);
    }

    // Raccourcis simples pour ajouter/retirer uniquement
    public static WinApiResult addNormalStyle(long hwnd, long flagsToAdd) {
        return setNormalStyleIf(hwnd, flagsToAdd, 0);
    }

    public static WinApiResult addExStyle(long hwnd, long flagsToAdd) {
        return setExStyleIf(hwnd, flagsToAdd, 0);
    }

    public static WinApiResult removeNormalStyle(long hwnd, long flagsToRemove) {
        return setNormalStyleIf(hwnd, 0, flagsToRemove);
    }

    public static WinApiResult removeExStyle(long hwnd, long flagsToRemove) {
        return setExStyleIf(hwnd, 0, flagsToRemove);
    }

    public static WinApiResult setToolWindow(long hwnd, boolean enabled) {
        if (enabled) {
            return addExStyle(hwnd, WS_EX_TOOLWINDOW);
        }
        return removeExStyle(hwnd, WS_EX_TOOLWINDOW);
    }

    public static WinApiResult setAppWindow(long hwnd, boolean enabled) {
        if (enabled) {
            return addExStyle(hwnd, WS_EX_APPWINDOW);
        }
        return removeExStyle(hwnd, WS_EX_APPWINDOW);
    }

    public static WinApiResult setNoRedirectionBitmap(long hwnd, boolean enabled) {
        if (enabled) {
            return addExStyle(hwnd, WS_EX_NOREDIRECTIONBITMAP);
        }
        return removeExStyle(hwnd, WS_EX_NOREDIRECTIONBITMAP);
    }

    /**
     * ------------------ TRANSPARENCE ------------------
     */
    public static WinApiResult enableTransparency(long hwnd) {
        // Etape 1 : ajouter WS_EX_LAYERED si necessaire (dans EXSTYLE, pas STYLE)
        WinApiResult addResult = addExStyle(hwnd, WS_EX_LAYERED);
        if (addResult.isFailure()) {
            return addResult;
        }

        // Etape 2 : appliquer l'opacite totale (255)
        return applyLayeredOpacity(hwnd, (byte) 255);
    }

    /**
     * Applique une opacite a une fenetre ayant WS_EX_LAYERED.
     * alpha = 0 (transparent) a 255 (opaque)
     */
    private static WinApiResult applyLayeredOpacity(long hwnd, byte alpha) {
        Kernel32.setLastError(0);
        boolean ok = User32.setLayeredWindowAttributes(hwnd, 0, alpha, WinUser.LWA_ALPHA);
        int error = Kernel32.getLastError();
        if (!ok && error != 0) {
            return WinApiResult.failure(error);
        }
        return WinApiResult.success();
    }

    /**
     * ------------------ MODES DE FENETRE ------------------
     */

    public static WinApiResult setBorderless(long hwnd) {
        WinApiResultExtended<Integer> screenIndexResult = getScreenIndex(hwnd);
        if (screenIndexResult.isFailure()) {
            return WinApiResult.failure(screenIndexResult.getErrorCode());
        }
        return setBorderlessOnScreen(hwnd, screenIndexResult.getResult());
    }

    public static WinApiResult setBorderlessOnScreen(long hwnd, int screenIndex) {
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

        // S'assurer qu'elle reste dans la barre des taches
        WinApiResult exStyleRes = addExStyle(hwnd, WS_EX_APPWINDOW);
        if (exStyleRes.isFailure()) {
            return exStyleRes;
        }

        return resize(hwnd, bounds, true);
    }

    public static WinApiResult setFullScreen(long hwnd) {
        WinApiResultExtended<Integer> screenIndexResult = getScreenIndex(hwnd);
        if (screenIndexResult.isFailure()) {
            return WinApiResult.failure(screenIndexResult.getErrorCode());
        }
        return setFullScreenOnScreen(hwnd, screenIndexResult.getResult());
    }

    public static WinApiResult setFullScreenOnScreen(long hwnd, int screenIndex) {
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

    /**
     * Decoree (bordures)
     */
    public static WinApiResult setWindowDecorated(long hwnd) {
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

    /**
     * Non decoree (sans bordures ni barre de titre)
     */
    public static WinApiResult setWindowUnDecorated(long hwnd) {
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

    public static WinApiResult showWindow(long hwnd) {
        IconicResult iconic = isIconic(hwnd);
        if (iconic.isFailure()) {
            return WinApiResult.failure(iconic.getErrorCode());
        }
        // ShowWindow renvoie l'etat de visibilite precedent, pas un booleen de reussite :
        // on evalue le succes via GetLastError.
        Kernel32.setLastError(0);
        if (iconic.isIconic()) {
            User32.showWindow(hwnd, WinUser.SW_RESTORE);
        } else {
            User32.showWindow(hwnd, WinUser.SW_SHOW);
        }
        int error = Kernel32.getLastError();
        if (error != 0) {
            return WinApiResult.failure(error);
        }
        return WinApiResult.success();
    }

    public static WinApiResult hideWindow(long hwnd) {
        IconicResult iconic = isIconic(hwnd);
        if (iconic.isFailure()) {
            return WinApiResult.failure(iconic.getErrorCode());
        }
        if (iconic.isIconic()) {
            return WinApiResult.success();
        }
        Kernel32.setLastError(0);
        User32.showWindow(hwnd, WinUser.SW_HIDE);
        int error = Kernel32.getLastError();
        if (error != 0) {
            return WinApiResult.failure(error);
        }
        return WinApiResult.success();
    }

    /**
     * ------------------ AUTRES UTILS ------------------
     */
    private static WinApiResult resize(long hwnd, Rectangle bounds, boolean showWindow) {
        int flags = WinUser.SWP_FRAMECHANGED | WinUser.SWP_NOZORDER;
        if (showWindow) {
            flags |= WinUser.SWP_SHOWWINDOW;
        }
        Kernel32.setLastError(0);
        boolean ok = User32.setWindowPos(
                hwnd,
                0,
                bounds.x, bounds.y,
                bounds.width, bounds.height,
                flags
        );
        if (!ok) {
            int error = Kernel32.getLastError();
            return WinApiResult.failure(error != 0 ? error : 0x71000);
        }
        return WinApiResult.success();
    }

    /**
     * ------------------ INFOS ------------------
     */

    public static WinApiResultExtended<Boolean> isWindowDecorated(long hwnd) {
        WindowStyleResult styleResult = getNormalStyle(hwnd);
        if (styleResult.isFailure()) {
            return WinApiResultExtended.failureValue(styleResult.getErrorCode());
        }
        long style = styleResult.getStyle();
        boolean decorated = (style & WinUser.WS_OVERLAPPEDWINDOW) != 0;
        return WinApiResultExtended.success(decorated);
    }

    public static Rectangle adjustBoundsToScreen(Rectangle bounds, long hwnd) {
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

    /**
     * ------------------ Utils ------------------
     */
    public static WinApiResult setClickThrough(long hwnd) {
        return addExStyle(hwnd, WinUser.WS_EX_LAYERED | WinUser.WS_EX_TRANSPARENT);
    }

    public static WinApiResult setClickThroughReceiver(long hwnd) {
        return removeExStyle(hwnd, WinUser.WS_EX_TRANSPARENT);
    }

    public static WinApiResult setChromaKeyTransparency(long hwnd) {
        // 1. On s'assure d'avoir WS_EX_LAYERED
        WinApiResult addResult = addExStyle(hwnd, WS_EX_LAYERED);
        if (addResult.isFailure()) {
            return addResult;
        }

        // 2. On applique le ColorKey sur le Noir (RGB: 0, 0, 0)
        Kernel32.setLastError(0);
        boolean ok = User32.setLayeredWindowAttributes(hwnd, 0, (byte) 0, LWA_COLORKEY);
        int error = Kernel32.getLastError();

        if (!ok && error != 0) {
            return WinApiResult.failure(error);
        }
        return WinApiResult.success();
    }

    public static WinApiResult makeNoActivate(long hwnd) {
        return addExStyle(hwnd, WS_EX_NOACTIVATE);
    }

    public static WinApiResult setAlwaysOnTop(long hwnd) {
        Kernel32.setLastError(0);
        boolean ok = User32.setWindowPos(
                hwnd,
                WinUser.HWND_TOPMOST,
                0, 0, 0, 0,
                WinUser.SWP_NOMOVE | WinUser.SWP_NOSIZE | WinUser.SWP_NOACTIVATE | WinUser.SWP_SHOWWINDOW
        );

        int error = Kernel32.getLastError();
        if (!ok && error != 0) {
            return WinApiResult.failure(error);
        }
        return WinApiResult.success();
    }

    public static WinApiResult setForegroundWindow(long hwnd) {
        Kernel32.setLastError(0);

        // Recupere le thread de la fenetre active
        long foreground = User32.getForegroundWindow();
        int currentThread = Kernel32.getCurrentThreadId();
        int foregroundThread = User32.getWindowThreadProcessId(foreground, MemorySegment.NULL);

        // Attache les threads pour forcer le focus
        User32.attachThreadInput(foregroundThread, currentThread, true);

        // Montre et met en avant
        User32.bringWindowToTop(hwnd);
        User32.showWindow(hwnd, WinUser.SW_SHOW);
        boolean ok = User32.setForegroundWindow(hwnd);
        int error = Kernel32.getLastError();

        // Detache apres usage
        User32.attachThreadInput(foregroundThread, currentThread, false);

        if (!ok) {
            return WinApiResult.failure(error != 0 ? error : 0x71002);
        }
        return WinApiResult.success();
    }

    public static IconicResult isIconic(long hwnd) {
        Kernel32.setLastError(0);
        boolean iconic = User32.isIconic(hwnd);
        int error = Kernel32.getLastError();

        // Si la fonction echoue (rare, mais possible si hwnd est invalide)
        if (!iconic && error != 0) {
            return IconicResult.failure(error);
        }
        return IconicResult.success(iconic);
    }

    public static WinApiResultExtended<Integer> getScreenIndex(long hwnd) {
        // Trouve le moniteur associe a la fenetre
        Kernel32.setLastError(0);
        long targetMonitor = User32.monitorFromWindow(hwnd, WinUser.MONITOR_DEFAULTTONEAREST);
        if (targetMonitor == 0L) {
            int error = Kernel32.getLastError();
            return WinApiResultExtended.failureValue(error != 0 ? error : 0x91000);
        }

        // Enumere tous les moniteurs pour trouver l'index du moniteur cible
        final List<Long> monitors = new ArrayList<>();
        Kernel32.setLastError(0);
        boolean ok = User32.enumDisplayMonitors(monitor -> {
            monitors.add(monitor);
            return true; // continue enumeration
        });

        if (!ok || monitors.isEmpty()) {
            int error = Kernel32.getLastError();
            return WinApiResultExtended.failureValue(error != 0 ? error : 0x90000);
        }

        // Cherche l'index correspondant
        for (int i = 0; i < monitors.size(); i++) {
            if (monitors.get(i) == targetMonitor) {
                return WinApiResultExtended.success(i);
            }
        }
        return WinApiResultExtended.failureValue(0x92000); // Moniteur non trouve
    }

    public static DisplayModeResult getDisplayMode(long hwnd) {
        // Si la fenetre est invisible
        if (!User32.isWindowVisible(hwnd)) {
            return DisplayModeResult.success(WindowDisplayMode.Windowed);
        }
        // Si la fenetre est minimisee
        IconicResult iconic = isIconic(hwnd);
        if (iconic.isFailure()) {
            return DisplayModeResult.failure(iconic.getErrorCode());
        }
        if (iconic.isIconic()) {
            return DisplayModeResult.success(WindowDisplayMode.Windowed);
        }

        // Obtenir le rectangle de la fenetre
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

        // Verifie le style de la fenetre
        WinApiResultExtended<Boolean> windowDecorated = isWindowDecorated(hwnd);

        boolean hasBorder;
        if (windowDecorated.isSuccess()) {
            hasBorder = windowDecorated.getResult();
        } else {
            return DisplayModeResult.failure(windowDecorated.getErrorCode());
        }
        if (hasBorder) {
            // Elle a encore des bordures, donc c'est une fenetre maximisee
            return DisplayModeResult.success(WindowDisplayMode.Windowed);
        }
        // --- Fullscreen exclusif ? ---
        boolean resolutionChanged = WindowMonitorUtils.hasResolutionChangedForWindow(hwnd);
        boolean dwmDisabled = !WindowMonitorUtils.isDwmCompositionEnabled();

        if (resolutionChanged || dwmDisabled) {
            return DisplayModeResult.success(WindowDisplayMode.Fullscreen);
        }
        boolean exclusive = WindowMonitorUtils.isExclusiveFullscreenLight(hwnd);

        if (exclusive) {
            return DisplayModeResult.success(WindowDisplayMode.Fullscreen);
        }

        // Sinon c'est du Borderless
        return DisplayModeResult.success(WindowDisplayMode.Borderless);
    }

    public static ScreenBoundsResult getScreenBoundsForWindow(long hwnd) {
        // Recupere le moniteur de la fenetre
        Kernel32.setLastError(0);
        long hMonitor = User32.monitorFromWindow(hwnd, WinUser.MONITOR_DEFAULTTONEAREST);
        int error = Kernel32.getLastError();
        if (hMonitor == 0L) {
            return ScreenBoundsResult.failure(error != 0 ? error : 0x30000); // code "no monitor"
        }

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment info = MONITORINFOEXW.allocate(arena);

            Kernel32.setLastError(0);
            boolean ok = User32.getMonitorInfo(hMonitor, info);
            error = Kernel32.getLastError();
            if (!ok) {
                return ScreenBoundsResult.failure(error != 0 ? error : 0x40000); // code "monitor info failed"
            }

            MemorySegment rc = MONITORINFOEXW.rcMonitor(info);
            Rectangle bounds = new Rectangle(
                    RECT.left(rc),
                    RECT.top(rc),
                    RECT.right(rc) - RECT.left(rc),
                    RECT.bottom(rc) - RECT.top(rc)
            );
            return ScreenBoundsResult.success(bounds);
        }
    }

    /***
     * visible means window is in task bar
     * true if minimized
     * true if in background (non visible , other window in front)
     */
    public static VisibleResult isVisible(long hwnd) {
        Kernel32.setLastError(0);
        boolean visible = User32.isWindowVisible(hwnd);
        int error = Kernel32.getLastError();

        // Si l'appel echoue (rare mais possible si hwnd invalide)
        if (!visible && error != 0) {
            return VisibleResult.failure(error);
        }

        return VisibleResult.success(visible);
    }

    public static HwndResult getForegroundWindow() {
        Kernel32.setLastError(0);
        long hwnd = User32.getForegroundWindow();
        int error = Kernel32.getLastError();
        if (hwnd == 0L) {
            // Code 0x10000 = code personnalise "no active window"
            return HwndResult.failure(error != 0 ? error : 0x10000);
        }
        return HwndResult.success(hwnd);
    }

    public static ForeGroundWindowNameResult getForegroundWindowName() {
        long hwnd = getForegroundWindow().getHwnd();

        Kernel32.setLastError(0);
        String title = User32.getWindowText(hwnd);
        int error = Kernel32.getLastError();

        if (title.isEmpty() && error != 0) {
            return ForeGroundWindowNameResult.failure(error);
        }
        return ForeGroundWindowNameResult.success(title);
    }

    public static WinApiScreensBounds getAllScreenBounds() {
        final List<Rectangle> monitors = new ArrayList<>();

        Kernel32.setLastError(0);
        boolean ok;
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment info = MONITORINFOEXW.allocate(arena);
            ok = User32.enumDisplayMonitors(hMonitor -> {
                if (User32.getMonitorInfo(hMonitor, info)) {
                    MemorySegment rc = MONITORINFOEXW.rcMonitor(info);
                    monitors.add(new Rectangle(
                            RECT.left(rc),
                            RECT.top(rc),
                            RECT.right(rc) - RECT.left(rc),
                            RECT.bottom(rc) - RECT.top(rc)
                    ));
                }
                return true; // continuer l'enumeration
            });
        }
        int error = Kernel32.getLastError();

        if (!ok || monitors.isEmpty()) {
            return WinApiScreensBounds.failure(error != 0 ? error : 0x90000);
        }

        return WinApiScreensBounds.success(monitors);
    }

    public static WindowBoundsResult getWindowBounds(long hwnd) {
        Kernel32.setLastError(0);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment rect = RECT.allocate(arena);
            boolean ok = User32.getWindowRect(hwnd, rect);
            int error = Kernel32.getLastError();
            if (!ok && error != 0) {
                return WindowBoundsResult.failure(error);
            }

            int width = RECT.right(rect) - RECT.left(rect);
            int height = RECT.bottom(rect) - RECT.top(rect);

            return WindowBoundsResult.success(new Rectangle(RECT.left(rect), RECT.top(rect), width, height));
        }
    }

    public static ScreenBoundsResult getScreenBounds(int screenIndex) {
        WinApiScreensBounds screenBoundsResult = getAllScreenBounds();

        if (!screenBoundsResult.isSuccess()) {
            return ScreenBoundsResult.failure(screenBoundsResult.getErrorCode());
        }
        List<Rectangle> screensBounds = screenBoundsResult.getResult();
        if (screenIndex < 0 || screenIndex >= screensBounds.size()) {
            return ScreenBoundsResult.failure(0x20000); // code personnalise : index invalide
        }
        return ScreenBoundsResult.success(screensBounds.get(screenIndex));
    }

    public static WinApiResult setWindowPosition(long hwnd, int x, int y) {
        WindowBoundsResult windowBounds = getWindowBounds(hwnd);
        if (windowBounds.isFailure()) {
            return WinApiResult.failure(windowBounds.getErrorCode());
        }
        Rectangle bounds = windowBounds.getResult();
        bounds.x = x;
        bounds.y = y;
        return setWindowPosition(hwnd, bounds);
    }

    public static WinApiResult setWindowPosition(long hwnd, Rectangle posSize) {
        Kernel32.setLastError(0);
        boolean ok = User32.setWindowPos(
                hwnd,
                0,
                posSize.x,
                posSize.y,
                posSize.width,
                posSize.height, WinUser.SWP_NOZORDER | WinUser.SWP_NOACTIVATE
        );
        int error = Kernel32.getLastError();
        if (!ok) {
            return WinApiResult.failure(error != 0 ? error : 0x50000); // code personnalise "SetWindowPos failed"
        }
        return WinApiResult.success();
    }

    public static WinApiResultExtended<Integer> getDpiForWindow(long hwnd) {
        Kernel32.setLastError(0);
        int dpi = User32.getDpiForWindow(hwnd);
        int error = Kernel32.getLastError();

        if (dpi == 0 && error != 0) {
            return WinApiResultExtended.failureValue(error); // echec : renvoie l'erreur
        }

        return WinApiResultExtended.success(dpi); // succes
    }
}
