package com.nz.jnawintools.win32;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;

/**
 * ole32.dll bindings (FFM).
 */
public final class Ole32 {

    private static final SymbolLookup LOOKUP = Foreign.library("ole32");

    private static final MethodHandle CO_TASK_MEM_FREE = Foreign.downcall(
            LOOKUP, "CoTaskMemFree", FunctionDescriptor.ofVoid(ADDRESS));

    private Ole32() {
    }

    /** {@code void CoTaskMemFree(LPVOID pv)}. */
    public static void coTaskMemFree(MemorySegment pv) {
        try {
            CO_TASK_MEM_FREE.invokeExact(pv);
        } catch (Throwable t) {
            throw new IllegalStateException("Ole32.CoTaskMemFree failed", t);
        }
    }
}
