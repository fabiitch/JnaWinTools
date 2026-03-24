package com.nz.jnawintools.hook.handler;

import com.nz.jnawintools.hook.event.RawWinEvent;
import com.nz.jnawintools.hook.event.WindowEventAction;
import com.nz.jnawintools.hook.event.dispatch.AbstractEventDispatcher;
import com.nz.jnawintools.hook.window.WindowChecker;
import com.nz.jnawintools.window.Window64Helper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseWindowEventHandler {

    protected final WindowChecker windowToTrackChecker;
    protected final AbstractEventDispatcher<WindowEventAction> dispatcher;
    protected final Window64Helper window64Helper;

    protected BaseWindowEventHandler(WindowChecker windowToTrackChecker,
                                     AbstractEventDispatcher<WindowEventAction> dispatcher) {
        this.windowToTrackChecker = windowToTrackChecker;
        this.dispatcher = dispatcher;
        this.window64Helper = new Window64Helper(log);
    }

    public abstract String name();

    public abstract boolean supports(RawWinEvent event);

    public abstract void handle(RawWinEvent event);

    protected final void dispatch(WindowEventAction action) {
        try {
            dispatcher.dispatch(action);
        } catch (Throwable t) {
            log.error("[{}] dispatch failed", name(), t);
        }
    }
}
