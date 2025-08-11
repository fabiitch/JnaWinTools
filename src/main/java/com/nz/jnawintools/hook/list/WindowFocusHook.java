package com.nz.jnawintools.hook.list;

import com.nz.jnawintools.hook.WindowEventAction;
import com.nz.jnawintools.hook.events.AbstractEventDispatcher;
import com.nz.jnawintools.log.JnaWinToolsLogger;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;

import static com.nz.jnawintools.hook.cst.WinEventConstants.*;


public class WindowFocusHook extends BaseWindowHook {

    private boolean hasFocus;

    public WindowFocusHook(String windowToTrack,
                           AbstractEventDispatcher<WindowEventAction> messageDispatcher,
                           JnaWinToolsLogger logger) {
        super(windowToTrack, messageDispatcher, logger);
    }

    @Override
    protected String name() {
        return "FocusHook";
    }

    @Override
    protected WinNT.HANDLE buildHook() {
        hasFocus =  WINDOW_TRACKED.equalsIgnoreCase(window64Helper.getActiveWindowTitle());
        logger.debug("Initial focus = {}", hasFocus);
        WinUser.WinEventProc eventProc = new WinUser.WinEventProc() {

            @Override
            public void callback(WinNT.HANDLE hWinEventHook,
                                 WinDef.DWORD event,
                                 WinDef.HWND hwnd,
                                 WinDef.LONG idObject,
                                 WinDef.LONG idChild,
                                 WinDef.DWORD dwEventThread,
                                 WinDef.DWORD dwmsEventTime) {
                if (idObject.intValue() != 0 || idChild.intValue() != 0) return;

                String windowTitle = window64Helper.getName(hwnd);
                boolean trackedWindowHasFocus = WINDOW_TRACKED.equalsIgnoreCase(windowTitle);
                WindowEventAction action = null;
                if (hasFocus && !trackedWindowHasFocus) {
                    action = WindowEventAction.LooseFocus;
                } else if (!hasFocus && trackedWindowHasFocus) {
                    action = WindowEventAction.GainFocus;
                }
                hasFocus = trackedWindowHasFocus;
                if (action != null)
                    dispatch(action);
            }
        };

        return User32.INSTANCE.SetWinEventHook(
            EVENT_SYSTEM_FOREGROUND,
            EVENT_SYSTEM_FOREGROUND,
            null,
            eventProc,
            0, 0,
            WINEVENT_OUTOFCONTEXT | WINEVENT_SKIPOWNPROCESS
        );
    }
}
