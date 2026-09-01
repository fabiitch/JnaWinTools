package com.nz.jnawintools.win32;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

/**
 * dwmapi.dll bindings (FFM).
 */
public final class Dwmapi {

    private static final SymbolLookup LOOKUP = Foreign.library("dwmapi");

    private static final MethodHandle DWM_IS_COMPOSITION_ENABLED = Foreign.downcall(
            LOOKUP, "DwmIsCompositionEnabled", FunctionDescriptor.of(JAVA_INT, ADDRESS));

    private static final MethodHandle DWM_GET_WINDOW_ATTRIBUTE = Foreign.downcall(
            LOOKUP, "DwmGetWindowAttribute",
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT));

    private Dwmapi() {
    }

    /**
     * {@code HRESULT DwmIsCompositionEnabled(BOOL* pfEnabled)}. {@code pfEnabled} is a segment able
     * to hold a 4-byte {@code BOOL}. Returns the {@code HRESULT} ({@code 0} = S_OK).
     */
    public static int dwmIsCompositionEnabled(MemorySegment pfEnabled) {
        try {
            return (int) DWM_IS_COMPOSITION_ENABLED.invokeExact(pfEnabled);
        } catch (Throwable t) {
            throw new IllegalStateException("Dwmapi.DwmIsCompositionEnabled failed", t);
        }
    }

    /**
     * {@code HRESULT DwmGetWindowAttribute(HWND, DWORD, PVOID, DWORD)}. Returns the {@code HRESULT}.
     */
    public static int dwmGetWindowAttribute(long hwnd, int attribute, MemorySegment value, int size) {
        try {
            return (int) DWM_GET_WINDOW_ATTRIBUTE.invokeExact(Foreign.handle(hwnd), attribute, value, size);
        } catch (Throwable t) {
            throw new IllegalStateException("Dwmapi.DwmGetWindowAttribute failed", t);
        }
    }
}
