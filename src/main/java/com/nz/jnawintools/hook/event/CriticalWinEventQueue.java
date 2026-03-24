package com.nz.jnawintools.hook.event;

import com.sun.jna.platform.win32.WinDef;
import lombok.Getter;
import org.jctools.queues.SpscArrayQueue;

import java.util.function.Consumer;

public final class CriticalWinEventQueue {

    private final SpscArrayQueue<RawWinEvent> freeQ;
    private final SpscArrayQueue<RawWinEvent> readyQ;

    @Getter
    private int missFree,missReady, published, drained;

    public CriticalWinEventQueue(int capacity) {
        if (capacity < 2) {
            throw new IllegalArgumentException("capacity must be >= 2");
        }

        this.freeQ = new SpscArrayQueue<>(capacity);
        this.readyQ = new SpscArrayQueue<>(capacity);

        for (int i = 0; i < capacity; i++) {
            freeQ.offer(new RawWinEvent());
        }
    }

    public void publish(int event,
                        WinDef.HWND hwnd,
                        int idObject,
                        int idChild,
                        int eventThread,
                        int eventTime) {
        RawWinEvent e = freeQ.poll();
        if (e == null) {
            missFree++;
            return;
        }

        e.set(event, hwnd, idObject, idChild, eventThread, eventTime);

        if (!readyQ.offer(e)) {
            e.clear();
            freeQ.offer(e);
            missReady++;
            return;
        }

        published++;
    }

    public int drainTo(Consumer<RawWinEvent> consumer, int budget) {
        int count = 0;

        for (int i = 0; i < budget; i++) {
            RawWinEvent e = readyQ.poll();
            if (e == null) {
                break;
            }

            try {
                consumer.accept(e);
            } finally {
                e.clear();
                freeQ.offer(e);
                drained++;
            }

            count++;
        }

        return count;
    }

}