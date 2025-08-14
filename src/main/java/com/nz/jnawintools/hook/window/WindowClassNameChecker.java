//package com.nz.jnawintools.hook.window;
//
//import com.sun.jna.platform.win32.WinDef;
//import lombok.AllArgsConstructor;
//
//@AllArgsConstructor
//public class WindowClassNameChecker implements WindowChecker {
//    private final String className;
//
//    @Override
//    public boolean isWindow(WinDef.HWND hwnd) {
//        hwnd = WinHelpers.getRoot(hwnd);
//        if (!WinHelpers.isValid(hwnd)) return false;
//        String cls = WinHelpers.getClassName(hwnd);
//        return className != null && className.equals(cls);
//    }
//
//    @Override
//    public String getWindowName() {
//        return "ClassName=" + className;
//    }
//}
