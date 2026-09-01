package com.nz.jnawintools.win32;

/**
 * Win32 window/message constants (subset used by this project), mirroring winuser.h.
 *
 * <p>Window style flags are declared as {@code long} on purpose: they are combined with
 * {@code LONG_PTR} style values read from {@code GetWindowLongPtrW}, and declaring
 * {@code WS_POPUP = 0x80000000} as an {@code int} would sign-extend to the high 32 bits when
 * widened to {@code long}.
 */
public final class WinUser {

    private WinUser() {
    }

    // GetWindowLongPtr / SetWindowLongPtr indices
    public static final int GWL_STYLE = -16;
    public static final int GWL_EXSTYLE = -20;

    // GetClassLongPtr index
    public static final int GCL_STYLE = -26;

    // Window styles (WS_*)
    public static final long WS_OVERLAPPED = 0x00000000L;
    public static final long WS_POPUP = 0x80000000L;
    public static final long WS_VISIBLE = 0x10000000L;
    public static final long WS_CAPTION = 0x00C00000L;
    public static final long WS_SYSMENU = 0x00080000L;
    public static final long WS_THICKFRAME = 0x00040000L;
    public static final long WS_MINIMIZEBOX = 0x00020000L;
    public static final long WS_MAXIMIZEBOX = 0x00010000L;
    public static final long WS_OVERLAPPEDWINDOW =
            WS_CAPTION | WS_SYSMENU | WS_THICKFRAME | WS_MINIMIZEBOX | WS_MAXIMIZEBOX;

    // Extended window styles (WS_EX_*)
    public static final long WS_EX_TOOLWINDOW = 0x00000080L;
    public static final long WS_EX_APPWINDOW = 0x00040000L;
    public static final long WS_EX_NOACTIVATE = 0x08000000L;
    public static final long WS_EX_LAYERED = 0x00080000L;
    public static final long WS_EX_TRANSPARENT = 0x00000020L;
    public static final long WS_EX_NOREDIRECTIONBITMAP = 0x00200000L;

    // ShowWindow commands
    public static final int SW_HIDE = 0;
    public static final int SW_SHOW = 5;
    public static final int SW_RESTORE = 9;

    // SetWindowPos flags
    public static final int SWP_NOSIZE = 0x0001;
    public static final int SWP_NOMOVE = 0x0002;
    public static final int SWP_NOZORDER = 0x0004;
    public static final int SWP_NOACTIVATE = 0x0010;
    public static final int SWP_FRAMECHANGED = 0x0020;
    public static final int SWP_SHOWWINDOW = 0x0040;

    // Layered window attribute flags
    public static final int LWA_COLORKEY = 0x0001;
    public static final int LWA_ALPHA = 0x0002;

    // MonitorFromWindow flags
    public static final int MONITOR_DEFAULTTONULL = 0x00000000;
    public static final int MONITOR_DEFAULTTOPRIMARY = 0x00000001;
    public static final int MONITOR_DEFAULTTONEAREST = 0x00000002;

    // EnumDisplaySettings mode numbers
    public static final int ENUM_CURRENT_SETTINGS = -1;
    public static final int ENUM_REGISTRY_SETTINGS = -2;

    // Messages
    public static final int WM_CLOSE = 0x0010;
    public static final int WM_QUIT = 0x0012;
    public static final int PM_NOREMOVE = 0x0000;

    // Special HWND values
    public static final long HWND_TOPMOST = -1L;
    public static final long HWND_MESSAGE = -3L;
}
