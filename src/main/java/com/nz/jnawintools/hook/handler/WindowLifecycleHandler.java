package com.nz.jnawintools.hook.handler;

import com.nz.jnawintools.hook.event.RawWinEvent;
import com.nz.jnawintools.hook.event.WindowEventAction;
import com.nz.jnawintools.hook.event.dispatch.AbstractEventDispatcher;
import com.nz.jnawintools.hook.window.WindowChecker;
import lombok.extern.slf4j.Slf4j;

import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_OBJECT_CREATE;
import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_OBJECT_DESTROY;

@Slf4j
public class WindowLifecycleHandler extends BaseWindowEventHandler {

    public WindowLifecycleHandler(WindowChecker windowToTrackChecker,
                                  AbstractEventDispatcher<WindowEventAction> dispatcher) {
        super(windowToTrackChecker, dispatcher);
    }

    @Override
    public String name() {
        return "LifecycleHandler";
    }

    @Override
    public boolean supports(RawWinEvent event) {
        int eventId = event.getEvent();
        return eventId == EVENT_OBJECT_CREATE || eventId == EVENT_OBJECT_DESTROY;
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

        if (event.getEvent() == EVENT_OBJECT_CREATE) {
            log.trace("[{}] dispatch action={} for hwnd={}", name(), WindowEventAction.Created, event.getHwnd());
            dispatch(WindowEventAction.Created);
        } else if (event.getEvent() == EVENT_OBJECT_DESTROY) {
            log.trace("[{}] dispatch action={} for hwnd={}", name(), WindowEventAction.Closed, event.getHwnd());
            dispatch(WindowEventAction.Closed);
        }
    }
}
