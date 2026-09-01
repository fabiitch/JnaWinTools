[![CI](https://github.com/fabien-occelli/JnaWinTools/actions/workflows/ci.yml/badge.svg)](https://github.com/fabien-occelli/JnaWinTools/actions/workflows/ci.yml)

## Backend natif

Ce projet utilise l'API Java **FFM / Panama** (`java.lang.foreign`, stable depuis JDK 22) pour
appeler directement les DLL Windows (user32, kernel32, dwmapi, shell32, ole32). **JNA n'est plus
utilisé** : il n'y a plus aucune dépendance `net.java.dev.jna` ni import `com.sun.jna`.

- Toolchain **Java 25**, build **Gradle 9.2.1**.
- Les appels natifs nécessitent l'accès natif : lancez la JVM avec
  `--enable-native-access=ALL-UNNAMED` (déjà configuré pour la tâche `test` dans `build.gradle`).
- Windows x64 uniquement (les tests dépendant du natif sont conditionnés à l'OS Windows).

later maybe

https://github.com/oshi/oshi


Quelques libs Windows-friendly utiles côté Java. Selon le besoin :

JNativeHook → hooks globaux clavier/souris (WH_KEYBOARD_LL / WH_MOUSE_LL) cross-platform. Pratique pour hotkeys sans garder le focus.

OSHI → infos système (CPU, RAM, GPU, process, monitors, températures). S’appuie sur WMI/PerfCounters sous Windows.

Waffle → SSO Windows (NTLM/Kerberos) côté serveur Java.

COM bridges :

com4j (léger, no-nonsense)

JACOB (plus ancien, mais solide)
Pour piloter COM/Office/OBS via COM, etc.

Windows Service :

WinSW (wrappe un jar en service Windows)

Apache Commons Daemon (Procrun)
