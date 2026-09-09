/*
 * Copyright (C) 2024-2026 Yet Another AOSP Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.system;

import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

@SearchIndexable
public class DynamicIslandSettings extends DashboardFragment {

    private static final String TAG = "DynamicIslandSettings";
    private static final String KEY_RESET = "dynamic_island_reset";
    private static final String[] DYNAMIC_ISLAND_KEYS = {
            "dynamic_island_enabled",
            "dynamic_island_cutout_type",
            "dynamic_island_cutout_width_offset",
            "dynamic_island_cutout_height_offset",
            "dynamic_island_cutout_offset_x",
            "dynamic_island_cutout_offset_y",
            "dynamic_island_event_media",
            "dynamic_island_event_calls",
            "dynamic_island_event_ongoing",
            "dynamic_island_event_torch",
            "dynamic_island_event_battery",
            "dynamic_island_event_timers",
            "dynamic_island_event_biometrics",
            "dynamic_island_event_recording",
            "dynamic_island_event_notification",
            "dynamic_island_hide_text_behind_cutout",
            "dynamic_island_live_progress",
            "dynamic_island_icon_only",
            "dynamic_island_media_glow",
            "dynamic_island_tap_action",
            "dynamic_island_long_press_action",
            "dynamic_island_swipe_dismiss",
            "dynamic_island_landscape_mode",
            "dynamic_island_suppress_fullscreen",
            "dynamic_island_animation_style",
            "dynamic_island_collapse_timeout",
            "dynamic_island_scale",
            "ax_dynamic_bar_keyguard_enabled",
            "ax_dynamic_bar_keyguard_battery_chip_mode",
    };

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.dynamic_island_settings;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (KEY_RESET.equals(preference.getKey())) {
            writePreferenceClickMetric(preference);
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.dynamic_island_reset_dialog_title)
                    .setMessage(R.string.dynamic_island_reset_dialog_message)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.dynamic_island_reset_confirm, (dialog, which) -> {
                        for (String key : DYNAMIC_ISLAND_KEYS) {
                            Settings.System.putString(requireContext().getContentResolver(), key, null);
                        }
                        requireActivity().recreate();
                    })
                    .show();
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.YASP;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.dynamic_island_settings);
}
