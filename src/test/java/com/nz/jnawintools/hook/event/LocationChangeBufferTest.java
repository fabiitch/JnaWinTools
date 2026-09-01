package com.nz.jnawintools.hook.event;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocationChangeBufferTest {

    private static final int EVENT = 0x800B; // EVENT_OBJECT_LOCATIONCHANGE

    @Test
    void drainReturnsFalseWhenEmpty() {
        LocationChangeBuffer buffer = new LocationChangeBuffer(8);
        assertFalse(buffer.hasPending());
        assertFalse(buffer.drainTo(e -> {
            throw new AssertionError("must not be called");
        }));
    }

    @Test
    void coalescesToLatestEvent() {
        LocationChangeBuffer buffer = new LocationChangeBuffer(8);

        buffer.publish(EVENT, 0x1111L, 0, 0, 1, 10);
        buffer.publish(EVENT, 0x2222L, 0, 0, 1, 20);
        buffer.publish(EVENT, 0x3333L, 0, 0, 1, 30);

        assertTrue(buffer.hasPending());

        List<Long> seen = new ArrayList<>();
        assertTrue(buffer.drainTo(e -> seen.add(e.getHwnd())));

        assertEquals(List.of(0x3333L), seen); // only the most recent survives
        assertEquals(3, buffer.getPublished());
        assertEquals(2, buffer.getOverwritten());
        assertEquals(1, buffer.getDrained());
        assertFalse(buffer.hasPending());
    }

    @Test
    void publishDrainCycleReusesBuffersWithoutAllocationFailure() {
        LocationChangeBuffer buffer = new LocationChangeBuffer(2);

        for (int i = 1; i <= 1000; i++) {
            buffer.publish(EVENT, i, 0, 0, 1, i);
            final long expected = i;
            assertTrue(buffer.drainTo(e -> assertEquals(expected, e.getHwnd())));
        }

        assertEquals(0, buffer.getMissFree());
        assertEquals(1000, buffer.getDrained());
    }

    @Test
    void keepsLatestEventInBurstAfterACompletedDrain() {
        LocationChangeBuffer buffer = new LocationChangeBuffer(2);

        buffer.publish(EVENT, 0x1000L, 0, 0, 1, 1);
        assertTrue(buffer.drainTo(e -> assertEquals(0x1000L, e.getHwnd())));

        buffer.publish(EVENT, 0x2000L, 0, 0, 1, 2);
        buffer.publish(EVENT, 0x3000L, 0, 0, 1, 3);

        assertTrue(buffer.drainTo(e -> assertEquals(0x3000L, e.getHwnd())));
        assertEquals(0, buffer.getMissFree());
        assertEquals(1, buffer.getOverwritten());
    }

    @Test
    void keepsLatestWithMinimumPoolWhileConsumerHoldsOtherSlot() throws InterruptedException {
        LocationChangeBuffer buffer = new LocationChangeBuffer(2);
        CountDownLatch consuming = new CountDownLatch(1);
        CountDownLatch releaseConsumer = new CountDownLatch(1);

        buffer.publish(EVENT, 0x1000L, 0, 0, 1, 1);
        Thread consumer = new Thread(() -> buffer.drainTo(event -> {
            consuming.countDown();
            try {
                assertTrue(releaseConsumer.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError(e);
            }
        }));
        consumer.start();
        assertTrue(consuming.await(5, TimeUnit.SECONDS));

        buffer.publish(EVENT, 0x2000L, 0, 0, 1, 2);
        buffer.publish(EVENT, 0x3000L, 0, 0, 1, 3);

        releaseConsumer.countDown();
        consumer.join(5_000);
        assertFalse(consumer.isAlive());
        assertTrue(buffer.drainTo(event -> assertEquals(0x3000L, event.getHwnd())));
        assertEquals(0, buffer.getMissFree());
    }
}
