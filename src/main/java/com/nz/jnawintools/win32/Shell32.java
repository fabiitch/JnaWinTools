package com.nz.jnawintools.win32;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_INT;

/**
 * shell32.dll bindings (FFM), plus the {@code KNOWNFOLDERID} GUIDs this project needs.
 */
public final class Shell32 {

    private static final SymbolLookup LOOKUP = Foreign.library("shell32");

    private static final MethodHandle SH_GET_KNOWN_FOLDER_PATH = Foreign.downcall(
            LOOKUP, "SHGetKnownFolderPath",
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, ADDRESS));

    /** {@code FOLDERID_Documents} = {FDD39AD0-238F-46AF-ADB4-6C85480369C7}. */
    private static final byte[] FOLDERID_DOCUMENTS = {
            (byte) 0xD0, (byte) 0x9A, (byte) 0xD3, (byte) 0xFD, // Data1 (little-endian)
            (byte) 0x8F, (byte) 0x23,                           // Data2 (little-endian)
            (byte) 0xAF, (byte) 0x46,                           // Data3 (little-endian)
            (byte) 0xAD, (byte) 0xB4,                           // Data4[0..1]
            (byte) 0x6C, (byte) 0x85, (byte) 0x48, (byte) 0x03, (byte) 0x69, (byte) 0xC7 // Data4[2..7]
    };

    private Shell32() {
    }

    /**
     * Returns the current user's {@code Documents} folder path.
     *
     * <p>The wide-string result is allocated by the shell with {@code CoTaskMemAlloc}; this method
     * frees it via {@link Ole32#coTaskMemFree(MemorySegment)} before returning.
     *
     * @throws IllegalStateException if {@code SHGetKnownFolderPath} returns a failing {@code HRESULT}
     */
    public static String getDocumentsPath() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment rfid = arena.allocate(FOLDERID_DOCUMENTS.length);
            MemorySegment.copy(FOLDERID_DOCUMENTS, 0, rfid, JAVA_BYTE, 0, FOLDERID_DOCUMENTS.length);
            MemorySegment pathPtrOut = arena.allocate(ADDRESS);

            int hr = shGetKnownFolderPath(rfid, 0, MemorySegment.NULL, pathPtrOut);
            if (hr != 0) {
                throw new IllegalStateException("SHGetKnownFolderPath failed, HRESULT=0x"
                        + Integer.toHexString(hr));
            }

            MemorySegment pathPtr = pathPtrOut.get(ADDRESS, 0);
            try {
                return pathPtr.reinterpret(Long.MAX_VALUE).getString(0, StandardCharsets.UTF_16LE);
            } finally {
                Ole32.coTaskMemFree(pathPtr);
            }
        }
    }

    public static int shGetKnownFolderPath(MemorySegment rfid, int flags, MemorySegment token,
                                           MemorySegment pathPtrOut) {
        try {
            return (int) SH_GET_KNOWN_FOLDER_PATH.invokeExact(rfid, flags, token, pathPtrOut);
        } catch (Throwable t) {
            throw new IllegalStateException("Shell32.SHGetKnownFolderPath failed", t);
        }
    }
}
