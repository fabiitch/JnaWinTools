package com.nz.jnawintools.win32;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_INT;

/**
 * {@code POINT} (windef.h): two {@code LONG} fields, 8 bytes.
 */
public final class POINT {

    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            JAVA_INT.withName("x"),
            JAVA_INT.withName("y")
    ).withName("POINT");

    private static final VarHandle X = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("x"));
    private static final VarHandle Y = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("y"));

    private POINT() {
    }

    public static MemorySegment allocate(SegmentAllocator allocator) {
        return allocator.allocate(LAYOUT);
    }

    public static int x(MemorySegment point) {
        return (int) X.get(point, 0L);
    }

    public static int y(MemorySegment point) {
        return (int) Y.get(point, 0L);
    }

    public static void set(MemorySegment point, int x, int y) {
        X.set(point, 0L, x);
        Y.set(point, 0L, y);
    }
}
