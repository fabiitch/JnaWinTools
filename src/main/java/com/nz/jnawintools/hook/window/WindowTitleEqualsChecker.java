package com.nz.jnawintools.hook.window;

import com.nz.jnawintools.win32.User32;

/**
 * Matches a window by exact title.
 *
 * <p>The last matching handle is cached: repeated LOCATIONCHANGE events avoid title reads and
 * allocations. Destroy/name-change events invalidate the cache, with periodic revalidation as a
 * guard against a missed notification or a recycled native handle.
 */
public class WindowTitleEqualsChecker implements WindowChecker {

    private static final int CACHE_VALIDATION_INTERVAL = 256;

    private final String expected;
    private volatile long lastMatchingHwnd;
    private int cacheHitsUntilValidation;

    public WindowTitleEqualsChecker(String expected) {
        this.expected = expected;
    }

    public static WindowTitleEqualsChecker get(String expected) {
        return new WindowTitleEqualsChecker(expected);
    }

    @Override
    public boolean isWindow(long hwnd) {
        if (hwnd == 0L) {
            return false;
        }
        long cached = lastMatchingHwnd;
        if (hwnd == cached && cacheHitsUntilValidation > 0) {
            cacheHitsUntilValidation--;
            return true;
        }
        boolean matches = User32.windowTextEquals(hwnd, expected);
        if (matches) {
            lastMatchingHwnd = hwnd;
            cacheHitsUntilValidation = CACHE_VALIDATION_INTERVAL;
        } else if (hwnd == cached) {
            invalidate(hwnd);
        }
        return matches;
    }

    @Override
    public void invalidate(long hwnd) {
        if (lastMatchingHwnd == hwnd) {
            cacheHitsUntilValidation = 0;
            lastMatchingHwnd = 0L;
        }
    }

    @Override
    public String getWindowName() {
        return expected;
    }
}
