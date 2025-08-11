//package com.nz.jnawintools.hook.v2;
//
//
//import com.sun.jna.platform.win32.User32;
//import com.sun.jna.platform.win32.WinUser;
//
//public class WindowsMessagePump implements Runnable {
//    private volatile boolean running = true;
//    private final boolean blocking;
//
//    public WindowsMessagePump(boolean blocking) {
//        this.blocking = blocking;
//    }
//
//    public void stop() {
//        running = false;
//    }
//
//    @Override
//    public void run() {
//        User32 user32 = User32.INSTANCE;
//        WinUser.MSG msg = new WinUser.MSG();
//
//        while (running) {
//            if (blocking) {
//                user32.GetMessage(msg, null, 0, 0);
//                user32.TranslateMessage(msg);
//                user32.DispatchMessage(msg);
//            } else {
//                while (user32.PeekMessage(msg, null, 0, 0, 1)) {
//                    user32.TranslateMessage(msg);
//                    user32.DispatchMessage(msg);
//                }
//                try {
//                    Thread.sleep(10); // évite de surconsommer le CPU
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//            }
//        }
//    }
//}
