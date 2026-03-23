package com.nz.jnawintools.hook.v2;

import com.sun.jna.platform.win32.WinDef;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public final class RawWinEvent {

    private  int event;
    private  WinDef.HWND hwnd;
    private  int idObject;
    private  int idChild;
    private  int eventThread;
    private  int eventTime;

    public RawWinEvent(WinDef.DWORD event,
                       WinDef.HWND hwnd,
                       WinDef.LONG idObject,
                       WinDef.LONG idChild,
                       WinDef.DWORD eventThread,
                       WinDef.DWORD eventTime) {
        this.event = event.intValue();
        this.hwnd = hwnd;
        this.idObject = idObject.intValue();
        this.idChild = idChild.intValue();
        this.eventThread = eventThread.intValue();
        this.eventTime = eventTime.intValue();
    }
    public boolean isWindowObject() {
        return idObject == 0 && idChild == 0;
    }
}