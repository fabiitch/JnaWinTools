package com.nz.jnawintools.hook.v2;

import org.jctools.queues.MpscUnboundedArrayQueue;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

public class WinEventPump {

    private final Logger logger;
    private final MpscUnboundedArrayQueue<RawWinEvent> queue = new MpscUnboundedArrayQueue<>(1024);
    private final WinEventRouter router = new WinEventRouter();
    private final WinEventPumpThread pumpThread;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread consumerThread;

    public WinEventPump(Logger logger, WinEventPumpThread pumpThread) {
        this.logger = logger;
        this.pumpThread = pumpThread;
    }

    public void registerHandler(com.nz.jnawintools.hook.v2.handler.BaseWindowEventHandler handler) {
        router.register(handler);
    }

    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        pumpThread.startAndWait();

        consumerThread = new Thread(() -> {
            while (running.get()) {
                RawWinEvent event = queue.relaxedPoll();
                if (event == null) {
                    Thread.onSpinWait();
                    continue;
                }

                try {
                    router.route(event);
                } catch (Throwable t) {
                    logger.error("Event routing failed", t);
                }
            }
        }, "WinEventConsumer");

        consumerThread.setDaemon(true);
        consumerThread.start();
    }

    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        pumpThread.shutdown();

        if (consumerThread != null) {
            consumerThread.interrupt();
        }
    }

    public MpscUnboundedArrayQueue<RawWinEvent> queue() {
        return queue;
    }
}