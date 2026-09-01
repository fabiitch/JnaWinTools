package com.nz.jnawintools.hook.pump;

import com.nz.jnawintools.hook.cst.WinEventConstants;
import com.nz.jnawintools.hook.event.CriticalWinEventQueue;
import com.nz.jnawintools.hook.event.LocationChangeBuffer;
import com.nz.jnawintools.hook.handler.WinEventRange;
import com.nz.jnawintools.win32.Kernel32;
import com.nz.jnawintools.win32.MSG;
import com.nz.jnawintools.win32.User32;
import com.nz.jnawintools.win32.WinUser;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.nz.jnawintools.hook.cst.WinEventConstants.OBJID_WINDOW;

/**
 * Dedicated OS thread that owns the whole WinEvent lifecycle: {@code SetWinEventHook},
 * {@code GetMessageW}, {@code DispatchMessageW} and {@code UnhookWinEvent} all run on this single
 * thread, as required by the Win32 message pump model.
 *
 * <p>A single {@link User32.WinEventProc} upcall stub is created once and shared by all event
 * ranges; it lives in an explicit {@link Arena} that is closed only after every
 * {@code UnhookWinEvent} has completed. The {@code MSG} structure is allocated once, outside the
 * loop, and reused for every message.
 */
@Slf4j
public class WinEventPumpThread extends Thread {

    private final List<WinEventRange> ranges;
    private final CriticalWinEventQueue criticalQueue;
    private final LocationChangeBuffer locationBuffer;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final CountDownLatch startedLatch = new CountDownLatch(1);

    private final long[] hookHandles;

    private Arena arena;
    private MemorySegment upcallStub;

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
        this.hookHandles = new long[ranges.size()];
        setDaemon(true);
    }

    @Override
    public void run() {
        boolean startupSignalled = false;
        try {
            nativeThreadId = Kernel32.getCurrentThreadId();
            arena = Arena.ofConfined();
            MemorySegment msg = MSG.allocate(arena);

            // PeekMessage creates the native queue before startAndWait returns, eliminating the
            // PostThreadMessage race when a caller starts and immediately stops the hook.
            User32.peekMessage(msg, 0L, 0, 0, WinUser.PM_NOREMOVE);
            upcallStub = User32.createWinEventUpcall(this::onWinEvent, arena);

            for (int i = 0; i < ranges.size(); i++) {
                WinEventRange range = ranges.get(i);
                Kernel32.setLastError(0);
                long handle = User32.setWinEventHook(
                        range.eventMin(),
                        range.eventMax(),
                        0L,
                        upcallStub,
                        0,
                        0,
                        range.flags());

                if (handle == 0L) {
                    throw new IllegalStateException("SetWinEventHook failed for " + range.name()
                            + " err=" + Kernel32.getLastError());
                }

                hookHandles[i] = handle;

                if (log.isTraceEnabled()) {
                    log.trace("Installed hook [{}] handle=0x{} min={} max={} flags=0x{}",
                            range.name(),
                            Long.toHexString(handle),
                            range.eventMin(),
                            range.eventMax(),
                            Integer.toHexString(range.flags()));
                }
            }

            running.set(true);
            startupSignalled = true;
            startedLatch.countDown();

            int result;
            while ((result = User32.getMessage(msg, 0L, 0, 0)) > 0) {
                User32.translateMessage(msg);
                User32.dispatchMessage(msg);
            }

            if (result == -1) {
                log.error("GetMessage failed. err={}", Kernel32.getLastError());
            }
        } catch (Throwable t) {
            if (startupSignalled) {
                log.error("WinEvent pump failed", t);
            } else {
                startupFailure = t;
            }
        } finally {
            if (!startupSignalled) {
                startedLatch.countDown();
            }
            cleanup();
            running.set(false);
            nativeThreadId = 0;
        }
    }

    /**
     * Native WinEvent callback. Runs on this pump thread. It must never allocate on the happy path
     * and must never let an exception cross the native boundary.
     */
    private void onWinEvent(MemorySegment hWinEventHook, int event, MemorySegment hwnd,
                            int idObject, int idChild, int idEventThread, int dwmsEventTime) {
        try {
            long handle = hwnd.address();
            if (event == WinEventConstants.EVENT_OBJECT_LOCATIONCHANGE) {
                if (idObject != OBJID_WINDOW || idChild != 0) {
                    return;
                }
                locationBuffer.publish(event, handle, idObject, idChild, idEventThread, dwmsEventTime);
            } else {
                criticalQueue.publish(event, handle, idObject, idChild, idEventThread, dwmsEventTime);
            }

            WinEventPump p = pump;
            if (p != null) {
                p.signalWork();
            }
        } catch (Throwable t) {
            // Swallow: an exception must not propagate through the native callback frame.
            log.error("WinEvent callback failure", t);
        }
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

    public synchronized void shutdown() {
        if (!running.get()) {
            return;
        }

        int threadId = nativeThreadId;
        if (threadId == 0) {
            throw new IllegalStateException("WinEvent pump has no native thread id");
        }

        Kernel32.setLastError(0);
        if (!User32.postThreadMessage(threadId, WinUser.WM_QUIT, 0L, 0L)) {
            throw new IllegalStateException("PostThreadMessageW(WM_QUIT) failed, err="
                    + Kernel32.getLastError());
        }
        running.set(false);
    }

    private void cleanup() {
        for (int i = 0; i < hookHandles.length; i++) {
            long handle = hookHandles[i];
            if (handle == 0L) {
                continue;
            }
            try {
                Kernel32.setLastError(0);
                if (!User32.unhookWinEvent(handle)) {
                    log.error("UnhookWinEvent failed for handle=0x{}, err={}",
                            Long.toHexString(handle), Kernel32.getLastError());
                }
            } catch (Throwable t) {
                log.error("UnhookWinEvent failed", t);
            }
            hookHandles[i] = 0L;
        }

        // Close the arena only after every UnhookWinEvent has completed, so the shared upcall stub
        // stays valid for as long as any hook could still fire.
        if (arena != null) {
            try {
                arena.close();
            } catch (Throwable t) {
                log.error("Arena close failed", t);
            }
            arena = null;
            upcallStub = null;
        }
    }
}
