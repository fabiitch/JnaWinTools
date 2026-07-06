package com.nz.jnawintools.hook.pump;

import com.nz.jnawintools.hook.WinEventHandlerRouter;
import com.nz.jnawintools.hook.event.CriticalWinEventQueue;
import com.nz.jnawintools.hook.event.LocationChangeBuffer;
import com.nz.jnawintools.hook.event.RawWinEvent;
import com.nz.jnawintools.hook.handler.BaseWindowEventHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

@Slf4j
public class WinEventPump implements Runnable {

    private final CriticalWinEventQueue criticalQueue;
    private final LocationChangeBuffer locationBuffer;
    @Getter
    private final WinEventPumpThread pumpThread;
    private final WinEventHandlerRouter router;
    private final Consumer<RawWinEvent> criticalConsumer = new CriticalEventConsumer();
    private final Consumer<RawWinEvent> locationConsumer = new LocationEventConsumer();

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Getter
    private volatile Thread consumerThread;

    public WinEventPump(CriticalWinEventQueue criticalQueue,
                        LocationChangeBuffer locationBuffer,
                        WinEventPumpThread pumpThread) {
        this.criticalQueue = criticalQueue;
        this.locationBuffer = locationBuffer;
        this.pumpThread = pumpThread;
        this.router = new WinEventHandlerRouter();
    }

    public void signalWork() {
        Thread t = consumerThread;
        if (t != null) {
            LockSupport.unpark(t);
        }
    }

    public void start() {
        if (!running.compareAndSet(false, true)) return;

        pumpThread.setPump(this);
        pumpThread.startAndWait();

        consumerThread = new Thread(this, "WinEventConsumer");
        consumerThread.setDaemon(true);
        consumerThread.start();
    }

    @Override
    public void run() {
        while (running.get()) {
            int drainedCritical = criticalQueue.drainTo(criticalConsumer, 1024);
            boolean drainedLocation = locationBuffer.drainTo(locationConsumer);

            if (drainedCritical == 0 && !drainedLocation) {
                LockSupport.parkNanos(1_000_000L);
            }
        }
    }

    public void stop() {
        if (!running.compareAndSet(true, false)) return;

        pumpThread.shutdown();

        if (consumerThread != null) {
            LockSupport.unpark(consumerThread);
        }
    }

    public void registerHandler(BaseWindowEventHandler handler) {
        router.register(handler);
    }

    private final class CriticalEventConsumer implements Consumer<RawWinEvent> {
        @Override
        public void accept(RawWinEvent event) {
            try {
                router.route(event);
            } catch (Throwable t) {
                log.error("Critical routing failed", t);
            }
        }
    }

    private final class LocationEventConsumer implements Consumer<RawWinEvent> {
        @Override
        public void accept(RawWinEvent event) {
            try {
                router.route(event);
            } catch (Throwable t) {
                log.error("Location routing failed", t);
            }
        }
    }
}
