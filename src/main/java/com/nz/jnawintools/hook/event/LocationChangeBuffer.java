package com.nz.jnawintools.hook.event;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Single-slot coalescing buffer for {@code EVENT_OBJECT_LOCATIONCHANGE}: only the most recent event
 * is retained, older ones are dropped.
 *
 * <p>The previous implementation exposed the {@code latest} field through a plain (non-volatile)
 * reference shared between the producer (WinEvent thread) and the consumer, and reused a
 * {@code SpscArrayQueue} as a free-list with two independent producers &mdash; both a data race and
 * an SPSC violation. This version uses an atomic pending slot and a lock-free free-list. It is safe
 * for one publishing thread and one consuming thread, retains the configured pool size and never
 * allocates per event.
 */
public final class LocationChangeBuffer {

    private static final class Slot {
        private final RawWinEvent event = new RawWinEvent();
        private Slot next;
    }

    /** The latest published event awaiting consumption ({@code null} once drained). */
    private final AtomicReference<Slot> pending = new AtomicReference<>();

    /** Reusable slots returned by both threads and acquired only by the publisher. */
    private final AtomicReference<Slot> freeHead = new AtomicReference<>();

    @Getter
    private int missFree, published, overwritten, drained;

    public LocationChangeBuffer(int poolSize) {
        if (poolSize < 2) {
            throw new IllegalArgumentException("poolSize must be >= 2");
        }
        Slot head = null;
        for (int i = 0; i < poolSize; i++) {
            Slot slot = new Slot();
            slot.next = head;
            head = slot;
        }
        freeHead.set(head);
    }

    /**
     * Publishes the latest event, coalescing with any not-yet-consumed one. Called by the single
     * producer (WinEvent) thread only.
     */
    public void publish(int event,
                        long hwnd,
                        int idObject,
                        int idChild,
                        int eventThread,
                        int eventTime) {
        Slot slot = acquire();
        if (slot == null) {
            // With the minimum two-slot pool, one slot can be in the consumer while the other is
            // pending. Reclaim that pending (older) event before dropping the new one.
            slot = pending.getAndSet(null);
            if (slot != null) {
                overwritten++;
            } else {
                // The consumer may have taken pending and returned its previous slot meanwhile.
                slot = acquire();
                if (slot == null) {
                    missFree++;
                    return;
                }
            }
        }

        slot.event.set(event, hwnd, idObject, idChild, eventThread, eventTime);

        Slot previous = pending.getAndSet(slot);
        if (previous != null) {
            release(previous);
            overwritten++;
        }
        published++;
    }

    /**
     * Drains the latest event (if any). Called by the single consumer thread only.
     */
    public boolean drainTo(Consumer<RawWinEvent> consumer) {
        Slot slot = pending.getAndSet(null);
        if (slot == null) {
            return false;
        }

        try {
            consumer.accept(slot.event);
        } finally {
            release(slot);
            drained++;
        }

        return true;
    }

    public boolean hasPending() {
        return pending.get() != null;
    }

    private Slot acquire() {
        Slot head;
        Slot next;
        do {
            head = freeHead.get();
            if (head == null) {
                return null;
            }
            next = head.next;
        } while (!freeHead.compareAndSet(head, next));
        head.next = null;
        return head;
    }

    private void release(Slot slot) {
        slot.event.clear();
        Slot head;
        do {
            head = freeHead.get();
            slot.next = head;
        } while (!freeHead.compareAndSet(head, slot));
    }
}
