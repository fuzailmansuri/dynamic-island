# 🏝️ Dynamic Island for Android (AOSP)

[![Android](https://img.shields.io/badge/Android-15%20%7C%2016%20%7C%2017-3DDC84?logo=android&logoColor=white)](https://source.android.com/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203%20Expressive-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

An authentic, production-grade **Dynamic Island** implementation custom-engineered for Android (AOSP 15, 16, and 17). Designed with zero-jank 120Hz spring physics, continuous organic squircle curvature, physical camera cutout adaptability, and deep platform telemetry.

```
       +--------------------------------------------------------------+
       |   (●) [Album Art]   <--- CAMERA DEADZONE --->   |||| [02:45] |
       +--------------------------------------------------------------+
```

---

## ✨ Flagship Highlights

- **📐 Symmetrical 3-Part Pill Architecture**: Solves camera lens clipping. Divides the compact pill into a **Leading Slot** (album art/call avatars on the left), a **Hardware Dead-Zone Spacer** (matching the camera punch-hole width where zero text/graphics are drawn), and a **Trailing Slot** (real-time waveform/timer/counters on the right).
- **🎵 Real-Time 4-Bar Equalizer Waveform**: Smooth procedural audio visualizer animating synchronously with live playback state.
- **⚡ Dual-Capsule Split Physics**: When concurrent tasks run (e.g. background music + foreground countdown timer), the pill organically bifurcates into a primary pill and an independent satellite capsule.
- **🔋 Hardware Charging Telemetry**: Instant feedback distinguishing standard charging, high-wattage fast charging, and Qi/PMA wireless charging with animated percentages.
- **🟢 Privacy Glow Rings**: Integrated hardware canvas perimeter shaders that render vibrant emerald (Camera), amber (Microphone), and coral (Screen Recording) aura rings.
- **🖤 True OLED Black**: `#000000` canvas background prevents display panel power consumption on modern AMOLED / LTPO displays.
- **🛠️ Notch & Cutout Adaptability**: Fully adjustable for center, left, and right camera punch-holes with real-time fine-tuning seekbars (-50dp to +100dp).

---

## 🏛️ Architecture Overview

The system is organized into clean, decoupled layers following strict Android platform engineering standards:

```
dynamic-island/
├── core/
│   ├── dagger/          # Dagger 2 / Anvil dependency injection modules
│   ├── data/            # 10 dedicated system event managers & telemetry collectors
│   ├── domain/          # Priority weighting, deduping refiner, reactive settings
│   ├── model/           # Sealed hierarchy of Island events & UI states
│   ├── shared/          # Mathematical formatters, action triggers, physics tokens
│   └── ui/              # Complete Jetpack Compose UI engine & SceneTransitions
├── res/                 # Comprehensive string localization, colors, and IDs
├── settings/            # AOSP Settings dashboard XMLs, seekbars, and controllers
└── docs/                # Architecture specifications & Integration manuals
```

Detailed architectural diagrams and reactive flow documentation can be found in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

---

## 🚀 Quick Integration

To integrate this module into your custom AOSP ROM:

1. **Copy Source & Resources**:
   ```bash
   cp -r core/* <rom_root>/frameworks/base/packages/SystemUI/src/com/android/systemui/axdynamicbar/
   cp res/values/* <rom_root>/frameworks/base/packages/SystemUI/res/values/
   ```

2. **Register Dagger Module**:
   Add `DynamicIslandModule.class` to `ReferenceSystemUIModule.java`.

3. **Mount in Compose Tree**:
   Place `<AxDynamicBarChip />` with `Alignment.TopCenter` inside `StatusBarRoot.kt`.

4. **Settings UI**:
   Add `dynamic_island_settings.xml` into `packages/apps/Settings/res/xml/`.

Full step-by-step instructions and code snippets are available in [docs/INTEGRATION.md](docs/INTEGRATION.md).

---

## ⚙️ Settings Dictionary

All options are controlled via reactive `Settings.System` keys with instant live updates:

| Setting Key | Type | Default | Description |
|---|---|---|---|
| `dynamic_island_enabled` | `Int` (0/1) | `1` | Master toggle |
| `dynamic_island_cutout_type` | `String` | `"center"` | Cutout placement (`center`, `left`, `right`) |
| `dynamic_island_cutout_width_offset` | `Int` | `0` | Width adjustment (-50 to 100dp) |
| `dynamic_island_cutout_height_offset`| `Int` | `0` | Height adjustment (-50 to 100dp) |
| `dynamic_island_event_media` | `Int` (0/1) | `1` | Music playback & waveform |
| `dynamic_island_event_call` | `Int` (0/1) | `1` | Ongoing phone calls |
| `dynamic_island_event_battery` | `Int` (0/1) | `1` | Battery & charging telemetry |
| `dynamic_island_event_timer` | `Int` (0/1) | `1` | Timers and stopwatches |

See [docs/SETTINGS_KEYS.md](docs/SETTINGS_KEYS.md) for the complete dictionary.

---

## 📜 Credits & Provenance

- **Original Foundation**: [AxionAOSP](https://github.com/AxionAOSP) (`axdynamicbar`)
- **Refinements & Architecture**: [Fuzail Mansuri](https://github.com/fuzailmansuri)
- **Target Platform**: [Yet Another AOSP Project (YAAP)](https://github.com/yaap) & Nothing Phone (3a) / (2a) (`asteroids`)

---

## 📄 License
Licensed under the [Apache License, Version 2.0](LICENSE).
