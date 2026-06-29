package com.nz.jnawintools.hook.pump;

import com.nz.jnawintools.hook.cst.WinEventConstants;
import com.nz.jnawintools.hook.event.CriticalWinEventQueue;
import com.nz.jnawintools.hook.event.LocationChangeBuffer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class WinEventLoopThread {

    private static final int WM_APP_EXECUTE = WinEventConstants.WM_APP + 1;

    private final CriticalWinEventQueue criticalQueue;
    private final LocationChangeBuffer locationBuffer;

    private final CountDownLatch startedLatch = new CountDownLatch(1);
    private final AtomicBoolean running = new AtomicBoolean(false);


}
