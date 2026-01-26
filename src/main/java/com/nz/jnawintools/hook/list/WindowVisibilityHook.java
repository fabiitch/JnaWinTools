package com.nz.jnawintools.hook.list;

import com.nz.jnawintools.hook.event.WindowEventAction;
import com.nz.jnawintools.hook.event.dispatch.AbstractEventDispatcher;
import com.nz.jnawintools.hook.window.WindowChecker;
import org.slf4j.Logger;
import com.sun.jna.platform.win32.WinDef;

import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_OBJECT_HIDE;
import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_OBJECT_SHOW;

public class WindowVisibilityHook extends BaseWindowHook {
    public WindowVisibilityHook(WindowChecker windowToTrackChecker,
                                AbstractEventDispatcher<WindowEventAction> messageDispatcher,
                                Logger logger) {
        super(windowToTrackChecker, messageDispatcher, logger);
    }

    @Override
    protected void onEvent(WinDef.DWORD event, WinDef.HWND hwnd,
                           WinDef.LONG idObject, WinDef.LONG idChild,
                           WinDef.DWORD dwEventThread, WinDef.DWORD dwmsEventTime) {
        String windowTitle = window64Helper.getName(hwnd);
        if (!windowToTrackChecker.isWindow(hwnd)) return;

        // Ignore si la fenêtre est minimisée : on laisse MinimiseHook gérer ça
        if (window64Helper.isIconic(hwnd)) {
            return;
        }
        // Déclencher SHOW ou HIDE
        if (event.intValue() == EVENT_OBJECT_SHOW) {
//                    dispatch(WindowEventAction.Show);
        } else if (event.intValue() == EVENT_OBJECT_HIDE) {
//                    dispatch(WindowEventAction.Hide);
        }
    }

    @Override
    protected String name() {
        return "VisibilityHook";
    }

    @Override
    protected int eventMin() {
        return EVENT_OBJECT_SHOW;
    }

    @Override
    protected int eventMax() {
        return EVENT_OBJECT_HIDE;
    }
}
