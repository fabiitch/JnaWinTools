package com.nz.jnawintools.hook.list;

import com.nz.jnawintools.hook.event.WindowEventAction;
import com.nz.jnawintools.hook.event.dispatch.AbstractEventDispatcher;
import com.nz.jnawintools.hook.window.WindowChecker;
import com.sun.jna.platform.win32.WinDef;
import org.slf4j.Logger;

import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_OBJECT_LOCATIONCHANGE;

public class WindowMoveHook extends BaseWindowHook {

    public WindowMoveHook(WindowChecker windowToTrackChecker,
                          AbstractEventDispatcher<WindowEventAction> messageDispatcher,
                          Logger logger) {
        super(windowToTrackChecker, messageDispatcher, logger);
    }

    @Override
    protected void onEvent(WinDef.DWORD event, WinDef.HWND hwnd,
                           WinDef.LONG idObject, WinDef.LONG idChild,
                           WinDef.DWORD dwEventThread, WinDef.DWORD dwmsEventTime) {
        if (idObject.intValue() != 0 || idChild.intValue() != 0) {
            logger.trace("[{}] ignored event={} (idObject={}, idChild={})", name(), event.intValue(), idObject.intValue(), idChild.intValue());
            return;
        }
        if (!windowToTrackChecker.isWindow(hwnd)) {
            logger.trace("[{}] ignored event={} for non tracked hwnd={}", name(), event.intValue(), hwnd);
            return;
        }

        logger.trace("[{}] dispatch action={} for hwnd={}", name(), WindowEventAction.Move, hwnd);
        dispatch(WindowEventAction.Move);
    }

    @Override
    protected String name() {
        return "MoveHook";
    }

    @Override
    protected int eventMin() {
        return EVENT_OBJECT_LOCATIONCHANGE;
    }

    @Override
    protected int eventMax() {
        return EVENT_OBJECT_LOCATIONCHANGE;
    }
}
