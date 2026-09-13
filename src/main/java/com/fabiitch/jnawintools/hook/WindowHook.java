package com.fabiitch.jnawintools.hook;

import com.fabiitch.jnawintools.hook.event.CriticalWinEventQueue;
import com.fabiitch.jnawintools.hook.event.LocationChangeBuffer;
import com.fabiitch.jnawintools.hook.event.WindowEventAction;
import com.fabiitch.jnawintools.hook.event.dispatch.AbstractEventDispatcher;
import com.fabiitch.jnawintools.hook.handler.WinEventRange;
import com.fabiitch.jnawintools.hook.handler.WindowFocusHandler;
import com.fabiitch.jnawintools.hook.handler.WindowLifecycleHandler;
import com.fabiitch.jnawintools.hook.handler.WindowMoveHandler;
import com.fabiitch.jnawintools.hook.pump.WinEventPump;
import com.fabiitch.jnawintools.hook.pump.WinEventPumpThread;
import com.fabiitch.jnawintools.hook.window.WindowChecker;
import lombok.Getter;

import java.util.List;
import java.util.function.Consumer;

import static com.fabiitch.jnawintools.hook.cst.WinEventConstants.*;

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
                                EVENT_OBJECT_STATECHANGE,
                                flags
                        ),
                        new WinEventRange(
                                "Move",
                                EVENT_SYSTEM_MOVESIZESTART,
                                EVENT_SYSTEM_MOVESIZEEND,
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

    public Thread getHookThread() {
        return pump.getPumpThread();
    }

    public Thread getConsumerThread() {
        return pump.getConsumerThread();
    }
}
