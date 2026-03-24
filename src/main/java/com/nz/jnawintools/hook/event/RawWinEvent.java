package com.nz.jnawintools.hook.event;

import com.sun.jna.platform.win32.WinDef;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public final class RawWinEvent {

    private int event;
    private WinDef.HWND hwnd;
    private int idObject;
    private int idChild;
    private int eventThread;
    private int eventTime;

    public void set(int event,
                    WinDef.HWND hwnd,
                    int idObject,
                    int idChild,
                    int eventThread,
                    int eventTime) {
        this.event = event;
        this.hwnd = hwnd;
        this.idObject = idObject;
        this.idChild = idChild;
        this.eventThread = eventThread;
        this.eventTime = eventTime;
    }

    public void clear() {
        this.event = 0;
        this.hwnd = null;
        this.idObject = 0;
        this.idChild = 0;
        this.eventThread = 0;
        this.eventTime = 0;
    }

    public boolean isWindowObject() {
        return idObject == 0 && idChild == 0;
    }
}