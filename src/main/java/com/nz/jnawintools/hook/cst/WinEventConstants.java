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
    int EVENT_OBJECT_HIDE            = 0x8003; // 32771
    int EVENT_OBJECT_REORDER         = 0x8004; // 32772
    int EVENT_OBJECT_FOCUS           = 0x8005; // 32773
    int EVENT_OBJECT_SELECTION       = 0x8006; // 32774
    int EVENT_OBJECT_SELECTIONADD    = 0x8007; // 32775
    int EVENT_OBJECT_SELECTIONREMOVE = 0x8008; // 32776
    int EVENT_OBJECT_SELECTIONWITHIN = 0x8009; // 32777
    int EVENT_OBJECT_STATECHANGE     = 0x800A; // 32778

    int EVENT_OBJECT_LOCATIONCHANGE = 0x800B;  // 32779 (SOURIS BOUGE)

    int EVENT_SYSTEM_MOVESIZESTART = 0x000A; // 10  Début déplacement / redimensionnement fenêtre
    int EVENT_SYSTEM_MOVESIZEEND   = 0x000B; // 11  Fin déplacement / redimensionnement fenêtre
    int EVENT_SYSTEM_FOREGROUND = 0x0003;

    int EVENT_SYSTEM_MINIMIZE_START = 22;
    int EVENT_SYSTEM_MINIMIZE_END = 23;



    int OBJID_WINDOW = 0;
}
