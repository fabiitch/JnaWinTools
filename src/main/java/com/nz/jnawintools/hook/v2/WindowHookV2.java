package com.nz.jnawintools.hook.v2;

import com.nz.jnawintools.hook.cst.WinEventConstants;
import com.nz.jnawintools.hook.event.WindowEventAction;
import com.nz.jnawintools.hook.event.dispatch.AbstractEventDispatcher;
import com.nz.jnawintools.hook.v2.event.CriticalWinEventQueue;
import com.nz.jnawintools.hook.v2.event.LocationChangeBuffer;
import com.nz.jnawintools.hook.v2.handler.WinEventRange;
import com.nz.jnawintools.hook.v2.handler.WindowFocusHandlerV2;
import com.nz.jnawintools.hook.v2.handler.WindowLifecycleHandlerV2;
import com.nz.jnawintools.hook.v2.handler.WindowMoveHandlerV2;
import com.nz.jnawintools.hook.v2.pump.WinEventPump;
import com.nz.jnawintools.hook.v2.pump.WinEventPumpThread;
import com.nz.jnawintools.hook.window.WindowChecker;
import lombok.Getter;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Consumer;

import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_OBJECT_CREATE;
import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_OBJECT_LOCATIONCHANGE;
import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_SYSTEM_FOREGROUND;
import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_SYSTEM_MINIMIZE_END;
import static com.nz.jnawintools.hook.cst.WinEventConstants.WINEVENT_OUTOFCONTEXT;
import static com.nz.jnawintools.hook.cst.WinEventConstants.WINEVENT_SKIPOWNPROCESS;

public class WindowHookV2 {

    private static final int CRITICAL_QUEUE_CAPACITY = 8192;
    private static final int LOCATION_POOL_SIZE = 8;

    private final Logger logger;
    private final AbstractEventDispatcher<WindowEventAction> dispatcher;

    private final WindowFocusHandlerV2 focusHandler;
    private final WindowLifecycleHandlerV2 lifecycleHandler;
    private final WindowMoveHandlerV2 moveHandler;

    @Getter
    private final CriticalWinEventQueue criticalQueue;

    @Getter
    private final LocationChangeBuffer locationBuffer;
    private final WinEventPump pump;

    public WindowHookV2(WindowChecker windowChecker,
                        AbstractEventDispatcher<WindowEventAction> dispatcher,
                        Logger logger) {
        this.logger = logger;
        this.dispatcher = dispatcher;

        this.focusHandler = new WindowFocusHandlerV2(windowChecker, dispatcher, logger);
        this.lifecycleHandler = new WindowLifecycleHandlerV2(windowChecker, dispatcher, logger);
        this.moveHandler = new WindowMoveHandlerV2(windowChecker, dispatcher, logger);

        this.criticalQueue = new CriticalWinEventQueue(CRITICAL_QUEUE_CAPACITY);
        this.locationBuffer = new LocationChangeBuffer(LOCATION_POOL_SIZE);

        int flags = WINEVENT_OUTOFCONTEXT | WINEVENT_SKIPOWNPROCESS;

        WinEventPumpThread pumpThread = new WinEventPumpThread(
                "WinEventPumpThread",
                logger,
                List.of(
                        new WinEventRange(
                                "SYSTEM",
                                EVENT_SYSTEM_FOREGROUND,
                                EVENT_SYSTEM_MINIMIZE_END,
                                flags
                        ),
                        new WinEventRange(
                                "OBJECT",
                                EVENT_OBJECT_CREATE,
                                EVENT_OBJECT_LOCATIONCHANGE,
                                flags
                        )
                ),
                criticalQueue,
                locationBuffer
        );

        this.pump = new WinEventPump(
                logger,
                criticalQueue,
                locationBuffer,
                pumpThread
        );
        pumpThread.setPump(pump);

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