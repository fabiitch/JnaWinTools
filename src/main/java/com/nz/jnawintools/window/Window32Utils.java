package com.nz.jnawintools.window;

import com.nz.jnawintools.enums.WindowDisplayMode;
import com.nz.jnawintools.win32.User32;
import com.nz.jnawintools.win32.WinUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.foreign.Arena;

/**
 * Legacy convenience helpers kept for compatibility. Style access uses the x64
 * {@code GetWindowLongPtrW}/{@code SetWindowLongPtrW} entry points (valid on Windows/JVM x64);
 * window styles occupy the low 32 bits of the {@code LONG_PTR} value.
 */
public class Window32Utils {

    public static final Logger logger = LoggerFactory.getLogger(Window32Utils.class);

    private static long findWindow(String windowName) {
        try (Arena arena = Arena.ofConfined()) {
            return User32.findWindow(arena, windowName);
        }
    }

    public static boolean isActive(String windowName) {
        return findWindow(windowName) != 0L;
    }

    public static void enableTransparency(String windowName) {
        long hwnd = findWindow(windowName);
        if (hwnd != 0L) {
            long exStyle = User32.getWindowLongPtr(hwnd, WinUser.GWL_EXSTYLE);
            User32.setWindowLongPtr(hwnd, WinUser.GWL_EXSTYLE, exStyle | WinUser.WS_EX_LAYERED);
            User32.setLayeredWindowAttributes(hwnd, 0, (byte) 255, WinUser.LWA_ALPHA);
        } else {
            logger.error("enableTransparency window={} not found", windowName);
        }
    }

    public static void setFullScreenBorderLess(String windowName) {
        long hwnd = findWindow(windowName);
        if (hwnd != 0L) {
            // Retirer bordures (style fenetre)
            long style = User32.getWindowLongPtr(hwnd, WinUser.GWL_STYLE);
            style &= ~WinUser.WS_OVERLAPPEDWINDOW;
            style |= WinUser.WS_POPUP | WinUser.WS_VISIBLE;
            User32.setWindowLongPtr(hwnd, WinUser.GWL_STYLE, style);
            logger.trace("setFullScreenBorderLess() window={} ok !", windowName);
        } else {
            logger.error("setFullScreenBorderLess() window={} not found", windowName);
        }
    }

    public static void setClickThrough(String windowName) {
        long hwnd = findWindow(windowName);
        if (hwnd != 0L) {
            long exStyle = User32.getWindowLongPtr(hwnd, WinUser.GWL_EXSTYLE);
            exStyle |= WinUser.WS_EX_LAYERED | WinUser.WS_EX_TRANSPARENT;
            User32.setWindowLongPtr(hwnd, WinUser.GWL_EXSTYLE, exStyle);
            logger.trace("setClickThrough() window={} ok !", windowName);
        } else {
            logger.error("setClickThrough() window={} not found", windowName);
        }
    }

    public static void setClickThroughReceiver(String windowName) {
        long hwnd = findWindow(windowName);
        if (hwnd != 0L) {
            long exStyle = User32.getWindowLongPtr(hwnd, WinUser.GWL_EXSTYLE);
            exStyle &= ~WinUser.WS_EX_TRANSPARENT;
            User32.setWindowLongPtr(hwnd, WinUser.GWL_EXSTYLE, exStyle);
            logger.trace("setClickThroughReceiver() window={} ok !", windowName);
        } else {
            logger.error("setClickThroughReceiver() window={} not found", windowName);
        }
    }

    public static void setAlwaysOnTop(String windowName) {
        long hwnd = findWindow(windowName);
        if (hwnd != 0L) {
            User32.setWindowPos(hwnd, WinUser.HWND_TOPMOST,
                    0, 0, 0, 0, WinUser.SWP_NOMOVE | WinUser.SWP_NOSIZE | WinUser.SWP_NOACTIVATE);
            logger.trace("setAlwaysOnTop() window={} ok !", windowName);
        } else {
            logger.error("setAlwaysOnTop() window={} not found", windowName);
        }
    }

    public static boolean isIconic(String windowName) {
        long hwnd = findWindow(windowName);
        if (hwnd == 0L) {
            logger.error("isIconic() window={} not found", windowName);
            return false;
        }
        boolean iconic = User32.isIconic(hwnd);
        logger.error("isIconic() window={} iconic={}", windowName, iconic);
        return iconic;
    }

    public static WindowDisplayMode getDisplayMode(String windowName) {
        long hwnd = findWindow(windowName);
        if (hwnd == 0L) {
            logger.error("getDisplayMode() window={} not found", windowName);
            return null;
        }
        if (!User32.isWindowVisible(hwnd)) return WindowDisplayMode.Windowed;

        Rectangle windowRect = getWindowBounds(windowName);
        if (windowRect == null) {
            return WindowDisplayMode.Windowed;
        }

        // Verifie si elle est en mode minimise
        if (User32.isIconic(hwnd)) return WindowDisplayMode.Windowed;

        int screenIndexGdx = getScreenIndexAwt(windowName);
        Rectangle screenBounds = getScreenBoundsAwt(screenIndexGdx);
        if (screenBounds != null
                && screenBounds.width == windowRect.width && screenBounds.height == windowRect.height
                && screenBounds.x == windowRect.x && screenBounds.y == windowRect.y) {
            // Elle occupe exactement un ecran -> peut etre borderless ou fullscreen
            long style = User32.getWindowLongPtr(hwnd, WinUser.GWL_STYLE);
            boolean hasBorder = (style & WinUser.WS_OVERLAPPEDWINDOW) != 0;

            return hasBorder ? WindowDisplayMode.Borderless : WindowDisplayMode.Fullscreen;
        }
        return WindowDisplayMode.Windowed;
    }

    /***
     * visible means window is in task bar
     * true if minimized
     * true if in background (non visible , other window in front)
     */
    public static boolean isVisible(String windowName) {
        long hwnd = findWindow(windowName);
        if (hwnd == 0L) {
            logger.error("isVisible() window={} not found", windowName);
            return false;
        }
        boolean visible = User32.isWindowVisible(hwnd);
        logger.trace("isVisible() window={} visible={}", windowName, visible);
        return visible;
    }

    public static String getActiveWindowTitle() {
        long hwnd = User32.getForegroundWindow(); // fenetre active
        if (hwnd == 0L) {
            logger.error("getActiveWindowTitle() found no active window");
            return null;
        }
        return User32.getWindowText(hwnd);
    }

    public static Rectangle getWindowBounds(String windowName) {
        long hwnd = findWindow(windowName);
        if (hwnd == 0L) {
            logger.error("getWindowBounds() window={} not found", windowName);
            return null;
        }

        var res = Window64Utils.getWindowBounds(hwnd);
        Rectangle bounds = res.isSuccess() ? res.getResult() : null;
        logger.trace("getWindowBounds() window={} at {}", windowName, bounds);
        return bounds;
    }

    public static Rectangle getScreenBoundsAwt(int screenIndex) {
        if (screenIndex == -1) {
            logger.trace("getScreenBoundsAwt called with screenIndex = -1, Bad value!");
            return null;
        }
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();
        Rectangle screenBounds = screens[screenIndex].getDefaultConfiguration().getBounds();
        logger.trace("getScreenBoundsAwt of screenIndex ={} result ={}", screenIndex, screenBounds);
        return screenBounds;
    }

    public static int getScreenIndexAwt(String windowName) {
        Rectangle windowBounds = getWindowBounds(windowName);
        if (windowBounds == null) {
            logger.error("getScreenIndexAwt() window={} not found", windowName);
            return 0;
        }

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();

        int bestIndex = -1;
        int maxIntersectionArea = -1;
        Rectangle bestBounds = null;
        for (int i = 0; i < screens.length; i++) {
            Rectangle screenBounds = screens[i].getDefaultConfiguration().getBounds();
            Rectangle intersection = screenBounds.intersection(windowBounds);

            int area = intersection.width * intersection.height;
            if (area > maxIntersectionArea) {
                maxIntersectionArea = area;
                bestIndex = i;
                bestBounds = screenBounds;
            }
        }
        if (bestIndex >= 0) {
            logger.trace(
                    "getScreenIndexAwt() window={} best match = screen index {} with size={}/{}, pos={}/{}",
                    windowName, bestIndex, bestBounds.width, bestBounds.height, bestBounds.x, bestBounds.y);
            return bestIndex;
        } else {
            logger.error(
                    "getScreenIndexAwt() no intersection found for window={} on rect={}",
                    windowName, windowBounds);
            return -1;
        }
    }

    public static void setWindowPosition(String windowName, Rectangle posSize) {
        long hwnd = findWindow(windowName);
        if (hwnd == 0L) {
            logger.error("setWindowPositions() window={} not found", windowName);
            return;
        }
        boolean ok = User32.setWindowPos(
                hwnd,
                0,
                posSize.x,
                posSize.y,
                posSize.width,
                posSize.height, WinUser.SWP_NOZORDER | WinUser.SWP_NOACTIVATE
        );

        if (ok) {
            logger.trace("setWindowPosition() success! window={} at pos={}", windowName, posSize);
        } else {
            logger.error("setWindowPosition() failed to move window={} at pos={}", windowName, posSize);
        }
    }

    public static int getDpiForWindow(String windowName) {
        long hwnd = findWindow(windowName);
        if (hwnd == 0L) {
            logger.error("getDpiForWindow() window={} not found", windowName);
            return -1;
        }
        int dpi = User32.getDpiForWindow(hwnd);
        logger.trace("getDpiForWindow() window={} has dpi={}", windowName, dpi);
        return dpi;
    }
}
