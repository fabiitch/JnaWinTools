package com.nz.jnawintools.hook.v2.handler;

import com.nz.jnawintools.hook.event.WindowEventAction;
import com.nz.jnawintools.hook.event.dispatch.AbstractEventDispatcher;
import com.nz.jnawintools.hook.v2.event.RawWinEvent;
import com.nz.jnawintools.hook.window.WindowChecker;
import com.sun.jna.platform.win32.WinDef;
import org.slf4j.Logger;

import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_SYSTEM_FOREGROUND;

public class WindowFocusHandlerV2 extends BaseWindowEventHandler {

    private volatile boolean hasFocus;

    public WindowFocusHandlerV2(WindowChecker windowToTrackChecker,
                                AbstractEventDispatcher<WindowEventAction> dispatcher,
                                Logger logger) {
        super(windowToTrackChecker, dispatcher, logger);
    }

    public void init() {
        WinDef.HWND foregroundWindow = window64Helper.getForeGroundWindow();
        hasFocus = windowToTrackChecker.isWindow(foregroundWindow);
        logger.trace("[{}] initial focus state={} for hwnd={}", name(), hasFocus, foregroundWindow);
    }

    @Override
    public String name() {
        return "FocusHandlerV2";
    }

    @Override
    public boolean supports(RawWinEvent event) {
        return event.getEvent() == EVENT_SYSTEM_FOREGROUND;
    }

    @Override
    public void handle(RawWinEvent event) {
        boolean trackedWindowHasFocus = windowToTrackChecker.isWindow(event.getHwnd());
        logger.trace("[{}] event={} hwnd={} hasFocusBefore={} trackedWindowHasFocus={}",
                name(), event.getEvent(), event.getHwnd(), hasFocus, trackedWindowHasFocus);

        WindowEventAction action = null;
        if (hasFocus && !trackedWindowHasFocus) {
            action = WindowEventAction.LooseFocus;
        } else if (!hasFocus && trackedWindowHasFocus) {
            action = WindowEventAction.GainFocus;
        }

        hasFocus = trackedWindowHasFocus;
        if (action != null) {
            logger.trace("[{}] dispatch action={}", name(), action);
            dispatch(action);
        }
    }
}
