package com.nz.jnawintools.hook.window;

public class WindowHwndChecker implements WindowChecker {
    private final long target;

    public WindowHwndChecker(long target) {
        this.target = target;
    }

    @Override
    public boolean isWindow(long hwnd) {
        return hwnd != 0L && hwnd == target;
    }

    @Override
    public String getWindowName() {
        return "";
    }
}
