package com.nz.jnawintools.hook.list;

import com.nz.jnawintools.hook.WindowEventAction;
import com.nz.jnawintools.hook.events.AbstractEventDispatcher;
import com.nz.jnawintools.log.JnaWinToolsLogger;
import com.nz.jnawintools.log.WindowHookLogger;
import com.nz.jnawintools.window.Window64Helper;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinNT;

public abstract class BaseWindowHook {

    protected final WindowHookLogger logger;
    private WinNT.HANDLE hook;
    protected final String WINDOW_TRACKED;
    protected final Window64Helper window64Helper;
    private final AbstractEventDispatcher<WindowEventAction> messageDispatcher;

    public BaseWindowHook(String WINDOW_TRACKED,
                          AbstractEventDispatcher<WindowEventAction> messageDispatcher,
                          JnaWinToolsLogger logger) {
        this.WINDOW_TRACKED = WINDOW_TRACKED;
        this.messageDispatcher = messageDispatcher;
        this.logger = new WindowHookLogger(name() + "-" + WINDOW_TRACKED, logger);
        this.window64Helper = new Window64Helper(logger);
        this.hook = buildHook();
    }

    protected void dispatch(WindowEventAction action) {
        logger.debug("Dispatch action={}", action);
        messageDispatcher.dispatch(action);
    }

    protected abstract String name();

    public void startHook() {
        this.buildHook();
        if (hook != null) {
            this.logger.log("Hook activated 🎯🎯", WINDOW_TRACKED);
        } else {
            this.logger.error("Fail to Hook window ❌❌", WINDOW_TRACKED);
        }
    }

    protected abstract WinNT.HANDLE buildHook();

    public void dispose() {
        if (hook != null) {
            User32.INSTANCE.UnhookWinEvent(hook);
            logger.log("End hook app {}", WINDOW_TRACKED);
        }
    }
}
