package com.nz.jnawintools.hook;

import com.nz.jnawintools.hook.events.AbstractEventDispatcher;
import com.nz.jnawintools.hook.list.*;
import com.nz.jnawintools.hook.window.WindowChecker;
import com.nz.jnawintools.log.JWTLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WindowHook {

    private final JWTLogger logger;
    private final AbstractEventDispatcher<WindowEventAction> messageDispatcher;
    private final WindowFocusHook focusHook;
    private final WindowLifecycleHook lifecycleHook;
    private final WindowMoveHook moveHook;
    //    private final WindowMinMaxHook minMaxHook;
//    private final WindowVisibilityHook visibilityHook;
    private final WindowChecker windowToTrackChecker;

    private final List<BaseWindowHook> hooks = new ArrayList<>();

    public WindowHook(WindowChecker windowToTrackChecker,
                      AbstractEventDispatcher<WindowEventAction> abstractEventDispatcher,
                      JWTLogger logger) {
        this.logger = logger;
        this.messageDispatcher = abstractEventDispatcher;
        this.focusHook = new WindowFocusHook(windowToTrackChecker, messageDispatcher, logger);
        this.moveHook = new WindowMoveHook(windowToTrackChecker, messageDispatcher, logger);
//        this.minMaxHook = new WindowMinMaxHook(windowToTrackChecker, messageDispatcher, logger);
        this.lifecycleHook = new WindowLifecycleHook(windowToTrackChecker, messageDispatcher, logger);
//        this.visibilityHook = new WindowVisibilityHook(windowToTrackChecker, messageDispatcher, logger);
        this.logger.log("Create with target window = {}", windowToTrackChecker.getWindowName());
        this.windowToTrackChecker = windowToTrackChecker;
    }

    public void startHook(){
        focusHook.start();
        moveHook.start();
        lifecycleHook.start();
//        minMaxHook.start();
    }

    public void dispose() {
        focusHook.stop();
        moveHook.stop();
//        minMaxHook.stop();
//        minMaxHook.dispose();
//        visibilityHook.dispose();
        lifecycleHook.stop();
        this.logger.log("Disposed all hooks with target window = {}", windowToTrackChecker.getWindowName());
    }

    public void addListener(Consumer<WindowEventAction> listener) {
        messageDispatcher.addListener(listener);
    }

    public void removeListener(Consumer<WindowEventAction> listener) {
        messageDispatcher.removeListener(listener);
    }

    public void clearListeners() {
        messageDispatcher.clear();
    }
}
