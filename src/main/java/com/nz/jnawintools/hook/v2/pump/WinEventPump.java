package com.nz.jnawintools.hook.v2.pump;

import com.nz.jnawintools.hook.v2.WinEventRouter;
import com.nz.jnawintools.hook.v2.event.CriticalWinEventQueue;
import com.nz.jnawintools.hook.v2.event.LocationChangeBuffer;
import com.nz.jnawintools.hook.v2.handler.BaseWindowEventHandler;
import org.slf4j.Logger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

public class WinEventPump {

    private final CriticalWinEventQueue criticalQueue;
    private final LocationChangeBuffer locationBuffer;
    private final WinEventPumpThread pumpThread;
    private final WinEventRouter router;
    private final Logger logger;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private volatile Thread consumerThread;

    public WinEventPump(Logger logger,
                        CriticalWinEventQueue criticalQueue,
                        LocationChangeBuffer locationBuffer,
                        WinEventPumpThread pumpThread) {
        this.logger = logger;
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
                        logger.error("Critical routing failed", t);
                    }
                }, 1024);

                boolean drainedLocation = locationBuffer.drainTo(event -> {
                    try {
                        router.route(event);
                    } catch (Throwable t) {
                        logger.error("Location routing failed", t);
                    }
                });

                if (drainedCritical == 0 && !drainedLocation) {
                    // sommeil léger + sécurité
                    LockSupport.parkNanos(1_000_000L); // 1 ms
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