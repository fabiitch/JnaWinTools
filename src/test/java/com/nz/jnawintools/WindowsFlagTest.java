package com.nz.jnawintools;

import com.nz.jnawintools.window.Window64Utils;
import com.nz.jnawintools.window.result.HwndResult;
import com.nz.jnawintools.window.result.WindowStyleResult;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

public class WindowsFlagTest {

    private static final String WINDOW_NAME_PROPERTY = "windowName";
    private static final String WINDOW_NAME_ENV = "WINDOW_NAME";
    private static final String DEFAULT_WINDOW_NAME = "Calculatrice";

    private static final int GCL_STYLE = -26;

    private static final int DWMWA_NCRENDERING_ENABLED = 1;
    private static final int DWMWA_CAPTION_BUTTON_BOUNDS = 5;
    private static final int DWMWA_EXTENDED_FRAME_BOUNDS = 9;
    private static final int DWMWA_HAS_ICONIC_BITMAP = 10;
    private static final int DWMWA_DISALLOW_PEEK = 11;
    private static final int DWMWA_EXCLUDED_FROM_PEEK = 12;
    private static final int DWMWA_CLOAKED = 14;
    private static final int DWMWA_FREEZE_REPRESENTATION = 15;
    private static final int DWMWA_PASSIVE_UPDATE_MODE = 16;
    private static final int DWMWA_FORCE_ICONIC_REPRESENTATION = 7;

    private static final long WS_EX_DLGMODALFRAME = 0x00000001L;
    private static final long WS_EX_NOPARENTNOTIFY = 0x00000004L;
    private static final long WS_EX_TOPMOST = 0x00000008L;
    private static final long WS_EX_ACCEPTFILES = 0x00000010L;
    private static final long WS_EX_TRANSPARENT = 0x00000020L;
    private static final long WS_EX_MDICHILD = 0x00000040L;
    private static final long WS_EX_TOOLWINDOW = 0x00000080L;
    private static final long WS_EX_WINDOWEDGE = 0x00000100L;
    private static final long WS_EX_CLIENTEDGE = 0x00000200L;
    private static final long WS_EX_CONTEXTHELP = 0x00000400L;
    private static final long WS_EX_RIGHT = 0x00001000L;
    private static final long WS_EX_RTLREADING = 0x00002000L;
    private static final long WS_EX_LEFTSCROLLBAR = 0x00004000L;
    private static final long WS_EX_CONTROLPARENT = 0x00010000L;
    private static final long WS_EX_STATICEDGE = 0x00020000L;
    private static final long WS_EX_APPWINDOW = 0x00040000L;
    private static final long WS_EX_LAYERED = 0x00080000L;
    private static final long WS_EX_NOINHERITLAYOUT = 0x00100000L;
    private static final long WS_EX_NOREDIRECTIONBITMAP = 0x00200000L;
    private static final long WS_EX_LAYOUTRTL = 0x00400000L;
    private static final long WS_EX_COMPOSITED = 0x02000000L;
    private static final long WS_EX_NOACTIVATE = 0x08000000L;

    private static final long WS_OVERLAPPED = 0x00000000L;
    private static final long WS_POPUP = 0x80000000L;
    private static final long WS_CHILD = 0x40000000L;
    private static final long WS_MINIMIZE = 0x20000000L;
    private static final long WS_VISIBLE = 0x10000000L;
    private static final long WS_DISABLED = 0x08000000L;
    private static final long WS_CLIPSIBLINGS = 0x04000000L;
    private static final long WS_CLIPCHILDREN = 0x02000000L;
    private static final long WS_MAXIMIZE = 0x01000000L;
    private static final long WS_CAPTION = 0x00C00000L;
    private static final long WS_BORDER = 0x00800000L;
    private static final long WS_DLGFRAME = 0x00400000L;
    private static final long WS_VSCROLL = 0x00200000L;
    private static final long WS_HSCROLL = 0x00100000L;
    private static final long WS_SYSMENU = 0x00080000L;
    private static final long WS_THICKFRAME = 0x00040000L;
    private static final long WS_GROUP = 0x00020000L;
    private static final long WS_TABSTOP = 0x00010000L;
    private static final long WS_MINIMIZEBOX = 0x00020000L;
    private static final long WS_MAXIMIZEBOX = 0x00010000L;
    private static final long WS_OVERLAPPEDWINDOW = WS_CAPTION | WS_SYSMENU | WS_THICKFRAME
            | WS_MINIMIZEBOX | WS_MAXIMIZEBOX;

    private static final long CS_VREDRAW = 0x0001L;
    private static final long CS_HREDRAW = 0x0002L;
    private static final long CS_DBLCLKS = 0x0008L;
    private static final long CS_OWNDC = 0x0020L;
    private static final long CS_CLASSDC = 0x0040L;
    private static final long CS_PARENTDC = 0x0080L;
    private static final long CS_NOCLOSE = 0x0200L;
    private static final long CS_SAVEBITS = 0x0800L;
    private static final long CS_BYTEALIGNCLIENT = 0x1000L;
    private static final long CS_BYTEALIGNWINDOW = 0x2000L;
    private static final long CS_GLOBALCLASS = 0x4000L;
    private static final long CS_DROPSHADOW = 0x00020000L;

    @Test
    public void testOverlay() {
        printWindowFlagsByName("NzCore");
    }

    @Test
    public void testPresentMoon() {
        printWindowFlagsByName("P2C#OVERLAY");
    }


    public void printWindowFlagsByName(String windowName) {
        HwndResult hwndResult = Window64Utils.getHwnd(windowName);
        Assumptions.assumeTrue(hwndResult.isSuccess(),
                "Window not found: " + windowName + ". Use -D" + WINDOW_NAME_PROPERTY
                        + "=\"exact title\" or " + WINDOW_NAME_ENV + "=exact title");

        WindowStyleResult exStyleResult = Window64Utils.getExStyle(hwndResult.getHwnd());
        WindowStyleResult styleResult = Window64Utils.getNormalStyle(hwndResult.getHwnd());

        Assumptions.assumeTrue(exStyleResult.isSuccess(), exStyleResult.getErrorMessage());
        Assumptions.assumeTrue(styleResult.isSuccess(), styleResult.getErrorMessage());

        WinDef.HWND hwndRef = hwndResult.getHwnd();
        long exStyle = exStyleResult.getStyle();
        long style = styleResult.getStyle();
        long classStyle = getClassStyle(hwndRef);
        long hwnd = Pointer.nativeValue(hwndRef.getPointer());

        System.out.println("Window name: " + windowName);
        System.out.println("HWND       : 0x" + Long.toHexString(hwnd).toUpperCase());
        System.out.println();

        printFlags("EXSTYLE", exStyle,
                flag("WS_EX_DLGMODALFRAME", WS_EX_DLGMODALFRAME),
                flag("WS_EX_NOPARENTNOTIFY", WS_EX_NOPARENTNOTIFY),
                flag("WS_EX_TOPMOST", WS_EX_TOPMOST),
                flag("WS_EX_ACCEPTFILES", WS_EX_ACCEPTFILES),
                flag("WS_EX_TRANSPARENT", WS_EX_TRANSPARENT),
                flag("WS_EX_MDICHILD", WS_EX_MDICHILD),
                flag("WS_EX_TOOLWINDOW", WS_EX_TOOLWINDOW),
                flag("WS_EX_WINDOWEDGE", WS_EX_WINDOWEDGE),
                flag("WS_EX_CLIENTEDGE", WS_EX_CLIENTEDGE),
                flag("WS_EX_CONTEXTHELP", WS_EX_CONTEXTHELP),
                flag("WS_EX_RIGHT", WS_EX_RIGHT),
                flag("WS_EX_RTLREADING", WS_EX_RTLREADING),
                flag("WS_EX_LEFTSCROLLBAR", WS_EX_LEFTSCROLLBAR),
                flag("WS_EX_CONTROLPARENT", WS_EX_CONTROLPARENT),
                flag("WS_EX_STATICEDGE", WS_EX_STATICEDGE),
                flag("WS_EX_APPWINDOW", WS_EX_APPWINDOW),
                flag("WS_EX_LAYERED", WS_EX_LAYERED),
                flag("WS_EX_NOINHERITLAYOUT", WS_EX_NOINHERITLAYOUT),
                flag("WS_EX_NOREDIRECTIONBITMAP", WS_EX_NOREDIRECTIONBITMAP),
                flag("WS_EX_LAYOUTRTL", WS_EX_LAYOUTRTL),
                flag("WS_EX_COMPOSITED", WS_EX_COMPOSITED),
                flag("WS_EX_NOACTIVATE", WS_EX_NOACTIVATE));

        printFlags("STYLE", style,
                zeroFlag("WS_OVERLAPPED", WS_OVERLAPPED),
                flag("WS_POPUP", WS_POPUP),
                flag("WS_CHILD", WS_CHILD),
                flag("WS_MINIMIZE", WS_MINIMIZE),
                flag("WS_VISIBLE", WS_VISIBLE),
                flag("WS_DISABLED", WS_DISABLED),
                flag("WS_CLIPSIBLINGS", WS_CLIPSIBLINGS),
                flag("WS_CLIPCHILDREN", WS_CLIPCHILDREN),
                flag("WS_MAXIMIZE", WS_MAXIMIZE),
                flag("WS_CAPTION", WS_CAPTION),
                flag("WS_BORDER", WS_BORDER),
                flag("WS_DLGFRAME", WS_DLGFRAME),
                flag("WS_VSCROLL", WS_VSCROLL),
                flag("WS_HSCROLL", WS_HSCROLL),
                flag("WS_SYSMENU", WS_SYSMENU),
                flag("WS_THICKFRAME", WS_THICKFRAME),
                flag("WS_GROUP / WS_MINIMIZEBOX", WS_GROUP),
                flag("WS_TABSTOP / WS_MAXIMIZEBOX", WS_TABSTOP),
                flag("WS_OVERLAPPEDWINDOW", WS_OVERLAPPEDWINDOW));

        printFlags("CLASS_STYLE", classStyle,
                flag("CS_VREDRAW", CS_VREDRAW),
                flag("CS_HREDRAW", CS_HREDRAW),
                flag("CS_DBLCLKS", CS_DBLCLKS),
                flag("CS_OWNDC", CS_OWNDC),
                flag("CS_CLASSDC", CS_CLASSDC),
                flag("CS_PARENTDC", CS_PARENTDC),
                flag("CS_NOCLOSE", CS_NOCLOSE),
                flag("CS_SAVEBITS", CS_SAVEBITS),
                flag("CS_BYTEALIGNCLIENT", CS_BYTEALIGNCLIENT),
                flag("CS_BYTEALIGNWINDOW", CS_BYTEALIGNWINDOW),
                flag("CS_GLOBALCLASS", CS_GLOBALCLASS),
                flag("CS_DROPSHADOW", CS_DROPSHADOW));

        printDwmAttributes(hwndRef);
    }

    private static Flag flag(String name, long value) {
        return new Flag(name, value, false);
    }

    private static Flag zeroFlag(String name, long value) {
        return new Flag(name, value, true);
    }

    private static void printFlags(String label, long value, Flag... flags) {
        System.out.println(label + " raw : 0x" + Long.toHexString(value).toUpperCase());
        for (Flag flag : flags) {
            System.out.printf("%-34s %5s (0x%08X)%n", flag.name, hasFlag(value, flag), flag.value);
        }
        System.out.println();
    }

    private static long getClassStyle(WinDef.HWND hwnd) {
        try {
            return User32Flag.INSTANCE.GetClassLongPtrW(hwnd, GCL_STYLE).longValue();
        } catch (UnsatisfiedLinkError ignored) {
            return Integer.toUnsignedLong(User32Flag.INSTANCE.GetClassLongW(hwnd, GCL_STYLE));
        }
    }

    private static void printDwmAttributes(WinDef.HWND hwnd) {
        System.out.println("DWM");
        printDwmInt(hwnd, "DWMWA_NCRENDERING_ENABLED", DWMWA_NCRENDERING_ENABLED);
        printDwmInt(hwnd, "DWMWA_FORCE_ICONIC_REPRESENTATION", DWMWA_FORCE_ICONIC_REPRESENTATION);
        printDwmRect(hwnd, "DWMWA_EXTENDED_FRAME_BOUNDS", DWMWA_EXTENDED_FRAME_BOUNDS);
        printDwmRect(hwnd, "DWMWA_CAPTION_BUTTON_BOUNDS", DWMWA_CAPTION_BUTTON_BOUNDS);
        printDwmInt(hwnd, "DWMWA_HAS_ICONIC_BITMAP", DWMWA_HAS_ICONIC_BITMAP);
        printDwmInt(hwnd, "DWMWA_DISALLOW_PEEK", DWMWA_DISALLOW_PEEK);
        printDwmInt(hwnd, "DWMWA_EXCLUDED_FROM_PEEK", DWMWA_EXCLUDED_FROM_PEEK);
        printDwmInt(hwnd, "DWMWA_CLOAKED", DWMWA_CLOAKED);
        printDwmInt(hwnd, "DWMWA_FREEZE_REPRESENTATION", DWMWA_FREEZE_REPRESENTATION);
        printDwmInt(hwnd, "DWMWA_PASSIVE_UPDATE_MODE", DWMWA_PASSIVE_UPDATE_MODE);
        System.out.println();
    }

    private static void printDwmInt(WinDef.HWND hwnd, String name, int attribute) {
        WinDef.DWORDByReference value = new WinDef.DWORDByReference();
        int result = DwmapiFlag.INSTANCE.DwmGetWindowAttribute(hwnd, attribute, value.getPointer(), 4);
        if (result == 0) {
            System.out.printf("%-34s %5d%n", name, value.getValue().intValue());
        } else {
            System.out.printf("%-34s error=0x%08X%n", name, result);
        }
    }

    private static void printDwmRect(WinDef.HWND hwnd, String name, int attribute) {
        WinDef.RECT rect = new WinDef.RECT();
        int result = DwmapiFlag.INSTANCE.DwmGetWindowAttribute(hwnd, attribute, rect.getPointer(), rect.size());
        if (result == 0) {
            rect.read();
            System.out.printf("%-34s left=%d top=%d right=%d bottom=%d%n",
                    name, rect.left, rect.top, rect.right, rect.bottom);
        } else {
            System.out.printf("%-34s error=0x%08X%n", name, result);
        }
    }

    private static boolean hasFlag(long value, Flag flag) {
        if (flag.zeroValue) {
            return value == 0;
        }
        return (value & flag.value) == flag.value;
    }

    private static class Flag {
        private final String name;
        private final long value;
        private final boolean zeroValue;

        private Flag(String name, long value, boolean zeroValue) {
            this.name = name;
            this.value = value;
            this.zeroValue = zeroValue;
        }
    }

    private interface User32Flag extends StdCallLibrary {
        User32Flag INSTANCE = Native.load("user32", User32Flag.class);

        BaseTSD.ULONG_PTR GetClassLongPtrW(WinDef.HWND hWnd, int nIndex);

        int GetClassLongW(WinDef.HWND hWnd, int nIndex);
    }

    private interface DwmapiFlag extends StdCallLibrary {
        DwmapiFlag INSTANCE = Native.load("dwmapi", DwmapiFlag.class);

        int DwmGetWindowAttribute(WinDef.HWND hwnd, int dwAttribute, Pointer pvAttribute, int cbAttribute);
    }
}