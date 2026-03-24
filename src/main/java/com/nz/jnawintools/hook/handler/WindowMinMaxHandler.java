package com.nz.jnawintools.hook.handler;

import com.nz.jnawintools.hook.event.RawWinEvent;
import com.nz.jnawintools.hook.event.WindowEventAction;
import com.nz.jnawintools.hook.event.dispatch.AbstractEventDispatcher;
import com.nz.jnawintools.hook.window.WindowChecker;
import lombok.extern.slf4j.Slf4j;

import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_SYSTEM_MINIMIZE_END;
import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_SYSTEM_MINIMIZE_START;

@Slf4j
public class WindowMinMaxHandler extends BaseWindowEventHandler {

    public WindowMinMaxHandler(WindowChecker windowToTrackChecker,
                               AbstractEventDispatcher<WindowEventAction> dispatcher) {
        super(windowToTrackChecker, dispatcher);
    }

    @Override
    public String name() {
        return "MinMaxHandler";
    }

    @Override
    public boolean supports(RawWinEvent event) {
        int eventId = event.getEvent();
        return eventId == EVENT_SYSTEM_MINIMIZE_START || eventId == EVENT_SYSTEM_MINIMIZE_END;
    }

    @Override
    public void handle(RawWinEvent event) {
        if (!event.isWindowObject()) {
            log.trace("[{}] ignored event={} (idObject={}, idChild={})",
                    name(), event.getEvent(), event.getIdObject(), event.getIdChild());
            return;
        }

        String windowTitle = window64Helper.getName(event.getHwnd());
        if (!windowToTrackChecker.isWindow(event.getHwnd())) {
            log.trace("[{}] ignored event={} for non tracked window={}", name(), event.getEvent(), windowTitle);
            return;
        }

        if (event.getEvent() == EVENT_SYSTEM_MINIMIZE_START) {
            log.trace("[{}] tracked window minimize start for hwnd={} title={}", name(), event.getHwnd(), windowTitle);
        } else if (event.getEvent() == EVENT_SYSTEM_MINIMIZE_END) {
            log.trace("[{}] tracked window minimize end for hwnd={} title={}", name(), event.getHwnd(), windowTitle);
        }
    }
}
