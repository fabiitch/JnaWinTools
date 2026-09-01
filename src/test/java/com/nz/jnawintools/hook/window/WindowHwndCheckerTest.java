package com.nz.jnawintools.hook.window;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WindowHwndCheckerTest {

    @Test
    void matchesOnlyTheExactHandle() {
        WindowHwndChecker checker = new WindowHwndChecker(0x1234L);

        assertTrue(checker.isWindow(0x1234L));
        assertFalse(checker.isWindow(0x5678L));
    }

    @Test
    void nullHandleNeverMatches() {
        assertFalse(new WindowHwndChecker(0x1234L).isWindow(0L));
        // A null target must not be matched by a null probe either.
        assertFalse(new WindowHwndChecker(0L).isWindow(0L));
    }
}
