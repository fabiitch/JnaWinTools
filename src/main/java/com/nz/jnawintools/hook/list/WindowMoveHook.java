package com.nz.jnawintools.hook.list;

import com.nz.jnawintools.hook.WindowEventAction;
import com.nz.jnawintools.hook.events.AbstractEventDispatcher;
import com.nz.jnawintools.log.JnaWinToolsLogger;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;

import java.awt.*;

import static com.nz.jnawintools.hook.cst.WinEventConstants.*;


public class WindowMoveHook extends BaseWindowHook {


    public WindowMoveHook(String windowToTrack,
                          AbstractEventDispatcher<WindowEventAction> messageDispatcher,
                          JnaWinToolsLogger logger) {
        super(windowToTrack, messageDispatcher, logger);
    }

    @Override
    protected String name() {
        return "MoveHook";
    }


    Rectangle lastBound;
    @Override
    public WinNT.HANDLE buildHook() {
        WinUser.WinEventProc eventProc = new WinUser.WinEventProc() {
            public void callback(WinNT.HANDLE hWinEventHook, WinDef.DWORD event,
                                 WinDef.HWND hwnd, WinDef.LONG idObject, WinDef.LONG idChild,
                                 WinDef.DWORD dwEventThread, WinDef.DWORD dwmsEventTime) {
                if (idObject.intValue() != 0 || idChild.intValue() != 0) return;


                String windowTitle = window64Helper.getName(hwnd);
                if (WINDOW_TRACKED.equalsIgnoreCase(windowTitle)) {
                    dispatch(WindowEventAction.Move);
                }
            }
        };

        return User32.INSTANCE.SetWinEventHook(
            EVENT_OBJECT_LOCATIONCHANGE,
            EVENT_OBJECT_LOCATIONCHANGE,
            null,
            eventProc,
            0,
            0,
            WINEVENT_OUTOFCONTEXT | WINEVENT_SKIPOWNPROCESS
        );
    }
}
