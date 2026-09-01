package com.nz.jnawintools.win32;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_INT;

/**
 * {@code RECT} (winuser.h): four {@code LONG} fields, 16 bytes.
 */
public final class RECT {

    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            JAVA_INT.withName("left"),
            JAVA_INT.withName("top"),
            JAVA_INT.withName("right"),
            JAVA_INT.withName("bottom")
    ).withName("RECT");

    private static final VarHandle LEFT = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("left"));
    private static final VarHandle TOP = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("top"));
    private static final VarHandle RIGHT = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("right"));
    private static final VarHandle BOTTOM = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("bottom"));

    private RECT() {
    }

    public static MemorySegment allocate(SegmentAllocator allocator) {
        return allocator.allocate(LAYOUT);
    }

    public static int left(MemorySegment rect) {
        return (int) LEFT.get(rect, 0L);
    }

    public static int top(MemorySegment rect) {
        return (int) TOP.get(rect, 0L);
    }

    public static int right(MemorySegment rect) {
        return (int) RIGHT.get(rect, 0L);
    }

    public static int bottom(MemorySegment rect) {
        return (int) BOTTOM.get(rect, 0L);
    }
}
