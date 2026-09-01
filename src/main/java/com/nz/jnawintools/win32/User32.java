package com.nz.jnawintools.win32;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_CHAR;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

/**
 * user32.dll bindings (FFM). Opaque handles (HWND/HMONITOR/HWINEVENTHOOK) are exchanged with the
 * rest of the code base as {@code long}, and only converted to a boundary {@link MemorySegment}
 * right before a downcall.
 *
 * <p>All {@link MethodHandle}s and {@link FunctionDescriptor}s are resolved a single time into
 * {@code static final} fields; nothing is looked up on a hot path.
 */
public final class User32 {

    private static final SymbolLookup LOOKUP = Foreign.library("user32");

    private static final MethodHandle FIND_WINDOW_W = Foreign.downcallCapturingLastError(
            LOOKUP, "FindWindowW", FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS));
    private static final MethodHandle GET_WINDOW_LONG_PTR_W = Foreign.downcallCapturingLastError(
            LOOKUP, "GetWindowLongPtrW", FunctionDescriptor.of(JAVA_LONG, ADDRESS, JAVA_INT));
    private static final MethodHandle SET_WINDOW_LONG_PTR_W = Foreign.downcallCapturingLastError(
            LOOKUP, "SetWindowLongPtrW", FunctionDescriptor.of(JAVA_LONG, ADDRESS, JAVA_INT, JAVA_LONG));
    private static final MethodHandle GET_CLASS_LONG_PTR_W = Foreign.downcallCapturingLastError(
            LOOKUP, "GetClassLongPtrW", FunctionDescriptor.of(JAVA_LONG, ADDRESS, JAVA_INT));
    private static final MethodHandle SET_LAYERED_WINDOW_ATTRIBUTES = Foreign.downcallCapturingLastError(
            LOOKUP, "SetLayeredWindowAttributes",
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_BYTE, JAVA_INT));
    private static final MethodHandle SET_WINDOW_POS = Foreign.downcallCapturingLastError(
            LOOKUP, "SetWindowPos",
            FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT));
    private static final MethodHandle IS_WINDOW_VISIBLE = Foreign.downcallCapturingLastError(
            LOOKUP, "IsWindowVisible", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    private static final MethodHandle GET_WINDOW_RECT = Foreign.downcallCapturingLastError(
            LOOKUP, "GetWindowRect", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS));
    private static final MethodHandle GET_WINDOW_TEXT_LENGTH_W = Foreign.downcall(
            LOOKUP, "GetWindowTextLengthW", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    private static final MethodHandle GET_WINDOW_TEXT_W = Foreign.downcallCapturingLastError(
            LOOKUP, "GetWindowTextW", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT));
    private static final MethodHandle GET_FOREGROUND_WINDOW = Foreign.downcallCapturingLastError(
            LOOKUP, "GetForegroundWindow", FunctionDescriptor.of(ADDRESS));
    private static final MethodHandle SET_FOREGROUND_WINDOW = Foreign.downcallCapturingLastError(
            LOOKUP, "SetForegroundWindow", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    private static final MethodHandle BRING_WINDOW_TO_TOP = Foreign.downcallCapturingLastError(
            LOOKUP, "BringWindowToTop", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    private static final MethodHandle ATTACH_THREAD_INPUT = Foreign.downcallCapturingLastError(
            LOOKUP, "AttachThreadInput", FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT));
    private static final MethodHandle GET_WINDOW_THREAD_PROCESS_ID = Foreign.downcallCapturingLastError(
            LOOKUP, "GetWindowThreadProcessId", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS));
    private static final MethodHandle SHOW_WINDOW = Foreign.downcallCapturingLastError(
            LOOKUP, "ShowWindow", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT));
    private static final MethodHandle MONITOR_FROM_WINDOW = Foreign.downcallCapturingLastError(
            LOOKUP, "MonitorFromWindow", FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_INT));
    private static final MethodHandle ENUM_DISPLAY_MONITORS = Foreign.downcallCapturingLastError(
            LOOKUP, "EnumDisplayMonitors", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS, JAVA_LONG));
    private static final MethodHandle GET_MONITOR_INFO_W = Foreign.downcallCapturingLastError(
            LOOKUP, "GetMonitorInfoW", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS));
    private static final MethodHandle IS_WINDOW = Foreign.downcall(
            LOOKUP, "IsWindow", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    private static final MethodHandle IS_ICONIC = Foreign.downcallCapturingLastError(
            LOOKUP, "IsIconic", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    private static final MethodHandle GET_DPI_FOR_WINDOW = Foreign.downcallCapturingLastError(
            LOOKUP, "GetDpiForWindow", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    private static final MethodHandle ENUM_DISPLAY_SETTINGS_W = Foreign.downcall(
            LOOKUP, "EnumDisplaySettingsW", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS));
    private static final MethodHandle CREATE_WINDOW_EX_W = Foreign.downcallCapturingLastError(
            LOOKUP, "CreateWindowExW", FunctionDescriptor.of(ADDRESS,
                    JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT,
                    ADDRESS, ADDRESS, ADDRESS, ADDRESS));
    private static final MethodHandle DESTROY_WINDOW = Foreign.downcall(
            LOOKUP, "DestroyWindow", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    private static final MethodHandle SCREEN_TO_CLIENT = Foreign.downcall(
            LOOKUP, "ScreenToClient", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS));
    private static final MethodHandle CLIENT_TO_SCREEN = Foreign.downcall(
            LOOKUP, "ClientToScreen", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS));
    private static final MethodHandle GET_CLIENT_RECT = Foreign.downcall(
            LOOKUP, "GetClientRect", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS));
    private static final MethodHandle SET_WIN_EVENT_HOOK = Foreign.downcallCapturingLastError(
            LOOKUP, "SetWinEventHook", FunctionDescriptor.of(ADDRESS,
                    JAVA_INT, JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT));
    private static final MethodHandle UNHOOK_WIN_EVENT = Foreign.downcallCapturingLastError(
            LOOKUP, "UnhookWinEvent", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    private static final MethodHandle GET_MESSAGE_W = Foreign.downcallCapturingLastError(
            LOOKUP, "GetMessageW", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT));
    private static final MethodHandle PEEK_MESSAGE_W = Foreign.downcall(
            LOOKUP, "PeekMessageW",
            FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT));
    private static final MethodHandle TRANSLATE_MESSAGE = Foreign.downcall(
            LOOKUP, "TranslateMessage", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    private static final MethodHandle DISPATCH_MESSAGE_W = Foreign.downcall(
            LOOKUP, "DispatchMessageW", FunctionDescriptor.of(JAVA_LONG, ADDRESS));
    private static final MethodHandle POST_THREAD_MESSAGE_W = Foreign.downcallCapturingLastError(
            LOOKUP, "PostThreadMessageW", FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT, JAVA_LONG, JAVA_LONG));
    private static final MethodHandle POST_MESSAGE_W = Foreign.downcallCapturingLastError(
            LOOKUP, "PostMessageW", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_LONG, JAVA_LONG));

    /** Callback descriptor for {@code WINEVENTPROC}: {@code void(ADDRESS,int,ADDRESS,int,int,int,int)}. */
    public static final FunctionDescriptor WINEVENTPROC_DESCRIPTOR = FunctionDescriptor.ofVoid(
            ADDRESS, JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT);

    private static final FunctionDescriptor MONITORENUMPROC_DESCRIPTOR = FunctionDescriptor.of(
            JAVA_INT, ADDRESS, ADDRESS, ADDRESS, JAVA_LONG);

    private static final MethodHandle WINEVENTPROC_TARGET;
    private static final MethodHandle MONITORENUMPROC_TARGET;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            WINEVENTPROC_TARGET = lookup.findVirtual(WinEventProc.class, "onEvent",
                    MethodType.methodType(void.class,
                            MemorySegment.class, int.class, MemorySegment.class,
                            int.class, int.class, int.class, int.class));
            MONITORENUMPROC_TARGET = lookup.findStatic(User32.class, "monitorEnumThunk",
                    MethodType.methodType(int.class, MonitorEnumProc.class,
                            MemorySegment.class, MemorySegment.class, MemorySegment.class, long.class));
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static final int TEXT_SCRATCH_CHARS = 1024;

    /**
     * Reusable per-thread UTF-16 scratch buffer for {@code GetWindowTextW}. Backed by an automatic
     * arena so it is reclaimed when the owning thread and its {@link ThreadLocal} are collected;
     * no allocation happens per event/title read.
     */
    private static final ThreadLocal<MemorySegment> TEXT_SCRATCH = ThreadLocal.withInitial(
            () -> Arena.ofAuto().allocate((long) TEXT_SCRATCH_CHARS * Character.BYTES, Character.BYTES));

    private User32() {
    }

    /** {@code void CALLBACK WinEventProc(HWINEVENTHOOK, DWORD, HWND, LONG, LONG, DWORD, DWORD)}. */
    @FunctionalInterface
    public interface WinEventProc {
        void onEvent(MemorySegment hWinEventHook, int event, MemorySegment hwnd,
                     int idObject, int idChild, int idEventThread, int dwmsEventTime);
    }

    /** {@code BOOL CALLBACK MonitorEnumProc(HMONITOR, HDC, LPRECT, LPARAM)} (window handle only). */
    @FunctionalInterface
    public interface MonitorEnumProc {
        boolean onMonitor(long hMonitor);
    }

    public static long findWindow(Arena arena, String windowName) {
        MemorySegment name = windowName == null ? MemorySegment.NULL : Foreign.wide(arena, windowName);
        try {
            MemorySegment result = (MemorySegment) FIND_WINDOW_W.invokeExact(
                    Kernel32.callState(), MemorySegment.NULL, name);
            return result.address();
        } catch (Throwable t) {
            throw new IllegalStateException("User32.FindWindowW failed", t);
        }
    }

    public static long getWindowLongPtr(long hwnd, int index) {
        try {
            return (long) GET_WINDOW_LONG_PTR_W.invokeExact(
                    Kernel32.callState(), Foreign.handle(hwnd), index);
        } catch (Throwable t) {
            throw new IllegalStateException("User32.GetWindowLongPtrW failed", t);
        }
    }

    public static long setWindowLongPtr(long hwnd, int index, long value) {
        try {
            return (long) SET_WINDOW_LONG_PTR_W.invokeExact(
                    Kernel32.callState(), Foreign.handle(hwnd), index, value);
        } catch (Throwable t) {
            throw new IllegalStateException("User32.SetWindowLongPtrW failed", t);
        }
    }

    public static long getClassLongPtr(long hwnd, int index) {
        try {
            return (long) GET_CLASS_LONG_PTR_W.invokeExact(
                    Kernel32.callState(), Foreign.handle(hwnd), index);
        } catch (Throwable t) {
            throw new IllegalStateException("User32.GetClassLongPtrW failed", t);
        }
    }

    public static boolean setLayeredWindowAttributes(long hwnd, int colorKey, byte alpha, int flags) {
        try {
            return ((int) SET_LAYERED_WINDOW_ATTRIBUTES.invokeExact(
                    Kernel32.callState(), Foreign.handle(hwnd), colorKey, alpha, flags)) != 0;
        } catch (Throwable t) {
            throw new IllegalStateException("User32.SetLayeredWindowAttributes failed", t);
        }
    }

    public static boolean setWindowPos(long hwnd, long hwndInsertAfter,
                                       int x, int y, int cx, int cy, int flags) {
        try {
            return ((int) SET_WINDOW_POS.invokeExact(
                    Kernel32.callState(), Foreign.handle(hwnd), Foreign.handle(hwndInsertAfter),
                    x, y, cx, cy, flags)) != 0;
        } catch (Throwable t) {
            throw new IllegalStateException("User32.SetWindowPos failed", t);
        }
    }

    public static boolean isWindowVisible(long hwnd) {
        try {
            return ((int) IS_WINDOW_VISIBLE.invokeExact(
                    Kernel32.callState(), Foreign.handle(hwnd))) != 0;
        } catch (Throwable t) {
            throw new IllegalStateException("User32.IsWindowVisible failed", t);
        }
    }

    public static boolean getWindowRect(long hwnd, MemorySegment rect) {
        try {
            return ((int) GET_WINDOW_RECT.invokeExact(
                    Kernel32.callState(), Foreign.handle(hwnd), rect)) != 0;
        } catch (Throwable t) {
            throw new IllegalStateException("User32.GetWindowRect failed", t);
        }
    }

    public static int getWindowTextLength(long hwnd) {
        try {
            return (int) GET_WINDOW_TEXT_LENGTH_W.invokeExact(Foreign.handle(hwnd));
        } catch (Throwable t) {
            throw new IllegalStateException("User32.GetWindowTextLengthW failed", t);
        }
    }

    /** Raw {@code GetWindowTextW}; returns the number of characters copied (excluding the null). */
    public static int getWindowText(long hwnd, MemorySegment buffer, int maxCount) {
        try {
            return (int) GET_WINDOW_TEXT_W.invokeExact(
                    Kernel32.callState(), Foreign.handle(hwnd), buffer, maxCount);
        } catch (Throwable t) {
            throw new IllegalStateException("User32.GetWindowTextW failed", t);
        }
    }

    /** Reads the window title using the reusable per-thread scratch buffer. */
    public static String getWindowText(long hwnd) {
        MemorySegment scratch = TEXT_SCRATCH.get();
        scratch.setAtIndex(JAVA_CHAR, 0, '\0');
        getWindowText(hwnd, scratch, TEXT_SCRATCH_CHARS);
        return scratch.getString(0, StandardCharsets.UTF_16LE);
    }

    /** Compares the window title to {@code expected} without allocating a {@link String}. */
    public static boolean windowTextEquals(long hwnd, String expected) {
        MemorySegment scratch = TEXT_SCRATCH.get();
        scratch.setAtIndex(JAVA_CHAR, 0, '\0');
        int copied = getWindowText(hwnd, scratch, TEXT_SCRATCH_CHARS);
        if (copied != expected.length()) {
            return false;
        }
        for (int i = 0; i < copied; i++) {
            if (scratch.getAtIndex(JAVA_CHAR, i) != expected.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Case-insensitive {@code contains}, without allocating a {@link String}.
     *
     * @param expectedLower the needle, already lower-cased by the caller
     */
    public static boolean windowTextContains(long hwnd, String expectedLower) {
        int needle = expectedLower.length();
        if (needle == 0) {
            return true;
        }
        MemorySegment scratch = TEXT_SCRATCH.get();
        scratch.setAtIndex(JAVA_CHAR, 0, '\0');
        int copied = getWindowText(hwnd, scratch, TEXT_SCRATCH_CHARS);
        if (copied < needle) {
            return false;
        }
        for (int start = 0; start <= copied - needle; start++) {
            boolean match = true;
            for (int j = 0; j < needle; j++) {
                char c = Character.toLowerCase(scratch.getAtIndex(JAVA_CHAR, start + j));
                if (c != expectedLower.charAt(j)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return true;
            }
        }
        return false;
    }

    public static long getForegroundWindow() {
        try {
            MemorySegment result = (MemorySegment) GET_FOREGROUND_WINDOW.invokeExact(
                    Kernel32.callState());
            return result.address();
        } catch (Throwable t) {
            throw new IllegalStateException("User32.GetForegroundWindow failed", t);
        }
    }

    public static boolean setForegroundWindow(long hwnd) {
        try {
            return ((int) SET_FOREGROUND_WINDOW.invokeExact(
                    Kernel32.callState(), Foreign.handle(hwnd))) != 0;
        } catch (Throwable t) {
            throw new IllegalStateException("User32.SetForegroundWindow failed", t);
        }
    }

    public static boolean bringWindowToTop(long hwnd) {
        try {
            return ((int) BRING_WINDOW_TO_TOP.invokeExact(
                    Kernel32.callState(), Foreign.handle(hwnd))) != 0;
        } catch (Throwable t) {
            throw new IllegalStateException("User32.BringWindowToTop failed", t);
        }
    }

    public static boolean attachThreadInput(int idAttach, int idAttachTo, boolean attach) {
        try {
            return ((int) ATTACH_THREAD_INPUT.invokeExact(
                    Kernel32.callState(), idAttach, idAttachTo, attach ? 1 : 0)) != 0;
        } catch (Throwable t) {
            throw new IllegalStateException("User32.AttachThreadInput failed", t);
        }
    }

    public static int getWindowThreadProcessId(long hwnd, MemorySegment processIdOut) {
        try {
            return (int) GET_WINDOW_THREAD_PROCESS_ID.invokeExact(
                    Kernel32.callState(), Foreign.handle(hwnd), processIdOut);
        } catch (Throwable t) {
            throw new IllegalStateException("User32.GetWindowThreadProcessId failed", t);
        }
    }

    /** {@code ShowWindow} returns the <em>previous</em> visibility state, not a success flag. */
    public static boolean showWindow(long hwnd, int cmdShow) {
        try {
            return ((int) SHOW_WINDOW.invokeExact(
                    Kernel32.callState(), Foreign.handle(hwnd), cmdShow)) != 0;
        } catch (Throwable t) {
            throw new IllegalStateException("User32.ShowWindow failed", t);
        }
    }

    public static long monitorFromWindow(long hwnd, int flags) {
        try {
            MemorySegment result = (MemorySegment) MONITOR_FROM_WINDOW.invokeExact(
                    Kernel32.callState(), Foreign.handle(hwnd), flags);
            return result.address();
        } catch (Throwable t) {
            throw new IllegalStateException("User32.MonitorFromWindow failed", t);
        }
    }

    public static boolean getMonitorInfo(long hMonitor, MemorySegment info) {
        try {
            return ((int) GET_MONITOR_INFO_W.invokeExact(
                    Kernel32.callState(), Foreign.handle(hMonitor), info)) != 0;
        } catch (Throwable t) {
            throw new IllegalStateException("User32.GetMonitorInfoW failed", t);
        }
    }

    public static boolean isWindow(long hwnd) {
        try {
            return ((int) IS_WINDOW.invokeExact(Foreign.handle(hwnd))) != 0;
        } catch (Throwable t) {
            throw new IllegalStateException("User32.IsWindow failed", t);
        }
    }

    public static boolean isIconic(long hwnd) {
        try {
            return ((int) IS_ICONIC.invokeExact(
                    Kernel32.callState(), Foreign.handle(hwnd))) != 0;
        } catch (Throwable t) {
            throw new IllegalStateException("User32.IsIconic failed", t);
        }
    }

    public static int getDpiForWindow(long hwnd) {
        try {
            return (int) GET_DPI_FOR_WINDOW.invokeExact(
                    Kernel32.callState(), Foreign.handle(hwnd));
        } catch (Throwable t) {
            throw new IllegalStateException("User32.GetDpiForWindow failed", t);
        }
    }

    public static boolean enumDisplaySettings(Arena arena, String deviceName, int modeNum, MemorySegment devMode) {
        MemorySegment name = deviceName == null ? MemorySegment.NULL : Foreign.wide(arena, deviceName);
        try {
            return ((int) ENUM_DISPLAY_SETTINGS_W.invokeExact(name, modeNum, devMode)) != 0;
        } catch (Throwable t) {
            throw new IllegalStateException("User32.EnumDisplaySettingsW failed", t);
        }
    }

    public static long createWindowEx(int exStyle, MemorySegment className, MemorySegment windowName,
                                      int style, int x, int y, int width, int height,
                                      long parent, long menu, long instance, MemorySegment param) {
        try {
            MemorySegment result = (MemorySegment) CREATE_WINDOW_EX_W.invokeExact(
                    Kernel32.callState(), exStyle, className, windowName, style, x, y, width, height,
                    Foreign.handle(parent), Foreign.handle(menu), Foreign.handle(instance), param);
            return result.address();
        } catch (Throwable t) {
            throw new IllegalStateException("User32.CreateWindowExW failed", t);
        }
    }

    public static boolean destroyWindow(long hwnd) {
        try {
            return ((int) DESTROY_WINDOW.invokeExact(Foreign.handle(hwnd))) != 0;
        } catch (Throwable t) {
            throw new IllegalStateException("User32.DestroyWindow failed", t);
        }
    }

    public static boolean screenToClient(long hwnd, MemorySegment point) {
        return screenToClient(Foreign.handle(hwnd), point);
    }

    /**
     * Allocation-free overload for callers that retain a stable segment view of an HWND.
     */
    public static boolean screenToClient(MemorySegment hwnd, MemorySegment point) {
        try {
            return ((int) SCREEN_TO_CLIENT.invokeExact(hwnd, point)) != 0;
        } catch (Throwable t) {
            throw new IllegalStateException("User32.ScreenToClient failed", t);
        }
    }

    public static boolean clientToScreen(long hwnd, MemorySegment point) {
        try {
            return ((int) CLIENT_TO_SCREEN.invokeExact(Foreign.handle(hwnd), point)) != 0;
        } catch (Throwable t) {
            throw new IllegalStateException("User32.ClientToScreen failed", t);
        }
    }

    public static boolean getClientRect(long hwnd, MemorySegment rect) {
        try {
            return ((int) GET_CLIENT_RECT.invokeExact(Foreign.handle(hwnd), rect)) != 0;
        } catch (Throwable t) {
            throw new IllegalStateException("User32.GetClientRect failed", t);
        }
    }

    public static long setWinEventHook(int eventMin, int eventMax, long hmodWinEventProc,
                                       MemorySegment winEventProc, int idProcess, int idThread, int flags) {
        try {
            MemorySegment result = (MemorySegment) SET_WIN_EVENT_HOOK.invokeExact(
                    Kernel32.callState(), eventMin, eventMax, Foreign.handle(hmodWinEventProc), winEventProc,
                    idProcess, idThread, flags);
            return result.address();
        } catch (Throwable t) {
            throw new IllegalStateException("User32.SetWinEventHook failed", t);
        }
    }

    public static boolean unhookWinEvent(long hWinEventHook) {
        try {
            return ((int) UNHOOK_WIN_EVENT.invokeExact(
                    Kernel32.callState(), Foreign.handle(hWinEventHook))) != 0;
        } catch (Throwable t) {
            throw new IllegalStateException("User32.UnhookWinEvent failed", t);
        }
    }

    /** {@code GetMessageW}: returns {@code >0} for a message, {@code 0} for WM_QUIT, {@code -1} on error. */
    public static int getMessage(MemorySegment msg, long hwnd, int filterMin, int filterMax) {
        try {
            return (int) GET_MESSAGE_W.invokeExact(
                    Kernel32.callState(), msg, Foreign.handle(hwnd), filterMin, filterMax);
        } catch (Throwable t) {
            throw new IllegalStateException("User32.GetMessageW failed", t);
        }
    }

    /**
     * Forces creation of the current native thread's message queue without removing a message.
     */
    public static boolean peekMessage(MemorySegment msg, long hwnd,
                                      int filterMin, int filterMax, int removeMessage) {
        try {
            return ((int) PEEK_MESSAGE_W.invokeExact(
                    msg, Foreign.handle(hwnd), filterMin, filterMax, removeMessage)) != 0;
        } catch (Throwable t) {
            throw new IllegalStateException("User32.PeekMessageW failed", t);
        }
    }

    public static void translateMessage(MemorySegment msg) {
        try {
            int ignored = (int) TRANSLATE_MESSAGE.invokeExact(msg);
        } catch (Throwable t) {
            throw new IllegalStateException("User32.TranslateMessage failed", t);
        }
    }

    public static void dispatchMessage(MemorySegment msg) {
        try {
            long ignored = (long) DISPATCH_MESSAGE_W.invokeExact(msg);
        } catch (Throwable t) {
            throw new IllegalStateException("User32.DispatchMessageW failed", t);
        }
    }

    public static boolean postThreadMessage(int idThread, int message, long wParam, long lParam) {
        try {
            return ((int) POST_THREAD_MESSAGE_W.invokeExact(
                    Kernel32.callState(), idThread, message, wParam, lParam)) != 0;
        } catch (Throwable t) {
            throw new IllegalStateException("User32.PostThreadMessageW failed", t);
        }
    }

    public static boolean postMessage(long hwnd, int message, long wParam, long lParam) {
        try {
            return ((int) POST_MESSAGE_W.invokeExact(
                    Kernel32.callState(), Foreign.handle(hwnd), message, wParam, lParam)) != 0;
        } catch (Throwable t) {
            throw new IllegalStateException("User32.PostMessageW failed", t);
        }
    }

    /**
     * Creates the single shared {@code WINEVENTPROC} upcall stub bound to {@code proc}. The stub
     * lives in {@code arena} and must remain open until every {@code UnhookWinEvent} has completed.
     */
    public static MemorySegment createWinEventUpcall(WinEventProc proc, Arena arena) {
        MethodHandle bound = WINEVENTPROC_TARGET.bindTo(proc);
        return Foreign.LINKER.upcallStub(bound, WINEVENTPROC_DESCRIPTOR, arena);
    }

    /** Enumerates the display monitors, invoking {@code callback} once per monitor handle. */
    public static boolean enumDisplayMonitors(MonitorEnumProc callback) {
        try (Arena arena = Arena.ofConfined()) {
            MethodHandle bound = MONITORENUMPROC_TARGET.bindTo(callback);
            MemorySegment stub = Foreign.LINKER.upcallStub(bound, MONITORENUMPROC_DESCRIPTOR, arena);
            return ((int) ENUM_DISPLAY_MONITORS.invokeExact(
                    Kernel32.callState(), MemorySegment.NULL, MemorySegment.NULL, stub, 0L)) != 0;
        } catch (Throwable t) {
            throw new IllegalStateException("User32.EnumDisplayMonitors failed", t);
        }
    }

    @SuppressWarnings("unused") // referenced reflectively for the upcall stub
    private static int monitorEnumThunk(MonitorEnumProc callback, MemorySegment hMonitor,
                                        MemorySegment hdc, MemorySegment lprcMonitor, long lParam) {
        boolean keepGoing;
        try {
            keepGoing = callback.onMonitor(hMonitor.address());
        } catch (Throwable t) {
            keepGoing = true; // never let a Java exception cross the native boundary
        }
        return keepGoing ? 1 : 0;
    }
}
