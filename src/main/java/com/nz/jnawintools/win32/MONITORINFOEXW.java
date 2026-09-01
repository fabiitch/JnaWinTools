package com.nz.jnawintools.win32;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;

import static java.lang.foreign.ValueLayout.JAVA_CHAR;
import static java.lang.foreign.ValueLayout.JAVA_INT;

/**
 * {@code MONITORINFOEXW} (winuser.h): 104 bytes.
 *
 * <pre>
 *   offset 0   : DWORD cbSize        (4)
 *   offset 4   : RECT  rcMonitor     (16)
 *   offset 20  : RECT  rcWork        (16)
 *   offset 36  : DWORD dwFlags       (4)
 *   offset 40  : WCHAR szDevice[32]  (64)
 * </pre>
 */
public final class MONITORINFOEXW {

    /** {@code sizeof(MONITORINFOEXW)}. */
    public static final int SIZE = 104;

    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            JAVA_INT.withName("cbSize"),
            RECT.LAYOUT.withName("rcMonitor"),
            RECT.LAYOUT.withName("rcWork"),
            JAVA_INT.withName("dwFlags"),
            MemoryLayout.sequenceLayout(32, JAVA_CHAR).withName("szDevice")
    ).withName("MONITORINFOEXW");

    private static final VarHandle CB_SIZE =
            LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("cbSize"));

    private static final long RC_MONITOR_OFFSET =
            LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("rcMonitor"));
    private static final long SZ_DEVICE_OFFSET =
            LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("szDevice"));

    private MONITORINFOEXW() {
    }

    /**
     * Allocates a {@code MONITORINFOEXW} and pre-fills {@code cbSize}, as required by
     * {@code GetMonitorInfoW}.
     */
    public static MemorySegment allocate(SegmentAllocator allocator) {
        MemorySegment segment = allocator.allocate(LAYOUT);
        CB_SIZE.set(segment, 0L, SIZE);
        return segment;
    }

    public static MemorySegment rcMonitor(MemorySegment info) {
        return info.asSlice(RC_MONITOR_OFFSET, RECT.LAYOUT.byteSize());
    }

    public static String device(MemorySegment info) {
        return info.getString(SZ_DEVICE_OFFSET, StandardCharsets.UTF_16LE);
    }
}
