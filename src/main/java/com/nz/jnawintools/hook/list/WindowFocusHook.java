package com.nz.jnawintools.hook.list;

import com.nz.jnawintools.hook.event.WindowEventAction;
import com.nz.jnawintools.hook.event.dispatch.AbstractEventDispatcher;
import com.nz.jnawintools.hook.window.WindowChecker;
import org.slf4j.Logger;
import com.sun.jna.platform.win32.WinDef;

import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_SYSTEM_FOREGROUND;

public class WindowFocusHook extends BaseWindowHook {

    private volatile boolean hasFocus;

    public WindowFocusHook(WindowChecker windowToTrackChecker,
                            AbstractEventDispatcher<WindowEventAction> dispatcher,
                            Logger logger) {
        super(windowToTrackChecker, dispatcher, logger);
    }

    @Override
    public synchronized void start() {
        WinDef.HWND foreGroundWindow = window64Helper.getForeGroundWindow();
        hasFocus = windowToTrackChecker.isWindow(foreGroundWindow);
        logger.trace("[{}] initial focus state={} for hwnd={}", name(), hasFocus, foreGroundWindow);
        super.start();
    }

    @Override
    protected void onEvent(WinDef.DWORD event, WinDef.HWND hwnd,
                           WinDef.LONG idObject, WinDef.LONG idChild,
                           WinDef.DWORD dwEventThread, WinDef.DWORD dwmsEventTime) {
        if (idObject.intValue() != 0 || idChild.intValue() != 0) {
            logger.trace("[{}] ignored event={} (idObject={}, idChild={})", name(), event.intValue(), idObject.intValue(), idChild.intValue());
            return;
        }

        boolean trackedWindowHasFocus = windowToTrackChecker.isWindow(hwnd);
        logger.trace("[{}] event={} hwnd={} hasFocusBefore={} trackedWindowHasFocus={}",
                name(), event.intValue(), hwnd, hasFocus, trackedWindowHasFocus);

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

    @Override
    protected String name() {
        return "FocusHook";
    }

    @Override
    protected int eventMin() {
        return EVENT_SYSTEM_FOREGROUND;
    }

    @Override
    protected int eventMax() {
        return EVENT_SYSTEM_FOREGROUND;
    }


}
