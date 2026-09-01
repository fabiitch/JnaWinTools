package com.nz.jnawintools.hook.window;

public interface WindowChecker {

    /**
     * @param hwnd the opaque window handle as a primitive {@code long} ({@code 0} = null)
     */
    boolean isWindow(long hwnd);

    /**
     * Invalidates any identity cache associated with {@code hwnd}.
     */
    default void invalidate(long hwnd) {
    }

    String getWindowName();
}
