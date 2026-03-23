package com.nz.jnawintools.hook.v2.handler;

import com.nz.jnawintools.hook.event.WindowEventAction;
import com.nz.jnawintools.hook.event.dispatch.AbstractEventDispatcher;
import com.nz.jnawintools.hook.v2.RawWinEvent;
import com.nz.jnawintools.hook.window.WindowChecker;
import org.slf4j.Logger;

import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_OBJECT_CREATE;
import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_OBJECT_DESTROY;

public class WindowLifecycleHandlerV2 extends BaseWindowEventHandler {

    public WindowLifecycleHandlerV2(WindowChecker windowToTrackChecker,
                                    AbstractEventDispatcher<WindowEventAction> dispatcher,
                                    Logger logger) {
        super(windowToTrackChecker, dispatcher, logger);
    }

    @Override
    public String name() {
        return "LifecycleHandlerV2";
    }

    @Override
    public boolean supports(RawWinEvent event) {
        int eventId = event.getEvent();
        return eventId == EVENT_OBJECT_CREATE || eventId == EVENT_OBJECT_DESTROY;
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

        if (event.getEvent() == EVENT_OBJECT_CREATE) {
            logger.trace("[{}] dispatch action={} for hwnd={}", name(), WindowEventAction.Created, event.getHwnd());
            dispatch(WindowEventAction.Created);
        } else if (event.getEvent() == EVENT_OBJECT_DESTROY) {
            logger.trace("[{}] dispatch action={} for hwnd={}", name(), WindowEventAction.Closed, event.getHwnd());
            dispatch(WindowEventAction.Closed);
        }
    }
}
