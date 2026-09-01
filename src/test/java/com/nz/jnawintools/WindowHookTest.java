package com.nz.jnawintools;

import com.nz.jnawintools.hook.WindowHook;
import com.nz.jnawintools.hook.event.WindowEventAction;
import com.nz.jnawintools.hook.event.dispatch.SyncEventDispatcher;
import com.nz.jnawintools.hook.window.WindowTitleEqualsChecker;
import com.nz.jnawintools.win32.Kernel32;
import com.nz.jnawintools.win32.User32;
import com.nz.jnawintools.win32.WinUser;
import com.nz.jnawintools.window.Window64Utils;
import com.nz.jnawintools.window.result.HwndResult;
import com.nz.jnawintools.window.result.WindowBoundsResult;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Bounded, cleanly-stoppable smoke test for the WinEvent hook lifecycle.
 *
 * <p>The pump owns its own OS message loop, so this test no longer runs an infinite
 * {@code GetMessage} loop of its own. It merely starts the hook, observes for a short bounded
 * window, and stops it. The full calculator scenario is left to the parent project.
 */
public class WindowHookTest {

    private static final String CALCULATOR_TEST_ENV = "JNAWINTOOLS_CALCULATOR_TEST";
    private static final String CALCULATOR_TITLE_ENV = "JNAWINTOOLS_CALCULATOR_TITLE";
    private static final long CALCULATOR_START_TIMEOUT_MILLIS = 15_000L;

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    @Test
    public void hookStartsAndStopsCleanly() throws InterruptedException {
        Assumptions.assumeTrue(isWindows(), "WinEvent hooks require Windows");

        WindowHook windowHook = new WindowHook(
                WindowTitleEqualsChecker.get("Calculatrice"),
                new SyncEventDispatcher<>());

        AtomicInteger count = new AtomicInteger();
        windowHook.addListener((WindowEventAction action) -> count.incrementAndGet());

        windowHook.start();
        try {
            Thread rawThread = windowHook.getHookThread();
            Thread consumerThread = windowHook.getConsumerThread();
            assertNotNull(rawThread, "pump thread should be started");
            assertNotNull(consumerThread, "consumer thread should be started");

            // Bounded observation window; we do not require any specific window to be present.
            Thread.sleep(500);
        } finally {
            windowHook.stop();
        }
    }

    @Test
    public void calculatorMoveIsDeliveredByTheNativePump() throws Exception {
        Assumptions.assumeTrue(isWindows(), "WinEvent hooks require Windows");
        Assumptions.assumeTrue(Boolean.parseBoolean(System.getenv(CALCULATOR_TEST_ENV)),
                "Set " + CALCULATOR_TEST_ENV + "=true to run the Calculator integration test");

        CalculatorWindow calculator = openCalculator();
        try {
            WindowBoundsResult originalBoundsResult = Window64Utils.getWindowBounds(calculator.hwnd());
            assertTrue(originalBoundsResult.isSuccess(), originalBoundsResult::getErrorMessage);
            Rectangle originalBounds = originalBoundsResult.getResult();
            assertEquals(calculator.title(), Window64Utils.getName(calculator.hwnd()).getResult());

            CountDownLatch moved = new CountDownLatch(1);
            WindowHook windowHook = new WindowHook(
                    WindowTitleEqualsChecker.get(calculator.title()),
                    new SyncEventDispatcher<>());
            windowHook.addListener(action -> {
                if (action == WindowEventAction.Move) {
                    moved.countDown();
                }
            });

            windowHook.start();
            try {
                Rectangle probeBounds = new Rectangle(originalBounds);
                probeBounds.translate(originalBounds.x < 16 ? 16 : -16, 0);
                assertTrue(Window64Utils.setWindowPosition(calculator.hwnd(), probeBounds).isSuccess(),
                        "SetWindowPos failed for Calculator");
                assertTrue(moved.await(10, TimeUnit.SECONDS),
                        "No EVENT_OBJECT_LOCATIONCHANGE was delivered for Calculator");
            } finally {
                Window64Utils.setWindowPosition(calculator.hwnd(), originalBounds);
                windowHook.stop();
            }
        } finally {
            calculator.closeIfStartedByTest();
        }
    }

    private static CalculatorWindow openCalculator() throws IOException, InterruptedException {
        Set<String> candidateTitles = new LinkedHashSet<>();
        String configuredTitle = System.getenv(CALCULATOR_TITLE_ENV);
        if (configuredTitle != null && !configuredTitle.isBlank()) {
            candidateTitles.add(configuredTitle);
        }
        candidateTitles.add("Calculatrice");
        candidateTitles.add("Calculator");

        CalculatorWindow existing = findCalculator(candidateTitles, null, false);
        if (existing != null) {
            return existing;
        }

        Process process = new ProcessBuilder("calc.exe").start();
        long deadline = System.nanoTime()
                + TimeUnit.MILLISECONDS.toNanos(CALCULATOR_START_TIMEOUT_MILLIS);
        do {
            CalculatorWindow started = findCalculator(candidateTitles, process, true);
            if (started != null) {
                return started;
            }
            Thread.sleep(100);
        } while (System.nanoTime() < deadline);

        process.destroy();
        return fail("Calculator did not expose a window named " + candidateTitles
                + " within " + CALCULATOR_START_TIMEOUT_MILLIS + " ms");
    }

    private static CalculatorWindow findCalculator(Set<String> candidateTitles,
                                                   Process process,
                                                   boolean startedByTest) {
        for (String title : candidateTitles) {
            HwndResult result = Window64Utils.getHwnd(title);
            if (result.isSuccess()) {
                return new CalculatorWindow(title, result.getHwnd(), process, startedByTest);
            }
        }
        return null;
    }

    private record CalculatorWindow(String title, long hwnd, Process process, boolean startedByTest) {
        private void closeIfStartedByTest() {
            if (!startedByTest) {
                return;
            }
            Kernel32.setLastError(0);
            boolean closePosted = User32.postMessage(hwnd, WinUser.WM_CLOSE, 0L, 0L);
            if (!closePosted && process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }
}
