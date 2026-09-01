package com.nz.jnawintools.win32;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

/**
 * Central plumbing for the Java FFM (Panama) Win32 layer.
 *
 * <p>The {@link Linker} and the per-library {@link SymbolLookup}s are resolved once and shared
 * globally. Every {@link MethodHandle} exposed by the {@code win32} classes is resolved a single
 * time into a {@code static final} field, so no symbol lookup or handle creation ever happens on a
 * hot path.
 */
public final class Foreign {

    /** Shared native linker for the whole process. */
    public static final Linker LINKER = Linker.nativeLinker();

    private Foreign() {
    }

    /**
     * Loads a native library for the lifetime of the JVM (backed by {@link Arena#global()}).
     */
    public static SymbolLookup library(String name) {
        return SymbolLookup.libraryLookup(name, Arena.global());
    }

    /**
     * Resolves a downcall {@link MethodHandle} once, failing loudly if the symbol is missing.
     */
    public static MethodHandle downcall(SymbolLookup lookup,
                                        String symbol,
                                        FunctionDescriptor descriptor,
                                        Linker.Option... options) {
        MemorySegment address = lookup.find(symbol)
                .orElseThrow(() -> new UnsatisfiedLinkError("Native symbol not found: " + symbol));
        return LINKER.downcallHandle(address, descriptor, options);
    }

    /**
     * Resolves a downcall that atomically captures the calling thread's Win32 last-error value.
     */
    public static MethodHandle downcallCapturingLastError(SymbolLookup lookup,
                                                          String symbol,
                                                          FunctionDescriptor descriptor) {
        return downcall(lookup, symbol, descriptor,
                Linker.Option.captureCallState("GetLastError"));
    }

    /**
     * Converts an opaque Windows handle carried as a {@code long} into a boundary
     * {@link MemorySegment}. {@code 0} maps to {@link MemorySegment#NULL}.
     */
    public static MemorySegment handle(long value) {
        return value == 0L ? MemorySegment.NULL : MemorySegment.ofAddress(value);
    }

    /**
     * Allocates a null terminated UTF-16LE (Windows wide) string into {@code arena}.
     */
    public static MemorySegment wide(Arena arena, String value) {
        return arena.allocateFrom(value, StandardCharsets.UTF_16LE);
    }
}
