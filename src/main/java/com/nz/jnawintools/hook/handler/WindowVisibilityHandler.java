package com.nz.jnawintools.hook.handler;

import com.nz.jnawintools.hook.event.RawWinEvent;
import com.nz.jnawintools.hook.event.WindowEventAction;
import com.nz.jnawintools.hook.event.dispatch.AbstractEventDispatcher;
import com.nz.jnawintools.hook.window.WindowChecker;
import lombok.extern.slf4j.Slf4j;

import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_OBJECT_HIDE;
import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_OBJECT_SHOW;

@Slf4j
public class WindowVisibilityHandler extends BaseWindowEventHandler {

    public WindowVisibilityHandler(WindowChecker windowToTrackChecker,
                                   AbstractEventDispatcher<WindowEventAction> dispatcher) {
        super(windowToTrackChecker, dispatcher);
    }

    @Override
    public String name() {
        return "VisibilityHandler";
    }

    @Override
    public boolean supports(RawWinEvent event) {
        int eventId = event.getEvent();
        return eventId == EVENT_OBJECT_SHOW || eventId == EVENT_OBJECT_HIDE;
    }

    @Override
    public void handle(RawWinEvent event) {
        if (!event.isWindowObject()) {
            log.trace("[{}] ignored event={} (idObject={}, idChild={})", name(), event.getEvent(), event.getIdObject(), event.getIdChild());
            return;
        }

        String windowTitle = window64Helper.getName(event.getHwnd());
        if (!windowToTrackChecker.isWindow(event.getHwnd())) {
            log.trace("[{}] ignored event={} for non tracked window={}", name(), event.getEvent(), windowTitle);
            return;
        }

        if (window64Helper.isIconic(event.getHwnd())) {
            log.trace("[{}] ignored visibility event={} because window is iconic (hwnd={} title={})",
                    name(), event.getEvent(), event.getHwnd(), windowTitle);
            return;
        }

        if (event.getEvent() == EVENT_OBJECT_SHOW) {
            log.trace("[{}] tracked window show event for hwnd={} title={}", name(), event.getHwnd(), windowTitle);
        } else if (event.getEvent() == EVENT_OBJECT_HIDE) {
            log.trace("[{}] tracked window hide event for hwnd={} title={}", name(), event.getHwnd(), windowTitle);
        }
    }
}
