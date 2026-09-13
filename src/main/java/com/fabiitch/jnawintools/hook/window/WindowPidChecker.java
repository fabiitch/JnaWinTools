//package com.nz.jnawintools.hook.window;
//
//public class WindowPidChecker implements WindowChecker {
//    private final int pid;
//    public PidChecker(int pid) { this.pid = pid; }
//    @Override public boolean isWindow(WinDef.HWND hwnd) {
//        hwnd = WinHelpers.getRoot(hwnd);
//        if (!WinHelpers.isValid(hwnd)) return false;
//        return WinHelpers.getPid(hwnd) == pid;
//    }
//}
