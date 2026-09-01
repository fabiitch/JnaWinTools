package com.nz.jnawintools.win32;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.JAVA_INT;

/**
 * kernel32.dll bindings (FFM). All handles are resolved once into {@code static final} fields.
 */
public final class Kernel32 {

    private static final SymbolLookup LOOKUP = Foreign.library("kernel32");

    private static final StructLayout CAPTURE_STATE_LAYOUT = Linker.Option.captureStateLayout();
    private static final long LAST_ERROR_OFFSET = CAPTURE_STATE_LAYOUT.byteOffset(
            MemoryLayout.PathElement.groupElement("GetLastError"));
    private static final ThreadLocal<MemorySegment> CAPTURE_STATE = ThreadLocal.withInitial(
            () -> Arena.ofAuto().allocate(CAPTURE_STATE_LAYOUT));

    private static final MethodHandle GET_LAST_ERROR = Foreign.downcall(
            LOOKUP, "GetLastError", FunctionDescriptor.of(JAVA_INT));

    private static final MethodHandle SET_LAST_ERROR = Foreign.downcall(
            LOOKUP, "SetLastError", FunctionDescriptor.ofVoid(JAVA_INT));

    private static final MethodHandle GET_CURRENT_THREAD_ID = Foreign.downcall(
            LOOKUP, "GetCurrentThreadId", FunctionDescriptor.of(JAVA_INT));

    private Kernel32() {
    }

    /**
     * Returns the last error captured atomically by the most recent error-aware FFM downcall on
     * this Java thread.
     */
    public static int getLastError() {
        return callState().get(JAVA_INT, LAST_ERROR_OFFSET);
    }

    /**
     * Queries the native thread-local value directly. Prefer {@link #getLastError()} immediately
     * after an error-aware downcall.
     */
    public static int queryLastError() {
        try {
            return (int) GET_LAST_ERROR.invokeExact();
        } catch (Throwable t) {
            throw new IllegalStateException("Kernel32.GetLastError failed", t);
        }
    }

    public static void setLastError(int errorCode) {
        try {
            SET_LAST_ERROR.invokeExact(errorCode);
            callState().set(JAVA_INT, LAST_ERROR_OFFSET, errorCode);
        } catch (Throwable t) {
            throw new IllegalStateException("Kernel32.SetLastError failed", t);
        }
    }

    public static int getCurrentThreadId() {
        try {
            return (int) GET_CURRENT_THREAD_ID.invokeExact();
        } catch (Throwable t) {
            throw new IllegalStateException("Kernel32.GetCurrentThreadId failed", t);
        }
    }

    static MemorySegment callState() {
        return CAPTURE_STATE.get();
    }
}
