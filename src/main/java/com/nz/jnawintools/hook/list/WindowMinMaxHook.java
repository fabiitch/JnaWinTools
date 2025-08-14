package com.nz.jnawintools.hook.list;

import com.nz.jnawintools.hook.WindowEventAction;
import com.nz.jnawintools.hook.events.AbstractEventDispatcher;
import com.nz.jnawintools.hook.window.WindowChecker;
import com.nz.jnawintools.log.JWTLogger;
import com.sun.jna.platform.win32.WinDef;

import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_SYSTEM_MINIMIZE_END;
import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_SYSTEM_MINIMIZE_START;

public class WindowMinMaxHook extends BaseWindowHook {

    public WindowMinMaxHook(WindowChecker windowToTrackChecker,
                             AbstractEventDispatcher<WindowEventAction> messageDispatcher,
                             JWTLogger logger) {
        super(windowToTrackChecker, messageDispatcher, logger);
    }

    @Override
    protected void onEvent(WinDef.DWORD event, WinDef.HWND hwnd,
                           WinDef.LONG idObject, WinDef.LONG idChild,
                           WinDef.DWORD dwEventThread, WinDef.DWORD dwmsEventTime) {
        if (idObject.intValue() != 0 || idChild.intValue() != 0) return;
        String windowTitle = window64Helper.getName(hwnd);
        if (!windowToTrackChecker.isWindow(hwnd)) return;

        int eventValue = event.intValue();
        if (eventValue == EVENT_SYSTEM_MINIMIZE_START) {
//                System.out.println("MINI START");
//                        dispatch(WindowEventAction.MinimiseStart);
        } else if (eventValue == EVENT_SYSTEM_MINIMIZE_END) {
//                System.out.println("MINI END");
//                        dispatch(WindowEventAction.MinimiseEnd);
        }
    }

    @Override
    protected String name() {
        return "MinMaxHook";
    }


    @Override
    protected int eventMin() {
        return EVENT_SYSTEM_MINIMIZE_START;
    }

    @Override
    protected int eventMax() {
        return EVENT_SYSTEM_MINIMIZE_END;
    }
}
