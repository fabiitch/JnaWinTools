package com.nz.jnawintools.hook.pump;

import com.nz.jnawintools.hook.WinEventRouter;
import com.nz.jnawintools.hook.event.CriticalWinEventQueue;
import com.nz.jnawintools.hook.event.LocationChangeBuffer;
import com.nz.jnawintools.hook.handler.BaseWindowEventHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

@Slf4j
public class WinEventPump {

    private final CriticalWinEventQueue criticalQueue;
    private final LocationChangeBuffer locationBuffer;
    private final WinEventPumpThread pumpThread;
    private final WinEventRouter router;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private volatile Thread consumerThread;

    public WinEventPump(CriticalWinEventQueue criticalQueue,
                        LocationChangeBuffer locationBuffer,
                        WinEventPumpThread pumpThread) {
        this.criticalQueue = criticalQueue;
        this.locationBuffer = locationBuffer;
        this.pumpThread = pumpThread;
        this.router = new WinEventRouter();
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

        consumerThread = new Thread(() -> {
            while (running.get()) {

                int drainedCritical = criticalQueue.drainTo(event -> {
                    try {
                        router.route(event);
                    } catch (Throwable t) {
                        log.error("Critical routing failed", t);
                    }
                }, 1024);

                boolean drainedLocation = locationBuffer.drainTo(event -> {
                    try {
                        router.route(event);
                    } catch (Throwable t) {
                        log.error("Location routing failed", t);
                    }
                });

                if (drainedCritical == 0 && !drainedLocation) {
                    LockSupport.parkNanos(1_000_000L);
                }
            }
        }, "WinEventConsumer");

        consumerThread.setDaemon(true);
        consumerThread.start();
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
}
