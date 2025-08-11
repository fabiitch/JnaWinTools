package com.nz.jnawintools.hook.cst;

public interface WinEventConstants {
    int WINEVENT_OUTOFCONTEXT = 0x0000; // Callback dans le thread du caller (pas d’injection)
    int WINEVENT_SKIPOWNTHREAD = 0x0001; // Ignore les events du thread appelant
    int WINEVENT_SKIPOWNPROCESS = 0x0002; // Ignore les events du process appelant
    int WINEVENT_INCONTEXT = 0x0004; // Injecte da

    // Windows Event constants (winuser.h)
    // Les valeurs correspondent à des événements système liés aux objets/fenêtres/accessibilité
    int EVENT_OBJECT_CREATE = 0x8000; // 32768 Création d'un objet (fenêtre, control, etc)
    int EVENT_OBJECT_DESTROY = 0x8001; // 32769 Destruction d'un objet
    int EVENT_OBJECT_SHOW = 0x8002; // 32770 L'objet devient visible (affiché)
    int EVENT_OBJECT_HIDE = 0x8003; // 32771 L'objet devient invisible (caché)
    int EVENT_OBJECT_LOCATIONCHANGE = 0x800B;

    int EVENT_SYSTEM_FOREGROUND = 0x0003;
    int EVENT_SYSTEM_MINIMIZE_END = 23;
    int EVENT_SYSTEM_MINIMIZE_START = 22;


    int OBJID_WINDOW = 0;
}
