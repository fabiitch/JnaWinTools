//package com.nz.jnawintools.hook.window;
//
//public class WindowExeNameChecker implements WindowChecker {
//    private final String exeNameLower; // "LeagueClientUx.exe" ou juste "leagueclientux.exe"
//    public ExeNameChecker(String exeName) { this.exeNameLower = exeName.toLowerCase(); }
//    @Override public boolean isWindow(WinDef.HWND hwnd) {
//        hwnd = WinHelpers.getRoot(hwnd);
//        if (!WinHelpers.isValid(hwnd)) return false;
//        int pid = WinHelpers.getPid(hwnd);
//        if (pid <= 0) return false;
//        String full = WinHelpers.getExePathByPid(pid).toLowerCase();
//        return full.endsWith(exeNameLower) || full.contains("\\" + exeNameLower);
//    }
//}
