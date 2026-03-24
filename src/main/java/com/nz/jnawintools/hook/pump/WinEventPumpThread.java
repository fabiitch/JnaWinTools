package com.nz.jnawintools.hook.pump;

import com.nz.jnawintools.hook.cst.WinEventConstants;
import com.nz.jnawintools.hook.event.CriticalWinEventQueue;
import com.nz.jnawintools.hook.event.LocationChangeBuffer;
import com.nz.jnawintools.hook.handler.WinEventRange;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class WinEventPumpThread extends Thread {

    private final List<WinEventRange> ranges;
    private final CriticalWinEventQueue criticalQueue;
    private final LocationChangeBuffer locationBuffer;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final CountDownLatch startedLatch = new CountDownLatch(1);

    private final List<WinNT.HANDLE> hookHandles = new ArrayList<>();
    private final List<WinUser.WinEventProc> callbacks = new ArrayList<>();

    private volatile int nativeThreadId;
    private volatile Throwable startupFailure;

    @Setter
    private WinEventPump pump;

    public WinEventPumpThread(String name,
                              List<WinEventRange> ranges,
                              CriticalWinEventQueue criticalQueue,
                              LocationChangeBuffer locationBuffer) {
        super(name);
        this.ranges = ranges;
        this.criticalQueue = criticalQueue;
        this.locationBuffer = locationBuffer;
        setDaemon(true);
    }

    @Override
    public void run() {
        nativeThreadId = Kernel32.INSTANCE.GetCurrentThreadId();
        running.set(true);

        try {
            for (WinEventRange range : ranges) {
                WinUser.WinEventProc proc = (hWinEventHook, event, hwnd, idObject, idChild, dwEventThread, dwmsEventTime) -> {
                    try {
                        int eventCode = event.intValue();

                        if (eventCode == WinEventConstants.EVENT_OBJECT_LOCATIONCHANGE) {
                            locationBuffer.publish(
                                    eventCode,
                                    hwnd,
                                    idObject.intValue(),
                                    idChild.intValue(),
                                    dwEventThread.intValue(),
                                    dwmsEventTime.intValue()
                            );
                        } else {
                            criticalQueue.publish(
                                    eventCode,
                                    hwnd,
                                    idObject.intValue(),
                                    idChild.intValue(),
                                    dwEventThread.intValue(),
                                    dwmsEventTime.intValue()
                            );
                        }
                        pump.signalWork();

                    } catch (Throwable t) {
                        log.error("[{}] callback failure", range.name(), t);
                    }
                };

                WinNT.HANDLE handle = User32.INSTANCE.SetWinEventHook(
                        range.eventMin(),
                        range.eventMax(),
                        null,
                        proc,
                        0,
                        0,
                        range.flags()
                );

                if (handle == null) {
                    throw new IllegalStateException("SetWinEventHook failed for " + range.name()
                            + " err=" + Kernel32.INSTANCE.GetLastError());
                }

                callbacks.add(proc);
                hookHandles.add(handle);

                log.trace("Installed hook [{}] handle=0x{} min={} max={} flags=0x{}",
                        range.name(),
                        Long.toHexString(Pointer.nativeValue(handle.getPointer())),
                        range.eventMin(),
                        range.eventMax(),
                        Integer.toHexString(range.flags()));
            }

        } catch (Throwable t) {
            startupFailure = t;
            cleanup();
            startedLatch.countDown();
            return;
        }

        startedLatch.countDown();

        WinUser.MSG msg = new WinUser.MSG();
        int result;
        while ((result = User32.INSTANCE.GetMessage(msg, null, 0, 0)) > 0) {
            User32.INSTANCE.TranslateMessage(msg);
            User32.INSTANCE.DispatchMessage(msg);
        }

        if (result == -1) {
            log.error("GetMessage failed. err={}", Kernel32.INSTANCE.GetLastError());
        }

        cleanup();
    }

    public void startAndWait() {
        start();
        try {
            startedLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting startup", e);
        }

        if (startupFailure != null) {
            throw new IllegalStateException("Pump startup failed", startupFailure);
        }
    }

    public void shutdown() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        if (nativeThreadId != 0) {
            User32.INSTANCE.PostThreadMessage(nativeThreadId, WinUser.WM_QUIT, null, null);
        }
    }

    private void cleanup() {
        for (WinNT.HANDLE handle : hookHandles) {
            try {
                User32.INSTANCE.UnhookWinEvent(handle);
            } catch (Throwable t) {
                log.error("UnhookWinEvent failed", t);
            }
        }
        hookHandles.clear();
        callbacks.clear();
    }
}
