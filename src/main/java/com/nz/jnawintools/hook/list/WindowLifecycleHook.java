package com.nz.jnawintools.hook.list;

import com.nz.jnawintools.hook.WindowEventAction;
import com.nz.jnawintools.hook.events.AbstractEventDispatcher;
import com.nz.jnawintools.log.JnaWinToolsLogger;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;

import static com.nz.jnawintools.hook.cst.WinEventConstants.*;

/**
 * Validated !
 * Hook pour détecter la création et destruction d’une fenêtre spécifique (WINDOW_TRACKED).
 */
public class WindowLifecycleHook extends BaseWindowHook {

    public WindowLifecycleHook(String windowToTrack,
                               AbstractEventDispatcher<WindowEventAction> messageDispatcher,
                               JnaWinToolsLogger logger) {
        super(windowToTrack, messageDispatcher, logger);
    }

    @Override
    protected String name() {
        return "LifecycleHook";
    }

    @Override
    protected WinNT.HANDLE buildHook() {
        WinUser.WinEventProc eventProc = (hWinEventHook, event,
                                          hwnd,
                                          idObject, idChild,
                                          dwEventThread, dwmsEventTime) -> {
            // On ne traite que les vraies fenêtres (OBJID_WINDOW)
            if (idObject.intValue() != 0 || idChild.intValue() != 0) return;

            int eventId = event.intValue();
            String title = window64Helper.getName(hwnd);

            if (WINDOW_TRACKED.equalsIgnoreCase(title)) {
                if (eventId == EVENT_OBJECT_CREATE) {
                    dispatch(WindowEventAction.Created);
                } else if (eventId == EVENT_OBJECT_DESTROY) {
                    dispatch(WindowEventAction.Closed);
                }
            }
        };

        // On écoute uniquement création et destruction (moins de bruit que SHOW/HIDE)
        WinNT.HANDLE handle = User32.INSTANCE.SetWinEventHook(
                EVENT_OBJECT_CREATE,
                EVENT_OBJECT_DESTROY,
                null,
                eventProc,
                0, 0,
                WINEVENT_OUTOFCONTEXT | WINEVENT_SKIPOWNPROCESS
        );
        return handle;
    }
}
