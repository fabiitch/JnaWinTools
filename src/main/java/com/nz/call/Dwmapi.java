package com.nz.call;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;

public interface Dwmapi extends StdCallLibrary {
    Dwmapi INSTANCE = Native.load("dwmapi", Dwmapi.class);

    /**
     * Renvoie 0 si OK, sinon un code d’erreur Windows.
     * pfEnabled : TRUE si DWM est actif (compositing), FALSE si désactivé.
     */
    int DwmIsCompositionEnabled(WinDef.BOOLByReference pfEnabled);
}
