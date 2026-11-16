# v0.3.0 batch 2 notes

Scope: music playlist controls and journey map scalability. This batch does not add cloud sync, accounts, repeating reminders, reboot recovery, accessibility overhaul, or list paging.

Music: supports a local playlist of up to 20 copied audio files. The sound tab adds previous/next, a progress bar, and a cycle through loop off, repeat all and repeat one. The foreground service keeps playlist index and loop mode in local preferences, updates lock-screen transport controls, and advances to the next track when repeat-all is selected.

Journey map: supports more than 20 completed stations through 20-station pages, pinch zoom between 0.5x and 3x, and filters for all, today and this week. The map remains illustrative and is not GPS-based.

Verification status: prepared, not locally compiled or device tested. The existing push workflow may attempt a build after main is updated; success and artifact availability are not claimed.

## Acceptance checks
- Compile assembleDebug and lint.
- Add one and multiple audio files; verify playlist cap of 20 and copied private files.
- Test previous/next, progress seek, loop off/all/one, background playback, lock-screen controls and audio-focus loss.
- Map: zero, 1, 20, 21 and many stations; page navigation; pinch zoom; today/week/all filters.
- Verify existing calendar, photo, backup and reminder features still open.
