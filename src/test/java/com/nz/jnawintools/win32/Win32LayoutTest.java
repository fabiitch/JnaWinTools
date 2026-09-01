package com.nz.jnawintools.win32;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies the exact Windows x64 ABI sizes of the modelled structures. No native calls.
 */
class Win32LayoutTest {

    @Test
    void rectIs16Bytes() {
        assertEquals(16, RECT.LAYOUT.byteSize());
    }

    @Test
    void pointIs8Bytes() {
        assertEquals(8, POINT.LAYOUT.byteSize());
    }

    @Test
    void msgIs48BytesOnX64() {
        assertEquals(48, MSG.LAYOUT.byteSize());
    }

    @Test
    void monitorInfoExwIs104Bytes() {
        assertEquals(104, MONITORINFOEXW.SIZE);
        assertEquals(104, MONITORINFOEXW.LAYOUT.byteSize());
    }

    @Test
    void devmodewIs220Bytes() {
        assertEquals(220, DEVMODEW.SIZE);
        assertEquals(220, DEVMODEW.LAYOUT.byteSize());
    }
}
