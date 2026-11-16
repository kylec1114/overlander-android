# Overlander Retro APK 0.4.1

Independent HTML Android app: hk.overlander.retro. Old app data is not automatically migrated.

UI is stored losslessly as ui-source.lzma.b64. CI decodes it to app/src/main/assets/index.html, validates JavaScript and XML, builds with JDK 17 / Gradle 8.11.1 / SDK 35, verifies the APK signature and uploads a debug artifact. If UI source is missing, build is explicitly skipped, not a successful APK build.

No INTERNET permission, accounts, analytics or background media service. File chooser and JSON/ICS export use Android document pickers. Audio pauses when backgrounded. User-selected audio is session-only. Back up tickets and photos before uninstalling. CI debug signing keys may differ between builds; stable production upgrades require a persistent signing key.

Build command: gradle -p retro-apk :app:assembleDebug

Android 14 launch, file selection, export/import, audio mixing, pause/resume, rotation and small-screen layout still need device verification. A successful build is not a complete device test.
