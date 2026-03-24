package com.nz.jnawintools.hook.handler;

import com.nz.jnawintools.hook.event.RawWinEvent;
import com.nz.jnawintools.hook.event.WindowEventAction;
import com.nz.jnawintools.hook.event.dispatch.AbstractEventDispatcher;
import com.nz.jnawintools.hook.window.WindowChecker;
import com.sun.jna.platform.win32.WinDef;
import lombok.extern.slf4j.Slf4j;

import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_SYSTEM_FOREGROUND;

@Slf4j
public class WindowFocusHandler extends BaseWindowEventHandler {

    private volatile boolean hasFocus;

    public WindowFocusHandler(WindowChecker windowToTrackChecker,
                              AbstractEventDispatcher<WindowEventAction> dispatcher) {
        super(windowToTrackChecker, dispatcher);
    }

    public void init() {
        WinDef.HWND foregroundWindow = window64Helper.getForeGroundWindow();
        hasFocus = windowToTrackChecker.isWindow(foregroundWindow);
        log.trace("[{}] initial focus state={} for hwnd={}", name(), hasFocus, foregroundWindow);
    }

    @Override
    public String name() {
        return "FocusHandler";
    }

    @Override
    public boolean supports(RawWinEvent event) {
        return event.getEvent() == EVENT_SYSTEM_FOREGROUND;
    }

    @Override
    public void handle(RawWinEvent event) {
        boolean trackedWindowHasFocus = windowToTrackChecker.isWindow(event.getHwnd());
        log.trace("[{}] event={} hwnd={} hasFocusBefore={} trackedWindowHasFocus={}",
                name(), event.getEvent(), event.getHwnd(), hasFocus, trackedWindowHasFocus);

        WindowEventAction action = null;
        if (hasFocus && !trackedWindowHasFocus) {
            action = WindowEventAction.LooseFocus;
        } else if (!hasFocus && trackedWindowHasFocus) {
            action = WindowEventAction.GainFocus;
        }

        hasFocus = trackedWindowHasFocus;
        if (action != null) {
            log.trace("[{}] dispatch action={}", name(), action);
            dispatch(action);
        }
    }
}
