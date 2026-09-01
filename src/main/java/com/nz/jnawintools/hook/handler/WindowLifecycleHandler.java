package com.nz.jnawintools.hook.handler;

import com.nz.jnawintools.hook.event.RawWinEvent;
import com.nz.jnawintools.hook.event.WindowEventAction;
import com.nz.jnawintools.hook.event.dispatch.AbstractEventDispatcher;
import com.nz.jnawintools.hook.window.WindowChecker;
import lombok.extern.slf4j.Slf4j;

import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_OBJECT_CREATE;
import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_OBJECT_DESTROY;
import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_OBJECT_NAMECHANGE;

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
        return eventId == EVENT_OBJECT_CREATE
                || eventId == EVENT_OBJECT_DESTROY
                || eventId == EVENT_OBJECT_NAMECHANGE;
    }

    @Override
    public void handle(RawWinEvent event) {
        if (!event.isWindowObject()) {
            if (log.isTraceEnabled()) {
                log.trace("[{}] ignored event={} (idObject={}, idChild={})",
                        name(), event.getEvent(), event.getIdObject(), event.getIdChild());
            }
            return;
        }

        int eventId = event.getEvent();
        long hwnd = event.getHwnd();
        if (eventId == EVENT_OBJECT_NAMECHANGE) {
            windowToTrackChecker.invalidate(hwnd);
            return;
        }
        if (eventId == EVENT_OBJECT_CREATE) {
            windowToTrackChecker.invalidate(hwnd);
        }

        if (!windowToTrackChecker.isWindow(hwnd)) {
            if (log.isTraceEnabled()) {
                log.trace("[{}] ignored event={} for non tracked hwnd={}", name(), eventId, hwnd);
            }
            return;
        }

        if (eventId == EVENT_OBJECT_CREATE) {
            if (log.isTraceEnabled()) {
                log.trace("[{}] dispatch action={} for hwnd={}", name(), WindowEventAction.Created, hwnd);
            }
            dispatch(WindowEventAction.Created);
        } else if (eventId == EVENT_OBJECT_DESTROY) {
            if (log.isTraceEnabled()) {
                log.trace("[{}] dispatch action={} for hwnd={}", name(), WindowEventAction.Closed, hwnd);
            }
            dispatch(WindowEventAction.Closed);
            windowToTrackChecker.invalidate(hwnd);
        }
    }
}
