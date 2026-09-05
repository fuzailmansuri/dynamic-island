# Dynamic Island Architecture

## 1. System Overview
Dynamic Island for AOSP is built on a modern **Reactive Model-View-Intent (MVI)** architecture powered by Kotlin Coroutines, StateFlow, Dagger 2 Dependency Injection, and Jetpack Compose with Material 3 Expressive motions.

```
+-------------------------------------------------------------------+
|                        System Event Producers                     |
|  (MediaSession, Telecom, BatteryController, FlashlightController) |
+---------------------------------+---------------------------------+
                                  |
                                  v
+---------------------------------+---------------------------------+
|                    Data Layer: Island Managers                    |
| (MediaIslandManager, SystemIslandManager, TorchIslandManager, etc)|
+---------------------------------+---------------------------------+
                                  |
                                  v
+---------------------------------+---------------------------------+
|               Domain Layer: IslandEventRepository                 |
|     (Event Aggregator, Deduplication & StateFlow Broadcasting)    |
+---------------------------------+---------------------------------+
                                  |
                                  v
+---------------------------------+---------------------------------+
|                 Domain Layer: Chips Refiner                       |
|   (Priority Weighting, Event Suppression, Settings Enforcement)   |
+---------------------------------+---------------------------------+
                                  |
                                  v
+---------------------------------+---------------------------------+
|               Presentation Layer: Jetpack Compose                 |
|   (AxDynamicBarChip: Symmetrical 3-Part Pill + Deadzone Spacer)   |
+-------------------------------------------------------------------+
```

---

## 2. Hardware Camera Cutout Dead-Zone Architecture
On devices with a physical punch-hole display cutout (such as the center punch-hole on Nothing Phone 3a / 2a `asteroids`), drawing content continuously across the pill causes critical text, counters, and icons to be clipped behind the black camera lens.

Dynamic Island solves this with an architectural **3-Part Symmetrical Layout**:

```
+-----------------------------------------------------------------------+
|  [ Leading Slot ]    <--- [ Camera Cutout Spacer ] --->   [ Trailing Slot ]  |
|  (Left of Camera)          (Hardware Dead-Zone)          (Right of Camera)   |
|   - Album Artwork           Width: 28dp + offset          - Live Audio Wave  |
|   - App Notification        Zero Text / Icons Drawn       - Chronometer Time |
|   - Call Avatar             Sits Behind Physical Lens     - Event Counter    |
+-----------------------------------------------------------------------+
```

### Layout Specifications
- **Leading Slot (`CenterCutoutLeadingSlot`)**: Anchored to the left of the lens. Renders the primary identity of the event (16dp rounded album art, vector iconography, team badges).
- **Camera Cutout Dead-Zone Spacer**:
  ```kotlin
  Spacer(modifier = Modifier.width((28f + cutoutWidthSetting).coerceAtLeast(14f).dp))
  ```
  Guarantees that **zero pixels** of text or interactive components ever cross through the camera diameter.
- **Trailing Slot (`CenterCutoutTrailingSlot`)**: Anchored to the right of the lens. Renders real-time dynamic data (animated 4-bar equalizer waveform, countdown timers, call duration, battery percentage).

---

## 3. Flagship Physics & Animation Specifications
- **True Continuous Curvature**: Rendered using `RoundedCornerShape(percent = 50)` to produce an organic squircle/pill without angular distortion.
- **OLED Pure Black**: Background canvas locked to `#000000` (`Color.Black`) for zero light bleed on AMOLED displays.
- **Spring Physics**: Utilizes `MaterialTheme.motionScheme.defaultSpatialSpec()` and `FastOutSlowInEasing` for natural expansion and contraction.
- **Dual-Capsule Split Physics**: When concurrent events occur (e.g. active timer + background music), the island seamlessly splits off an independent satellite capsule on the right side.
- **Privacy Glow Rings**: Integrated Canvas perimeter shaders with dynamic color rings:
  - **Camera Active**: Emerald Green (`#10B981`)
  - **Microphone Active**: Amber Orange (`#F59E0B`)
  - **Screen Recording**: Rose Coral (`#F43F5E`)
