package com.nz.jnawintools.hook;

import com.nz.jnawintools.hook.event.CriticalWinEventQueue;
import com.nz.jnawintools.hook.event.LocationChangeBuffer;
import com.nz.jnawintools.hook.event.WindowEventAction;
import com.nz.jnawintools.hook.event.dispatch.AbstractEventDispatcher;
import com.nz.jnawintools.hook.handler.WinEventRange;
import com.nz.jnawintools.hook.handler.WindowFocusHandler;
import com.nz.jnawintools.hook.handler.WindowLifecycleHandler;
import com.nz.jnawintools.hook.handler.WindowMoveHandler;
import com.nz.jnawintools.hook.pump.WinEventPump;
import com.nz.jnawintools.hook.pump.WinEventPumpThread;
import com.nz.jnawintools.hook.window.WindowChecker;
import lombok.Getter;

import java.util.List;
import java.util.function.Consumer;

import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_OBJECT_CREATE;
import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_OBJECT_LOCATIONCHANGE;
import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_SYSTEM_FOREGROUND;
import static com.nz.jnawintools.hook.cst.WinEventConstants.EVENT_SYSTEM_MINIMIZE_END;
import static com.nz.jnawintools.hook.cst.WinEventConstants.WINEVENT_OUTOFCONTEXT;
import static com.nz.jnawintools.hook.cst.WinEventConstants.WINEVENT_SKIPOWNPROCESS;

public class WindowHook {

    private static final int CRITICAL_QUEUE_CAPACITY = 8192;
    private static final int LOCATION_POOL_SIZE = 8;

    private final AbstractEventDispatcher<WindowEventAction> dispatcher;

    private final WindowFocusHandler focusHandler;
    private final WindowLifecycleHandler lifecycleHandler;
    private final WindowMoveHandler moveHandler;

    @Getter
    private final CriticalWinEventQueue criticalQueue;

    @Getter
    private final LocationChangeBuffer locationBuffer;
    private final WinEventPump pump;

    public WindowHook(WindowChecker windowChecker,
                      AbstractEventDispatcher<WindowEventAction> dispatcher) {
        this.dispatcher = dispatcher;

        this.focusHandler = new WindowFocusHandler(windowChecker, dispatcher);
        this.lifecycleHandler = new WindowLifecycleHandler(windowChecker, dispatcher);
        this.moveHandler = new WindowMoveHandler(windowChecker, dispatcher);

        this.criticalQueue = new CriticalWinEventQueue(CRITICAL_QUEUE_CAPACITY);
        this.locationBuffer = new LocationChangeBuffer(LOCATION_POOL_SIZE);

        WinEventPumpThread pumpThread = getWinEventPumpThread();

        this.pump = new WinEventPump(
                criticalQueue,
                locationBuffer,
                pumpThread
        );
        pumpThread.setPump(pump);

        this.pump.registerHandler(focusHandler);
        this.pump.registerHandler(lifecycleHandler);
        this.pump.registerHandler(moveHandler);
    }

    private WinEventPumpThread getWinEventPumpThread() {
        int flags = WINEVENT_OUTOFCONTEXT | WINEVENT_SKIPOWNPROCESS;

        WinEventPumpThread pumpThread = new WinEventPumpThread(
                "WinEventPumpThread",
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
        return pumpThread;
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
