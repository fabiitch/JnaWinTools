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

    private static final int CRITICAL_DRAIN_BUDGET = 1024;
    private static final long TERMINATION_TIMEOUT_MILLIS = 5_000L;

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

    /**
     * Wakes the consumer thread. Called from the WinEvent callback. Uses {@code unpark}: if the
     * consumer is not parked yet, the permit is retained so the next {@code park} returns
     * immediately &mdash; no wakeups are lost, and there is no busy polling.
     */
    public void signalWork() {
        Thread t = consumerThread;
        if (t != null) {
            LockSupport.unpark(t);
        }
    }

    public synchronized void start() {
        if (!running.compareAndSet(false, true)) return;

        pumpThread.setPump(this);
        try {
            pumpThread.startAndWait();
        } catch (RuntimeException | Error failure) {
            running.set(false);
            throw failure;
        }

        consumerThread = new Thread(this, "WinEventConsumer");
        consumerThread.setDaemon(true);
        consumerThread.start();
    }

    @Override
    public void run() {
        while (running.get()) {
            boolean didWork = drainOnce();
            if (!didWork && running.get()) {
                // Nothing to process: block until the producer signals more work. Re-checking
                // running above/below the park keeps stop() from being missed.
                LockSupport.park(this);
            }
        }
        // Drain anything left behind so pending events are not lost at shutdown.
        drainOnce();
    }

    private boolean drainOnce() {
        int drainedCritical = criticalQueue.drainTo(criticalConsumer, CRITICAL_DRAIN_BUDGET);
        boolean drainedLocation = locationBuffer.drainTo(locationConsumer);
        return drainedCritical > 0 || drainedLocation;
    }

    public synchronized void stop() {
        if (!running.get()) return;

        pumpThread.shutdown();
        awaitTermination(pumpThread);

        running.set(false);
        Thread t = consumerThread;
        if (t != null) {
            LockSupport.unpark(t);
            awaitTermination(t);
        }
    }

    private static void awaitTermination(Thread thread) {
        if (thread == Thread.currentThread()) {
            return;
        }
        try {
            thread.join(TERMINATION_TIMEOUT_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for " + thread.getName(), e);
        }
        if (thread.isAlive()) {
            throw new IllegalStateException(thread.getName() + " did not stop within "
                    + TERMINATION_TIMEOUT_MILLIS + " ms");
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
