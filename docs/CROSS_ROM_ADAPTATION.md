# Cross-ROM Adaptation Guide

How to keep one `axdynamicbar` codebase building across YAAP, PixelOS, LineageOS,
crDroid, and other AOSP-derived ROMs.

---

## 1. The stable contract: Settings keys

SystemUI never talks to the Settings app directly — both sides only share
`Settings.System` string keys. That contract **is** the ROM-agnostic seam:

- `core/domain/AxDynamicBarSettings.kt` reads keys reactively (ContentObserver).
- Any ROM's Settings UI can write those keys with whatever preference classes
  that ROM ships.

**Complete key list** (see `docs/SETTINGS_KEYS.md` for types/defaults), including
the newest keys:

| Key | Purpose |
|---|---|
| `dynamic_island_enabled` | Master toggle |
| `dynamic_island_cutout_type` / `..._width_offset` / `..._height_offset` / `..._offset_x` / `..._offset_y` | Cutout geometry |
| `dynamic_island_hide_text_behind_cutout` | Dead-zone padding under camera |
| `dynamic_island_live_progress` | Hairline progress under countdowns |
| `dynamic_island_icon_only` | Compact pill shows icons only, no text |
| `dynamic_island_media_glow` | Beat-reactive album-tinted bottom glow |
| `dynamic_island_event_media` / `..._calls` / `..._ongoing` / `..._battery` / `..._timers` / `..._torch` / `..._biometrics` / `..._recording` / `..._notification` | Event gates |

> **Historical bug to avoid:** the compact `settings/` XML used to write
> `dynamic_island_event_call` and `dynamic_island_event_timer` (singular) while
> SystemUI reads `..._calls` / `..._timers` (plural), and the hide-text toggle
> was missing entirely. If toggles "don't work" on a new ROM, diff the written
> keys against `AxDynamicBarSettings.kt` first.

Both settings-XML variants in this repo now write identical keys; they differ
only in preference class names:

- `settings/` → YAAP in-tree Settings
  (`com.android.settings.yaap.preferences.*`, `CustomSeekBarPreference`,
  mounted from `MiscSettings`).
- `integration/` → generic/YASP-style Settings
  (`com.yasp.settings.preferences.*`).

---

## 2. Integration touchpoints (what actually differs per ROM)

Copy `core/` + `res/` unchanged. Then adapt these seams:

### 2.1 Dagger registration
`integration/frameworks_base/.../dagger/ReferenceSystemUIModule.java` — add
`DynamicIslandModule.class` to the module includes. On ROMs that rename this
file (LineageOS: `SystemUIModule`), append there instead. The module itself is
self-contained (`@SysUISingleton` bindings only).

### 2.2 Compose mount point
`integration/.../statusbar/pipeline/shared/ui/composable/StatusBarRoot.kt` —
`AxDynamicBarChip` overlaid `TopCenter`. ROMs without the compose status bar
pipeline (A15-era trees) need a `FrameLayout`/`ComposeView` host added to
`super_status_bar.xml` or the status bar window instead.

### 2.3 Platform hooks (best-effort, all guarded)
These snapshot files patch stock SystemUI classes; re-apply by hand when the
target ROM's versions drifted:

- `MediaSessionManager.kt` — emits `IslandEvent.Media` (art, color, position,
  speed, custom actions).
- `NotificationListener.java` — feeds notification/now-playing/sports events.
- `KeyguardIndicationController.java` — lock-screen indications.
- `KeyguardStatusBarViewController.java` — hides stock center content while the
  pill is active.
- `ScrimUtils.kt`, `WeakListenerManager.kt` — shared utils (some ROMs already
  ship equivalents; keep ours package-scoped to avoid clashes).
- Keyguard blueprints + `AxDynamicBarKeyguardChipSection` — lock-screen chip.
  Blueprint section ordering changes across AOSP versions; rebase rather than
  copy when in doubt.

### 2.4 Settings metrics
`DynamicIslandSettings.java` returns `MetricsProto.MetricsEvent.YASP` — that
constant only exists on YAAP. When porting: use the target ROM's metrics
category (or `MetricsEvent.VIEW_UNKNOWN`) and that ROM's preference base
classes, e.g.:

| ROM | Preference package | Notes |
|---|---|---|
| YAAP | `com.android.settings.yaap.preferences.*` | Entry from `MiscSettings`; use `CustomSeekBarPreference` |
| PixelOS | their `org.pixelos`/Pixel-style prefs | Strip `YASP` metric, keep search indexable |
| LineageOS | `lineageos.preference` / `org.lineageos.settings` | Use LineageSDK `SystemSetting*` prefs |

---

## 3. Device adaptation notes

- **Cutout type** defaults to `center`; Pixels and most 2024+ devices are
  center punch-hole. `left`/`right` types bypass the dead-zone spacer and use
  the scene-based chip content instead.
- **Dead-zone width** now reads `DisplayCutout.boundingRectTop` from window
  insets at runtime (falls back to 34dp) — per-device tuning should rarely be
  needed beyond the ±offset seekbars.
- **Beat glow** honors `ANIMATOR_DURATION_SCALE == 0` (remove animations) and
  settles to a static shimmer when media is paused.
- **Icon-only mode** suppresses text in both cutout layouts; a11y
  content descriptions remain intact for TalkBack.

---

## 4. Porting checklist (per ROM)

1. Copy `core/` into SystemUI, `res/values/*` into SystemUI res.
2. Register `DynamicIslandModule` in that ROM's root Dagger module.
3. Mount `AxDynamicBarChip` in the ROM's status-bar root composable.
4. Rebase the 8 platform hook files against the ROM's versions (compile and
   fix drift; do not blind-copy).
5. Settings: copy the matching XML variant, rewrite preference class names to
   the ROM's, fix the metrics category, add entry point + strings/arrays.
6. Verify every key written by the Settings UI against
   `AxDynamicBarSettings.kt` (the singular/plural trap).
7. Build SystemUI only first (`m SystemUI`) before a full image build.
8. Test matrix: media (playing/paused/no-art), timer + stopwatch, call chip,
   charging (wired/fast/wireless), torch, notification HUN, biometric,
   icon-only ON, hide-text ON/OFF, reduced motion ON, landscape.
