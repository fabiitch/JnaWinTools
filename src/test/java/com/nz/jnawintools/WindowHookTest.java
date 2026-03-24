package com.nz.jnawintools;

import com.nz.jnawintools.hook.WindowHook;
import com.nz.jnawintools.hook.event.WindowEventAction;
import com.nz.jnawintools.hook.event.dispatch.SyncEventDispatcher;
import com.nz.jnawintools.hook.window.WindowTitleEqualsChecker;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;
@Disabled
public class WindowHookTest {

    @Test
    public void testHook() throws InterruptedException {
        WindowHook windowHook = new WindowHook(WindowTitleEqualsChecker.get("Calculatrice"),
            new SyncEventDispatcher<>());

        windowHook.addListener(new Consumer<WindowEventAction>() {
            int inc = 0;

            @Override
            public void accept(WindowEventAction windowEventAction) {
                System.out.println(inc + " - Action=" + windowEventAction);
                inc++;
            }
        });
        System.out.println("HOOKED");
        windowHook.start();

        // Boucle de messages Windows
        WinUser.MSG msg = new WinUser.MSG();
        while (true) {
            while (User32.INSTANCE.GetMessage(msg, null, 0, 0) > 0) {
                User32.INSTANCE.TranslateMessage(msg);
                User32.INSTANCE.DispatchMessage(msg);
            }
        }
    }
}
