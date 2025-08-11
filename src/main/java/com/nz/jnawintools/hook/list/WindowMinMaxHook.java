//package com.nz.jnawintools.hook.list;
//
//import com.nz.jnawintools.hook.WindowEventAction;
//import com.nz.jnawintools.hook.events.AbstractEventDispatcher;
//import com.nz.jnawintools.hook.list.BaseWindowHook;
//import com.nz.jnawintools.log.JnaWinToolsLogger;
//import com.sun.jna.Native;
//import com.sun.jna.platform.win32.User32;
//import com.sun.jna.platform.win32.WinDef;
//import com.sun.jna.platform.win32.WinNT;
//import com.sun.jna.platform.win32.WinUser;
//
//import static com.nz.jnawintools.hook.cst.WinEventConstants.*;
//
//
//public class WindowMinMaxHook extends BaseWindowHook {
//    public WindowMinMaxHook(String windowToTrack,
//                            AbstractEventDispatcher<WindowEventAction> messageDispatcher,
//                            JnaWinToolsLogger logger) {
//        super(windowToTrack, messageDispatcher, logger);
//    }
//
//    @Override
//    protected String name() {
//        return "MinMaxHook";
//    }
//
//    @Override
//    protected WinNT.HANDLE buildHook() {
//        WinUser.WinEventProc eventProc = new WinUser.WinEventProc() {
//            @Override
//            public void callback(WinNT.HANDLE hWinEventHook,
//                                 WinDef.DWORD event,
//                                 WinDef.HWND hwnd,
//                                 WinDef.LONG idObject,
//                                 WinDef.LONG idChild,
//                                 WinDef.DWORD dwEventThread,
//                                 WinDef.DWORD dwmsEventTime) {
//                if (idObject.intValue() != 0 || idChild.intValue() != 0) return;
//
//
//                String windowTitle = window64Helper.getName(hwnd);
//
//                if (WINDOW_TRACKED.equalsIgnoreCase(windowTitle)) {
//                    if (event.intValue() == EVENT_SYSTEM_MINIMIZE_START) {
////                        dispatch(WindowEventAction.MinimiseStart);
//                    } else if (event.intValue() == EVENT_SYSTEM_MINIMIZE_END) {
////                        dispatch(WindowEventAction.MinimiseEnd);
//                    }
//                }
//            }
//        };
//
//        return User32.INSTANCE.SetWinEventHook(
//            EVENT_SYSTEM_MINIMIZE_START,
//            EVENT_SYSTEM_MINIMIZE_END,
//            null,
//            eventProc,
//            0, 0,
//            WINEVENT_OUTOFCONTEXT
//        );
//    }
//}
