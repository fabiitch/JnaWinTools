package com.nz.jnawintools.hook.window;

import com.nz.jnawintools.win32.User32;

import java.util.Locale;

/**
 * Matches a window whose title contains a substring (case-insensitive).
 *
 * <p>Like {@link WindowTitleEqualsChecker}, the last matching handle is cached and periodically
 * revalidated. Destroy/name-change events invalidate it immediately.
 */
public class WindowTitleContainsChecker implements WindowChecker {

    private static final int CACHE_VALIDATION_INTERVAL = 256;

    private final String expected;
    private final String expectedLower;
    private volatile long lastMatchingHwnd;
    private int cacheHitsUntilValidation;

    public WindowTitleContainsChecker(String expected) {
        this.expected = expected;
        this.expectedLower = expected.toLowerCase(Locale.ROOT);
    }

    public static WindowTitleContainsChecker get(String expected) {
        return new WindowTitleContainsChecker(expected);
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
        boolean matches = User32.windowTextContains(hwnd, expectedLower);
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
