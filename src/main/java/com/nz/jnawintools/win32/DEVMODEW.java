package com.nz.jnawintools.win32;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_CHAR;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_SHORT;

/**
 * {@code DEVMODEW} (wingdi.h) with the classic 220-byte layout used by display enumeration.
 *
 * <p>The display-specific variant of the anonymous unions is modelled (POINTL {@code dmPosition}
 * plus {@code dmDisplayOrientation}/{@code dmDisplayFixedOutput}), which is what
 * {@code EnumDisplaySettingsW} fills for monitors.
 */
public final class DEVMODEW {

    /** Classic {@code sizeof(DEVMODEW)}. */
    public static final int SIZE = 220;

    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            MemoryLayout.sequenceLayout(32, JAVA_CHAR).withName("dmDeviceName"), // 0
            JAVA_SHORT.withName("dmSpecVersion"),                                // 64
            JAVA_SHORT.withName("dmDriverVersion"),                              // 66
            JAVA_SHORT.withName("dmSize"),                                       // 68
            JAVA_SHORT.withName("dmDriverExtra"),                                // 70
            JAVA_INT.withName("dmFields"),                                       // 72
            JAVA_INT.withName("dmPositionX"),                                    // 76
            JAVA_INT.withName("dmPositionY"),                                    // 80
            JAVA_INT.withName("dmDisplayOrientation"),                           // 84
            JAVA_INT.withName("dmDisplayFixedOutput"),                           // 88
            JAVA_SHORT.withName("dmColor"),                                      // 92
            JAVA_SHORT.withName("dmDuplex"),                                     // 94
            JAVA_SHORT.withName("dmYResolution"),                               // 96
            JAVA_SHORT.withName("dmTTOption"),                                   // 98
            JAVA_SHORT.withName("dmCollate"),                                    // 100
            MemoryLayout.sequenceLayout(32, JAVA_CHAR).withName("dmFormName"),   // 102
            JAVA_SHORT.withName("dmLogPixels"),                                  // 166
            JAVA_INT.withName("dmBitsPerPel"),                                   // 168
            JAVA_INT.withName("dmPelsWidth"),                                    // 172
            JAVA_INT.withName("dmPelsHeight"),                                   // 176
            JAVA_INT.withName("dmDisplayFlags"),                                 // 180
            JAVA_INT.withName("dmDisplayFrequency"),                             // 184
            JAVA_INT.withName("dmICMMethod"),                                    // 188
            JAVA_INT.withName("dmICMIntent"),                                    // 192
            JAVA_INT.withName("dmMediaType"),                                    // 196
            JAVA_INT.withName("dmDitherType"),                                   // 200
            JAVA_INT.withName("dmReserved1"),                                    // 204
            JAVA_INT.withName("dmReserved2"),                                    // 208
            JAVA_INT.withName("dmPanningWidth"),                                 // 212
            JAVA_INT.withName("dmPanningHeight")                                 // 216
    ).withName("DEVMODEW");

    private static final VarHandle DM_SIZE =
            LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dmSize"));
    private static final VarHandle DM_PELS_WIDTH =
            LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dmPelsWidth"));
    private static final VarHandle DM_PELS_HEIGHT =
            LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dmPelsHeight"));
    private static final VarHandle DM_DISPLAY_FREQUENCY =
            LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dmDisplayFrequency"));

    private DEVMODEW() {
    }

    /**
     * Allocates a {@code DEVMODEW} with {@code dmSize} pre-filled, as required by
     * {@code EnumDisplaySettingsW}.
     */
    public static MemorySegment allocate(SegmentAllocator allocator) {
        MemorySegment segment = allocator.allocate(LAYOUT);
        DM_SIZE.set(segment, 0L, (short) SIZE);
        return segment;
    }

    public static int pelsWidth(MemorySegment devMode) {
        return (int) DM_PELS_WIDTH.get(devMode, 0L);
    }

    public static int pelsHeight(MemorySegment devMode) {
        return (int) DM_PELS_HEIGHT.get(devMode, 0L);
    }

    public static int displayFrequency(MemorySegment devMode) {
        return (int) DM_DISPLAY_FREQUENCY.get(devMode, 0L);
    }
}
