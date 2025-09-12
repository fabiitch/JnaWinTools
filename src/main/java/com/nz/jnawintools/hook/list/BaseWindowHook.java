package com.nz.jnawintools.hook.list;

import com.nz.jnawintools.hook.WindowEventAction;
import com.nz.jnawintools.hook.events.AbstractEventDispatcher;
import com.nz.jnawintools.hook.window.WindowChecker;
import com.nz.jnawintools.log.JWTLogger;
import com.nz.jnawintools.log.WindowHookLogger;
import com.nz.jnawintools.window.Window64Helper;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;

import static com.nz.jnawintools.hook.cst.WinEventConstants.WINEVENT_OUTOFCONTEXT;
import static com.nz.jnawintools.hook.cst.WinEventConstants.WINEVENT_SKIPOWNPROCESS;

public abstract class BaseWindowHook {

    protected final WindowHookLogger logger;
    protected final WindowChecker windowToTrackChecker;

    protected final Window64Helper window64Helper;
    private final AbstractEventDispatcher<WindowEventAction> dispatcher;

    private WinUser.WinEventProc eventProc;
    private WinNT.HANDLE hookHandle;
    private volatile boolean started = false;

    public BaseWindowHook(WindowChecker windowToTrackChecker,
                           AbstractEventDispatcher<WindowEventAction> messageDispatcher,
                           JWTLogger logger) {
        this.windowToTrackChecker = windowToTrackChecker;
        this.dispatcher = messageDispatcher;
        this.logger = new WindowHookLogger(name() + "-" + windowToTrackChecker.getWindowName(), logger);
        this.window64Helper = new Window64Helper(logger);
    }

    protected abstract void onEvent(WinDef.DWORD event,
                                    WinDef.HWND hwnd,
                                    WinDef.LONG idObject,
                                    WinDef.LONG idChild,
                                    WinDef.DWORD dwEventThread,
                                    WinDef.DWORD dwmsEventTime);

    protected abstract String name();

    protected abstract int eventMin();

    protected abstract int eventMax();

    protected int hookFlags() {
        return WINEVENT_OUTOFCONTEXT | WINEVENT_SKIPOWNPROCESS;
    }

    protected final void dispatch(WindowEventAction action) {
        try {
            dispatcher.dispatch(action);
        } catch (Throwable t) {
            logger.error("[{}] dispatch failed: {}", name(), t.getMessage());
        }
    }

    /**
     * Démarre le hook (idempotent).
     */
    public synchronized void start() {
        if (started) {
            logger.debug("already started");
            return;
        }
        logger.debug("starting...");

        // Crée le callback et le garde en champ (réf forte)
        eventProc = (hWinEventHook, event, hwnd, idObject, idChild, dwEventThread, dwmsEventTime) -> {
            try {
                onEvent(event, hwnd, idObject, idChild, dwEventThread, dwmsEventTime);
            } catch (Throwable t) {
                logger.error("onEvent error: {}", t.getMessage());
            }
        };

        hookHandle = User32.INSTANCE.SetWinEventHook(
                eventMin(),
                eventMax(),
                null,
                eventProc,
                0, 0,
                hookFlags()
        );

        if (hookHandle == null) {
            int err = Kernel32.INSTANCE.GetLastError();
            eventProc = null; // rollback
            logger.error("SetWinEventHook failed. GetLastError={}", err);
            throw new IllegalStateException("SetWinEventHook failed: " + err);
        }

        logger.debug("hook installed handle=0x{} (min={}, max={}, flags=0x{})",
                Long.toHexString(Pointer.nativeValue(hookHandle.getPointer())),
                eventMin(), eventMax(), Integer.toHexString(hookFlags()));

        started = true;
    }

    public synchronized void stop() {
        if (!started) {
            logger.debug("already stopped");
            return;
        }
        logger.debug("stopping...");

        if (hookHandle != null) {
            boolean ok = User32.INSTANCE.UnhookWinEvent(hookHandle);
            logger.debug("UnhookWinEvent -> {}", ok);
            hookHandle = null;
        }
        eventProc = null;
        started = false;
    }
}
