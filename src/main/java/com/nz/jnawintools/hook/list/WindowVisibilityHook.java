//package com.nz.jnawintools.hook.list;
//
//import com.nz.jnawintools.hook.WindowEventAction;
//import com.nz.jnawintools.hook.events.AbstractEventDispatcher;
//import com.nz.jnawintools.log.JnaWinToolsLogger;
//import com.nz.jnawintools.window.Window64Helper;
//import com.sun.jna.platform.win32.User32;
//import com.sun.jna.platform.win32.WinDef;
//import com.sun.jna.platform.win32.WinNT;
//import com.sun.jna.platform.win32.WinUser;
//
//import static com.nz.jnawintools.hook.cst.WinEventConstants.WINEVENT_OUTOFCONTEXT;
//
///**
// * Hook qui détecte uniquement quand la fenêtre suivie est
// * affichée (SHOW) ou cachée (HIDE), mais **pas** quand elle
// * est simplement minimisée/restaurée.
// */
//public class WindowVisibilityHook extends BaseWindowHook {
//
//    private static final int EVENT_OBJECT_SHOW = 0x8002;
//    private static final int EVENT_OBJECT_HIDE = 0x8003;
//
//    public WindowVisibilityHook(String windowToTrack,
//                                AbstractEventDispatcher<WindowEventAction> messageDispatcher,
//                                JnaWinToolsLogger coreLogger) {
//        super(windowToTrack, messageDispatcher, coreLogger);
//    }
//
//    @Override
//    protected String name() {
//        return "VisibilityHook";
//    }
//
//    @Override
//    protected WinNT.HANDLE buildHook() {
//        WinUser.WinEventProc eventProc = new WinUser.WinEventProc() {
//
//            @Override
//            public void callback(WinNT.HANDLE hWinEventHook,
//                                 WinDef.DWORD event,
//                                 WinDef.HWND hwnd,
//                                 WinDef.LONG idObject,
//                                 WinDef.LONG idChild,
//                                 WinDef.DWORD dwEventThread,
//                                 WinDef.DWORD dwmsEventTime) {
//                String windowTitle = window64Helper.getName(hwnd);
//
//                // On ne traite que la fenêtre suivie
//                // On ne s'intéresse qu'à la fenêtre trackée
//                if (!WINDOW_TRACKED.equalsIgnoreCase(windowTitle)) {
//                    return;
//                }
//                // Ignore si la fenêtre est minimisée : on laisse MinimiseHook gérer ça
//                if (window64Helper.isIconic(WINDOW_TRACKED)) {
//                    return;
//                }
//                // Déclencher SHOW ou HIDE
//                if (event.intValue() == EVENT_OBJECT_SHOW) {
////                    dispatch(WindowEventAction.Show);
//                } else if (event.intValue() == EVENT_OBJECT_HIDE) {
////                    dispatch(WindowEventAction.Hide);
//                }
//            }
//        };
//
//        return User32.INSTANCE.SetWinEventHook(
//            EVENT_OBJECT_SHOW,
//            EVENT_OBJECT_HIDE,
//            null,
//            eventProc,
//            0, 0,
//            WINEVENT_OUTOFCONTEXT
//        );
//    }
//}
