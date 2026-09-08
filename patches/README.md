# Patch bundles

Patch files are grouped by Android project root. Apply them from the matching project directory only after checking the target revision.

- `frameworks_base/`: historical/integration patches for `frameworks/base`
- `packages_apps_Settings/`: historical/integration patches for `packages/apps/Settings`
- `vendor_themes/`, `vendor_yaap/`: supporting integration patches

The current Dynamic Bar source snapshot is in `core/`, and current Settings/SystemUI integration snapshots are in `integration/`. Existing historical patches may not apply cleanly to a newer or already-modified tree; always use `git apply --check` first.
