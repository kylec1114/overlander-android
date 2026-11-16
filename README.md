# 漫遊日誌 / The Overlander

Native offline Android prototype 0.1.0. Source is not yet compiled or device-tested. Never treat this README as proof of a successful APK build.

## Scope
Ticket task create/edit/delete/complete/undo, local JSON preferences, daily boarding, end-of-day summary, completed journal, static illustrated day/sunset/night scenes, collectible station progress. Programmatically synthesized train audio (not sampled), local music selection, photo document selection, system calendar INSERT confirmation UI. No internet permission, Google login, contact access, or cloud sync.

## Reminders
Optional notification permission, approximate one-off reminders. Completing/deleting a ticket cancels its reminder. Receiver checks the current task before posting. Reminder schedules do NOT survive reboot. Battery restrictions, force-stop and disabled notifications may prevent or delay delivery. No exact-alarm or safety-critical timing guarantees.

## Limitations
No calendar reading/import, background playback, focus timer, full animated scene, actual drag/stamp animation, real journey map, categories/deadlines, photo copying, JSON backup/export or persistent music selection. Start-a-journey is currently a simple task dialog. Journal ordering follows reverse task insertion, not completion time. Photo access depends on the original document provider. Uninstall removes task data. This is an early test prototype, not a finished product.

## Build
JDK 17, Gradle 8.9, Android SDK platform 35. Android Gradle Plugin 8.7.3. No Gradle wrapper binary included. Run installed Gradle: `gradle --no-daemon :app:assembleDebug`.
GitHub Actions runs on pushes to main and manual dispatch. It may consume account quota. Successful builds upload `overlander-debug-apk` for 7 days. Open Actions, select the run, inspect logs, download artifact and extract the APK. Debug signing is for testing, not production distribution. Upload of source does not guarantee Actions is allowed by account policy or that compilation succeeds.

## Test checklist
Compile; install; verify persistence, completion/undo, delete/reminder cancellation, deny notification permission, select image/music, switch scenes, background audio pause, calendar unavailable, repeated playback and rotation. No phone testing completed yet.
