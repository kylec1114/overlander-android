# v0.2.0 implementation notes

This source update replaces MainActivity while retaining the journey SharedPreferences tasks key and existing task fields. It adds a native MediaPlayer foreground service and MediaSession without an AndroidX dependency. No cloud sync, accounts, contacts permission, broad photo access or internet permission is added.

Implemented paths: long-press drag to fixed validation target; button fallback; dated stamp scale/fade; animated ticket-tail detachment; today's completed-ticket wallet; moving morning/sunset/night scenery; fictional task journey map with tappable first 20 stations and an accessible full list; runtime-authorized calendar selection and selected instance import for the next 30 days; background audio and lock-screen transport controls; persistent private music copy; private selected-photo copy; JSON export/import with embedded readable photos and replacement confirmation.

Task category/date/start/end fields are included. Existing approximate one-off reminders are retained. Daily/weekly repeating reminders, reminder recovery after reboot, focus timer and production-quality page transitions are NOT implemented by this commit. Calendar import is a local snapshot, not synchronization; all-day, overnight and timezone presentation still need device review. Map is illustrative, not GPS.

Limits: selected photo 4 MB, audio 64 MB, backup 32 MB and 10000 tasks. JSON includes readable photos, not audio. Unreadable photos cause export to fail visibly instead of silently omitting them. Imported photos are written into app-private storage; cancelled/failed restores can leave unreferenced copies. Old replaced photos/audio are not yet garbage-collected. An automatic local recovery_tasks snapshot is saved before replacement, but no recovery UI is provided. No data leaves the device except a user-selected export destination.

Verification status: code prepared, not locally compiled or device tested. The existing push workflow may attempt a build after main is updated; success and artifact availability are not claimed. UI version label is v0.2.0; Gradle versionCode/versionName have not been changed in this commit.

## Required acceptance checks
- Compile assembleDebug and lint with existing Gradle configuration.
- Upgrade over an existing install and verify tasks, notes, completion dates and legacy photo URIs.
- Drag/drop, cancel a drag, button completion, stamp/tail, restore completion and today's wallet across midnight.
- Rotate during a picker and during completion; background/resume animated scenery.
- Map with zero, one and over 20 stations; TalkBack full-list alternative.
- Calendar permission allow/deny/revoke, empty calendar, repeated import, recurring instances, all-day and overnight events.
- Audio selection, app background, lock screen, pause/play/stop, headphone unplug, focus loss, process kill and unavailable audio.
- Copy photo, delete original, restart app; test unsupported images and low storage.
- Export and restore with photos, malformed JSON, duplicate IDs, oversize files, cancellation and failed writes.
- Notification permission refusal, completed/deleted reminder cancellation and reboot limitation.
