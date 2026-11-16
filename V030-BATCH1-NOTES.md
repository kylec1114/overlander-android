# v0.3.0 batch 1 notes

Scope: calendar accuracy, versioned backup with restore preview, and multi-photo management. This batch does not add cloud sync, accounts, broad photo access, contacts permission, repeating reminders, reboot reminder recovery, playlist controls, map zoom/filter, accessibility overhaul, or paging.

Calendar: imports selected instances for the next 30 days with title, begin/end, event timezone, all-day flag, location, description and recurrence rule. All-day events display a date-only range; overnight events display both dates and are labelled. Calendar import remains a local snapshot, not synchronization.

Photos: each ticket supports up to six selected images. Selected images are copied into app-private storage, downscaled to at most 1600 px and compressed as JPEG quality 82. Cards show up to three thumbnails; Manage photos lists all images and supports deletion. Legacy single-photo records are migrated to a photos array on load.

Backup: export uses format overlander version 3, includes creation time, task count, photo count and embedded readable photos. Import accepts version 2 and 3, validates IDs, titles and completion flags, restores photos into private storage, and shows a preview with task and photo counts before replacing current data. A local recovery snapshot is retained before replacement.

Fix: the rail-sound WAV writer now emits a valid RIFF/WAVE/fmt/data header; the prior v0.2.0 source contained an invalid escape sequence in that path.

Verification status: prepared, not locally compiled or device tested. The existing push workflow may attempt a build after main is updated; success and artifact availability are not claimed. UI label is v0.3.0; Gradle versionCode/versionName remain unchanged in this batch.

## Acceptance checks
- Compile assembleDebug and lint.
- Upgrade from v0.2.0; verify legacy photo migration and existing tasks.
- Calendar: all-day, overnight, timezone, location, description, recurrence, repeated import and permission denial.
- Photos: add one and multiple, cap at six, compress, thumbnail display, delete, unsupported image and low storage.
- Backup: export v3, import v2 and v3, preview counts, cancel, replace, recovery snapshot and malformed input.
- Rail sound: generate and play without crashing.
