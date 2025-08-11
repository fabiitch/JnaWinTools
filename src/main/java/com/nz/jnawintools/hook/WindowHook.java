package com.nz.jnawintools.hook;

import com.nz.jnawintools.hook.events.AbstractEventDispatcher;
import com.nz.jnawintools.hook.list.WindowFocusHook;
import com.nz.jnawintools.hook.list.WindowLifecycleHook;
import com.nz.jnawintools.hook.list.WindowMoveHook;
import com.nz.jnawintools.log.JnaWinToolsLogger;

import java.util.function.Consumer;

public class WindowHook {

    private final JnaWinToolsLogger logger;
    private final AbstractEventDispatcher<WindowEventAction> messageDispatcher;
    private final WindowFocusHook focusHook;
    private final WindowLifecycleHook lifecycleHook;
//    private final WindowMinMaxHook minMaxHook;
    private final WindowMoveHook moveHook;
//    private final WindowVisibilityHook visibilityHook;
    private final String windowTrack;

    public WindowHook(String windowTrack,
                      AbstractEventDispatcher<WindowEventAction> abstractEventDispatcher,
                      JnaWinToolsLogger logger) {
        this.logger = logger;
        this.messageDispatcher = abstractEventDispatcher;
        this.focusHook = new WindowFocusHook(windowTrack, messageDispatcher, logger);
        this.moveHook = new WindowMoveHook(windowTrack, messageDispatcher, logger);
//        this.minMaxHook = new WindowMinMaxHook(windowTrack, messageDispatcher, logger);
        this.lifecycleHook = new WindowLifecycleHook(windowTrack, messageDispatcher, logger);
//        this.visibilityHook = new WindowVisibilityHook(windowTrack, messageDispatcher, logger);
        this.logger.log("Create with target window = {}", windowTrack);
        this.windowTrack = windowTrack;
    }

    public void startHook(){
    }

    public void dispose() {
        focusHook.dispose();
        moveHook.dispose();
//        minMaxHook.dispose();
//        visibilityHook.dispose();
        lifecycleHook.dispose();
        this.logger.log("Disposed all hooks with target window = {}", windowTrack);
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
