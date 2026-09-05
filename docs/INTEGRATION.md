# AOSP ROM Integration Guide

This guide details how to integrate Dynamic Island into any Android 15, 16, or 17 custom ROM.

---

## Step 1: Copy Source Files
Copy the `core/` package into your ROM's SystemUI source tree:
```bash
cp -r core/* frameworks/base/packages/SystemUI/src/com/android/systemui/axdynamicbar/
```

Copy the resource files:
```bash
cp res/values/* frameworks/base/packages/SystemUI/res/values/
```

---

## Step 2: Wire Dagger Module
In `frameworks/base/packages/SystemUI/src/com/android/systemui/ReferenceSystemUIModule.java`:

```java
import com.android.systemui.axdynamicbar.dagger.DynamicIslandModule;

@Module(includes = {
    ...
    DynamicIslandModule.class,
})
public abstract class ReferenceSystemUIModule {
    ...
}
```

---

## Step 3: Mount Root Overlay in SystemUI Compose Tree
In `frameworks/base/packages/SystemUI/src/com/android/systemui/statusbar/ui/compose/StatusBarRoot.kt`:

```kotlin
import com.android.systemui.axdynamicbar.ui.AxDynamicBarManager
import com.android.systemui.axdynamicbar.ui.compose.AxDynamicBarChip

@Composable
fun StatusBarRoot(
    viewModel: StatusBarRootViewModel,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        // 1. Stock Status Bar Elements
        Row(modifier = Modifier.fillMaxWidth()) {
            StartSideContent(...)
            Spacer(modifier = Modifier.weight(1f))
            EndSideContent(...)
        }

        // 2. Dynamic Island Centered Hardware Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            AxDynamicBarChip()
        }
    }
}
```

---

## Step 4: Settings UI Integration
Copy `settings/res/` files into `packages/apps/Settings/res/`:
```bash
cp settings/res/xml/* packages/apps/Settings/res/xml/
cp settings/res/values/* packages/apps/Settings/res/values/
```

Include `dynamic_island_settings.xml` inside your ROM's customization dashboard (e.g. `Settings > System > Misc`).
