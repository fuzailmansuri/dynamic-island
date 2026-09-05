# Dynamic Island Settings Dictionary

All settings are stored in `Settings.System` and monitored reactively via `ContentObserver` in `AxDynamicBarSettings.kt`.

| Key | Type | Default | Description |
|---|---|---|---|
| `dynamic_island_enabled` | `Int` (0/1) | `1` | Master toggle to enable/disable Dynamic Island |
| `dynamic_island_cutout_type` | `String` | `"center"` | Hardware camera cutout position: `center`, `left`, or `right` |
| `dynamic_island_cutout_width_offset` | `Int` (dp) | `0` | Horizontal width fine-tuning offset (-50dp to +100dp) |
| `dynamic_island_cutout_height_offset` | `Int` (dp) | `0` | Vertical height fine-tuning offset (-50dp to +100dp) |
| `dynamic_island_cutout_offset_x` | `Int` (dp) | `0` | X-axis position offset (-50dp to +50dp) |
| `dynamic_island_cutout_offset_y` | `Int` (dp) | `0` | Y-axis position offset (-50dp to +50dp) |
| `dynamic_island_event_media` | `Int` (0/1) | `1` | Show now playing media, album art, and 4-bar equalizer |
| `dynamic_island_event_call` | `Int` (0/1) | `1` | Show ongoing phone calls and chronometer duration |
| `dynamic_island_event_battery` | `Int` (0/1) | `1` | Show charging telemetry (fast charge, wireless, level) |
| `dynamic_island_event_timer` | `Int` (0/1) | `1` | Show active countdown timers and stopwatches |
| `dynamic_island_event_torch` | `Int` (0/1) | `1` | Show flashlight indicator and brightness level |
| `dynamic_island_event_notification` | `Int` (0/1) | `1` | Show priority heads-up notifications |
| `dynamic_island_event_biometrics` | `Int` (0/1) | `1` | Show face unlock and fingerprint confirmation |
