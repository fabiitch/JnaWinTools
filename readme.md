


later maybe 

https://github.com/oshi/oshi


Yep, y’a quelques libs Windows-friendly utiles côté Java. Selon le besoin :

JNA Platform (tu l’as déjà) → mappings User32/Kernel32/Gdi32/Advapi32, registry via Advapi32Util, DPI/Window styles, etc.

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
