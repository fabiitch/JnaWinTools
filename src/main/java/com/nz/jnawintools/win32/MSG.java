package com.nz.jnawintools.win32;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

/**
 * {@code MSG} (winuser.h) with the exact Windows x64 layout (48 bytes).
 *
 * <pre>
 *   offset 0  : HWND   hwnd      (8, pointer)
 *   offset 8  : UINT   message   (4)
 *   offset 12 : (padding 4)
 *   offset 16 : WPARAM wParam    (8)
 *   offset 24 : LPARAM lParam    (8)
 *   offset 32 : DWORD  time      (4)
 *   offset 36 : POINT  pt        (8 : x@36, y@40)
 *   offset 44 : DWORD  lPrivate  (4)
 * </pre>
 */
public final class MSG {

    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            ADDRESS.withName("hwnd"),
            JAVA_INT.withName("message"),
            MemoryLayout.paddingLayout(4),
            JAVA_LONG.withName("wParam"),
            JAVA_LONG.withName("lParam"),
            JAVA_INT.withName("time"),
            JAVA_INT.withName("pt_x"),
            JAVA_INT.withName("pt_y"),
            JAVA_INT.withName("lPrivate")
    ).withName("MSG");

    private MSG() {
    }

    public static MemorySegment allocate(SegmentAllocator allocator) {
        return allocator.allocate(LAYOUT);
    }
}
