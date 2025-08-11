//package com.nz.jnawintools.hook.v2;
//
//import com.nz.jnawintools.hook.list.BaseWindowHook;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class WindowHookManager {
//    private final List<BaseWindowHook> hooks = new ArrayList<>();
//    private Thread messagePumpThread;
//    private WindowsMessagePump messagePump;
//
//    public void start(boolean headless) {
//        // Démarrer le message pump seulement en mode headless (pas de GLFW)
//        if (headless) {
//            messagePump = new WindowsMessagePump(false);
//            messagePumpThread = new Thread(messagePump, "WindowsMessagePump");
//            messagePumpThread.setDaemon(true);
//            messagePumpThread.start();
//        }
//    }
//
//    public void stop() {
//        if (messagePump != null) {
//            messagePump.stop();
//        }
//        for (BaseWindowHook hook : hooks) {
//            hook.dispose();
//        }
//    }
//
//    public void addHook(BaseWindowHook hook) {
//        hooks.add(hook);
//    }
//}
