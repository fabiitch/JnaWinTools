package com.nz.jnawintools.hook.event;

public enum WindowEventAction {
    Created,        // Fenêtre ouverte/détectée
    Closed,         // Fenêtre fermée

//    MinimiseStart,  // Commence à être minimisée
//    MinimiseEnd,    // Sort de l’état minimisé (restauration)

    GainFocus,      // Devient la fenêtre active
    LooseFocus,     // Perd le focus

//    Maximise,  // Passe en maximisée/plein écran
//    Restore,        // Retourne en taille normale

    Move,           // Déplacée
//    Resized,        // Taille changée

//    Show ,         // Réaffichée
//    Hide,         // Cachée sans minimisation

}
