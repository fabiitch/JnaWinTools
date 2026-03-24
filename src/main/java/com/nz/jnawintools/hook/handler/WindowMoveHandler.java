package com.nz.jnawintools.hook.handler;

import com.nz.jnawintools.hook.event.RawWinEvent;
import com.nz.jnawintools.hook.event.WindowEventAction;
import com.nz.jnawintools.hook.event.dispatch.AbstractEventDispatcher;
import com.nz.jnawintools.hook.window.WindowChecker;
import lombok.extern.slf4j.Slf4j;

import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_OBJECT_LOCATIONCHANGE;
import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_SYSTEM_MINIMIZE_END;
import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_SYSTEM_MINIMIZE_START;

@Slf4j
public class WindowMoveHandler extends BaseWindowEventHandler {

    public WindowMoveHandler(WindowChecker windowToTrackChecker,
                             AbstractEventDispatcher<WindowEventAction> dispatcher) {
        super(windowToTrackChecker, dispatcher);
    }

    @Override
    public String name() {
        return "MoveHandler";
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
            log.trace("[{}] ignored event={} (idObject={}, idChild={})",
                    name(), event.getEvent(), event.getIdObject(), event.getIdChild());
            return;
        }
        if (!windowToTrackChecker.isWindow(event.getHwnd())) {
            log.trace("[{}] ignored event={} for non tracked hwnd={}", name(), event.getEvent(), event.getHwnd());
            return;
        }

        log.trace("[{}] dispatch action={} for hwnd={} event={}",
                name(), WindowEventAction.Move, event.getHwnd(), event.getEvent());
        dispatch(WindowEventAction.Move);
    }
}
