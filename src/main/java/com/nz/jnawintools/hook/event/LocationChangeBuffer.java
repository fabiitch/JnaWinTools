package com.nz.jnawintools.hook.event;

import com.sun.jna.platform.win32.WinDef;
import org.jctools.queues.SpscArrayQueue;

import java.util.function.Consumer;

public final class LocationChangeBuffer {

    private final SpscArrayQueue<RawWinEvent> freeQ;
    private RawWinEvent latest;

    private int missFree, published, overwritten, drained;

    public LocationChangeBuffer(int poolSize) {
        if (poolSize < 2) {
            throw new IllegalArgumentException("poolSize must be >= 2");
        }

        this.freeQ = new SpscArrayQueue<>(poolSize);

        for (int i = 0; i < poolSize; i++) {
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

        RawWinEvent previous = latest;
        latest = e;

        if (previous != null) {
            previous.clear();
            freeQ.offer(previous);
            overwritten++;
        }

        published++;
    }

    public boolean drainTo(Consumer<RawWinEvent> consumer) {
        RawWinEvent e = latest;
        if (e == null) {
            return false;
        }

        latest = null;

        try {
            consumer.accept(e);
        } finally {
            e.clear();
            freeQ.offer(e);
            drained++;
        }

        return true;
    }

    public boolean hasPending() {
        return latest != null;
    }

}