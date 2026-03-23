package com.nz.jnawintools.hook.v2.handler;

import com.nz.jnawintools.hook.event.WindowEventAction;
import com.nz.jnawintools.hook.event.dispatch.AbstractEventDispatcher;
import com.nz.jnawintools.hook.v2.RawWinEvent;
import com.nz.jnawintools.hook.window.WindowChecker;
import org.slf4j.Logger;

import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_OBJECT_LOCATIONCHANGE;
import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_SYSTEM_MINIMIZE_END;
import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_SYSTEM_MINIMIZE_START;

public class WindowMoveHandlerV2 extends BaseWindowEventHandler {

    public WindowMoveHandlerV2(WindowChecker windowToTrackChecker,
                               AbstractEventDispatcher<WindowEventAction> dispatcher,
                               Logger logger) {
        super(windowToTrackChecker, dispatcher, logger);
    }

    @Override
    public String name() {
        return "MoveHandlerV2";
    }

    @Override
    public boolean supports(RawWinEvent event) {
        int eventId = event.getEvent();
        return eventId == EVENT_OBJECT_LOCATIONCHANGE
                || eventId == EVENT_SYSTEM_MINIMIZE_START
                || eventId == EVENT_SYSTEM_MINIMIZE_END;
    }

    @Override
    public void handle(RawWinEvent event) {
        if (!event.isWindowObject()) {
            logger.trace("[{}] ignored event={} (idObject={}, idChild={})",
                    name(), event.getEvent(), event.getIdObject(), event.getIdChild());
            return;
        }
        if (!windowToTrackChecker.isWindow(event.getHwnd())) {
            logger.trace("[{}] ignored event={} for non tracked hwnd={}", name(), event.getEvent(), event.getHwnd());
            return;
        }

        logger.trace("[{}] dispatch action={} for hwnd={} event={}",
                name(), WindowEventAction.Move, event.getHwnd(), event.getEvent());
        dispatch(WindowEventAction.Move);
    }
}
