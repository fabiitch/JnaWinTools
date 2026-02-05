package com.nz.jnawintools.window;

import com.nz.jnawintools.enums.WindowDisplayMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nz.jnawintools.win32.User32Extended;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import java.awt.*;

public class Window32Utils {

    private final static User32Extended USER_32_EXTENDED = User32Extended.INSTANCE;

    public static final Logger logger = LoggerFactory.getLogger(Window32Utils.class);

    public static boolean isActive(String windowName) {
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowName);
        return hwnd != null;
    }

    public static void enableTransparency(String windowName) {
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowName);
        if (hwnd != null) {
            int exStyle = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_EXSTYLE);
            User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_EXSTYLE, exStyle | 0x00080000); // WS_EX_LAYERED
            User32.INSTANCE.SetLayeredWindowAttributes(hwnd, 0, (byte) 255, 2);
        } else {
            logger.error( "enableTransparency window={} not found", windowName);
        }
    }

    public static void setFullScreenBorderLess(String windowName) {
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowName);
        if (hwnd != null) {
            // Retirer bordures (style fenêtré)
            int style = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_STYLE);
            style &= ~WinUser.WS_OVERLAPPEDWINDOW;
            style |= WinUser.WS_POPUP | WinUser.WS_VISIBLE;
            User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_STYLE, style);
            logger.trace( "setFullScreenBorderLess() window={} ok !", windowName);
        } else {
            logger.error( "setFullScreenBorderLess() window={} not found", windowName);
        }
    }

    public static void setClickThrough(String windowName) {
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowName);
        if (hwnd != null) {
            int exStyle = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_EXSTYLE);
            exStyle |= WinUser.WS_EX_LAYERED | WinUser.WS_EX_TRANSPARENT;
            User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_EXSTYLE, exStyle);
            logger.trace( "setClickThrough() window={} ok !", windowName);
        } else {
            logger.error( "setClickThrough() window={} not found", windowName);
        }
    }

    public static void setClickThroughReceiver(String windowName) {
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowName);
        if (hwnd != null) {
            int exStyle = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_EXSTYLE);
            exStyle &= ~WinUser.WS_EX_TRANSPARENT;
            User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_EXSTYLE, exStyle);
            logger.trace( "setClickThroughReceiver() window={} ok !", windowName);
        } else {
            logger.error( "setClickThroughReceiver() window={} not found", windowName);
        }
    }

    public static void setAlwaysOnTop(String windowName) {
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowName);
        if (hwnd != null) {
            User32.INSTANCE.SetWindowPos(hwnd, new WinDef.HWND(new Pointer(-1)),
                    0, 0, 0, 0, WinUser.SWP_NOMOVE | WinUser.SWP_NOSIZE | WinUser.SWP_NOACTIVATE);
//            0, 0, 0, 0, WinUser.SWP_NOMOVE | WinUser.SWP_NOSIZE | WinUser.SWP_SHOWWINDOW);
            logger.trace( "setAlwaysOnTop() window={} ok !", windowName);
        } else {
            logger.error( "setAlwaysOnTop() window={} not found", windowName);
        }
    }


    public static boolean isIconic(String windowName) {
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowName);
        if (hwnd == null) {
            logger.error( "isIconic() window={} not found", windowName);
            return false;
        }
        boolean iconic = USER_32_EXTENDED.IsIconic(hwnd);
        logger.error( "isIconic() window={} iconic={}", windowName, iconic);
        return iconic;
    }

    public static WindowDisplayMode getDisplayMode(String windowName) {
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowName);
        if (hwnd == null) {
            logger.error( "getDisplayMode() window={} not found", windowName);
            return null;
        }
        if (!User32.INSTANCE.IsWindowVisible(hwnd)) return WindowDisplayMode.Windowed;


        WinDef.RECT windowRect = new WinDef.RECT();
        User32.INSTANCE.GetWindowRect(hwnd, windowRect);

        // Taille de la fenêtre
        int windowWidth = windowRect.right - windowRect.left;
        int windowHeight = windowRect.bottom - windowRect.top;

        // Vérifie si elle est en mode minimisé
        if (USER_32_EXTENDED.IsIconic(hwnd)) return WindowDisplayMode.Windowed;

        int screenIndexGdx = getScreenIndexAwt(windowName);
        Rectangle screenBounds = getScreenBoundsAwt(screenIndexGdx);
        if (screenBounds.width == windowWidth && screenBounds.height == windowHeight &&
                screenBounds.x == windowRect.left && screenBounds.y == windowRect.top) {
            // Elle occupe exactement un écran -> peut être borderless ou fullscreen
            // Pour affiner : check s’il y a une bordure ou une barre
            int style = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_STYLE);
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
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowName);
        if (hwnd == null) {
            logger.error( "isVisible() window={} not found", windowName);
            return false;
        }
        boolean visible = User32.INSTANCE.IsWindowVisible(hwnd);
        logger.trace( "isVisible() window={} visible={}", windowName, visible);
        return visible;
    }


    public static String getActiveWindowTitle() {
        char[] buffer = new char[1024];
        WinDef.HWND hwnd = User32.INSTANCE.GetForegroundWindow(); // fenêtre active
        if (hwnd == null) {
            logger.error( "getActiveWindowTitle() found no active window");
            return null;
        }
        User32.INSTANCE.GetWindowText(hwnd, buffer, 1024);
        return Native.toString(buffer);
    }

    public static Rectangle getWindowBounds(String windowName) {
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowName);
        if (hwnd == null) {
            logger.error( "getWindowBounds() window={} not found", windowName);
            return null;
        }

        WinDef.RECT rect = new WinDef.RECT();
        User32.INSTANCE.GetWindowRect(hwnd, rect);

        int width = rect.right - rect.left;
        int height = rect.bottom - rect.top;
        logger.trace( "getWindowBounds() window={} at {}", windowName, rect);
        return new Rectangle(rect.left, rect.top, width, height);
    }

    public static Rectangle getScreenBoundsAwt(int screenIndex) {
        if (screenIndex == -1) {
            logger.trace( "getScreenBoundsAwt called with screenIndex = -1, Bad value!");
            return null;
        }
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();
        Rectangle screenBounds = screens[screenIndex].getDefaultConfiguration().getBounds();
        logger.trace( "getScreenBoundsAwt of screenIndex ={} result ={}", screenIndex, screenBounds);
        return screenBounds;
    }


    public static int getScreenIndexAwt(String windowName) {
        Rectangle windowBounds = getWindowBounds(windowName);
        if (windowBounds == null) {
            logger.error( "getScreenIndexAwt() window={} not found", windowName);
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
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowName);
        if (hwnd == null) {
            logger.error( "setWindowPositions() window={} not found", windowName);
        }
        boolean ok = User32.INSTANCE.SetWindowPos(
                hwnd,
                null,
                posSize.x,
                posSize.y,
                posSize.width,
                posSize.height, WinUser.SWP_NOZORDER | WinUser.SWP_NOACTIVATE
        );

        if (ok) {
            logger.trace( "setWindowPosition() success! window={} at pos={}", windowName, posSize);
        } else {
            logger.error( "setWindowPosition() failed to move window={} at pos={}", windowName, posSize);

        }
    }

    public static int getDpiForWindow(String windowName) {
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowName);
        if (hwnd == null) {
            logger.error( "getDpiForWindow() window={} not found", windowName);
            return -1;
        }
        int dpi = USER_32_EXTENDED.GetDpiForWindow(hwnd);
        logger.trace( "getDpiForWindow() window={} has dpi={}", windowName, dpi);
        return dpi;
    }
}
