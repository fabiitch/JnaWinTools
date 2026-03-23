package com.nz.jnawintools.hook.v2;

import com.nz.jnawintools.hook.event.WindowEventAction;
import com.nz.jnawintools.hook.event.dispatch.AbstractEventDispatcher;
import com.nz.jnawintools.hook.v2.handler.WindowFocusHandler;
import com.nz.jnawintools.hook.v2.handler.WindowLifecycleHandler;
import com.nz.jnawintools.hook.v2.handler.WindowMoveHandler;
import com.nz.jnawintools.hook.window.WindowChecker;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Consumer;

import static com.nz.jnawintools.hook.cst.WinEventConstants.*;

public class WindowHookV2 {

    private final Logger logger;
    private final AbstractEventDispatcher<WindowEventAction> dispatcher;

    private final WindowFocusHandler focusHandler;
    private final WindowLifecycleHandler lifecycleHandler;
    private final WindowMoveHandler moveHandler;

    private final WinEventPump pump;

    public WindowHookV2(WindowChecker windowChecker,
                        AbstractEventDispatcher<WindowEventAction> dispatcher,
                        Logger logger) {
        this.logger = logger;
        this.dispatcher = dispatcher;

        this.focusHandler = new WindowFocusHandler(windowChecker, dispatcher, logger);
        this.lifecycleHandler = new WindowLifecycleHandler(windowChecker, dispatcher, logger);
        this.moveHandler = new WindowMoveHandler(windowChecker, dispatcher, logger);

        int flags = WINEVENT_OUTOFCONTEXT | WINEVENT_SKIPOWNPROCESS;

        WinEventPumpThread pumpThread = new WinEventPumpThread(
                "WinEventPumpThread",
                logger,
                List.of(
                        new WinEventRange("SYSTEM", EVENT_SYSTEM_FOREGROUND, EVENT_SYSTEM_MINIMIZE_END, flags),
                        new WinEventRange("OBJECT", EVENT_OBJECT_CREATE, EVENT_OBJECT_LOCATIONCHANGE, flags)
                ),
                new org.jctools.queues.MpscUnboundedArrayQueue<>(1024)
        );

        this.pump = new WinEventPump(logger, pumpThread);
        this.pump.registerHandler(focusHandler);
        this.pump.registerHandler(lifecycleHandler);
        this.pump.registerHandler(moveHandler);
    }

    public void start() {
        focusHandler.init();
        pump.start();
    }

    public void stop() {
        pump.stop();
    }

    public void addListener(Consumer<WindowEventAction> listener) {
        dispatcher.addListener(listener);
    }

    public void removeListener(Consumer<WindowEventAction> listener) {
        dispatcher.removeListener(listener);
    }

    public void clearListeners() {
        dispatcher.clear();
    }
}