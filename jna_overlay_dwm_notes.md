# Note technique -- Amélioration overlay Windows (JNA / Win32 / DWM)

Projet concerné : JnaWinTools / overlay Windows

Idée à implémenter plus tard : utiliser **DWM (Desktop Window Manager)**
pour améliorer le rendu d'un overlay.

------------------------------------------------------------------------

## 1. DwmExtendFrameIntoClientArea

API Win32 : `DwmExtendFrameIntoClientArea`

Objectif : - étendre le frame DWM dans la zone cliente - permettre un
rendu **glass / surface propre** - éviter les bords visuels "cheap" des
fenêtres borderless classiques

Principe : - passer des **marges négatives (-1)** pour obtenir l'effet
"sheet of glass"

Usage typique pour un overlay :

-   fenêtre borderless (`WS_POPUP`)
-   `WS_EX_LAYERED`
-   `WS_EX_TRANSPARENT` (click-through)
-   `HWND_TOPMOST`
-   -   `DwmExtendFrameIntoClientArea`

Cela permet d'avoir une fenêtre overlay plus propre visuellement.

------------------------------------------------------------------------

## 2. DwmSetWindowAttribute (optionnel)

API : `DwmSetWindowAttribute`

Permet d'utiliser certains attributs DWM modernes :

exemples : - coins arrondis - couleurs de frame - backdrop Windows 11
(Mica / Acrylic)

Utilité : - intéressant pour **backoffice / launcher** - probablement
inutile pour **overlay in‑game**

------------------------------------------------------------------------

## 3. Bonus possible

API : `SetWindowDisplayAffinity`

Peut empêcher certaines captures d'écran de capturer la fenêtre.

Attention : - ce n'est pas une protection parfaite - dépend de DWM et du
contexte

Peut être un sujet de blog séparé.

------------------------------------------------------------------------

## 4. Angle possible pour un article

Titre possible :

"Créer un overlay Windows propre en Java avec JNA et DWM"

Points clés : - wrapper Win32 avec JNA - gestion du HWND - overlay
click‑through - amélioration visuelle avec DWM

------------------------------------------------------------------------

## 5. TODO futur

Ajouter dans le repo :

-   interface `Dwmapi`
-   struct `MARGINS`
-   helper `enableGlassOverlay(HWND hwnd)`
-   exemple dans un module demo

Implémentation **pas prioritaire pour l'instant**.
