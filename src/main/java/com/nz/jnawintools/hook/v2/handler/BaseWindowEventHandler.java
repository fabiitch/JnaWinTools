package com.nz.jnawintools.hook.v2.handler;

import com.nz.jnawintools.hook.event.WindowEventAction;
import com.nz.jnawintools.hook.event.dispatch.AbstractEventDispatcher;
import com.nz.jnawintools.hook.v2.event.RawWinEvent;
import com.nz.jnawintools.hook.window.WindowChecker;
import com.nz.jnawintools.window.Window64Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseWindowEventHandler {

    protected final Logger logger;
    protected final WindowChecker windowToTrackChecker;
    protected final AbstractEventDispatcher<WindowEventAction> dispatcher;
    protected final Window64Helper window64Helper;

    protected BaseWindowEventHandler(WindowChecker windowToTrackChecker,
                                     AbstractEventDispatcher<WindowEventAction> dispatcher,
                                     Logger logger) {
        this.windowToTrackChecker = windowToTrackChecker;
        this.dispatcher = dispatcher;
        this.logger = logger != null ? logger : LoggerFactory.getLogger(getClass());
        this.window64Helper = new Window64Helper(this.logger);
    }

    public abstract String name();

    public abstract boolean supports(RawWinEvent event);

    public abstract void handle(RawWinEvent event);

    protected final void dispatch(WindowEventAction action) {
        try {
            dispatcher.dispatch(action);
        } catch (Throwable t) {
            logger.error("[{}] dispatch failed", name(), t);
        }
    }
}