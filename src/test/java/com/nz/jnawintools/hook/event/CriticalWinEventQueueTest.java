package com.nz.jnawintools.hook.event;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CriticalWinEventQueueTest {

    @Test
    void publishThenDrainPreservesOrderAndPayload() {
        CriticalWinEventQueue queue = new CriticalWinEventQueue(8);

        queue.publish(0x8000, 0xAAAAL, 0, 0, 5, 100);
        queue.publish(0x8001, 0xBBBBL, 1, 2, 6, 200);

        List<Long> handles = new ArrayList<>();
        List<Integer> events = new ArrayList<>();
        int drained = queue.drainTo(e -> {
            handles.add(e.getHwnd());
            events.add(e.getEvent());
        }, 16);

        assertEquals(2, drained);
        assertEquals(List.of(0xAAAAL, 0xBBBBL), handles);
        assertEquals(List.of(0x8000, 0x8001), events);
        assertEquals(2, queue.getPublished());
        assertEquals(2, queue.getDrained());
    }

    @Test
    void reusesPooledBuffersAcrossManyCycles() {
        CriticalWinEventQueue queue = new CriticalWinEventQueue(4);

        for (int i = 1; i <= 500; i++) {
            queue.publish(i, i, 0, 0, 0, 0);
            final long expected = i;
            int n = queue.drainTo(e -> assertEquals(expected, e.getHwnd()), 4);
            assertEquals(1, n);
        }

        assertEquals(0, queue.getMissFree());
        assertEquals(0, queue.getMissReady());
        assertEquals(500, queue.getDrained());
    }
}
