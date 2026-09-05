package com.android.systemui.axdynamicbar.domain

import android.database.ContentObserver
import android.os.Handler
import android.os.UserHandle
import com.android.systemui.axdynamicbar.model.IslandEvent
import com.android.systemui.axdynamicbar.shared.EVENT_TYPE_IDS
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.dagger.qualifiers.Main
import com.android.systemui.util.settings.SystemSettings
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@SysUISingleton
class AxDynamicBarSettings @Inject constructor(
    @Main private val mainHandler: Handler,
    private val systemSettings: SystemSettings,
) {
    companion object {
        const val KEY_DYNAMIC_ISLAND_ENABLED = "dynamic_island_enabled"
        const val KEY_CUTOUT_TYPE = "dynamic_island_cutout_type"
        const val KEY_CUTOUT_WIDTH_OFFSET = "dynamic_island_cutout_width_offset"
        const val KEY_CUTOUT_HEIGHT_OFFSET = "dynamic_island_cutout_height_offset"

        const val KEY_EVENT_MEDIA = "dynamic_island_event_media"
        const val KEY_EVENT_CALLS = "dynamic_island_event_calls"
        const val KEY_EVENT_TORCH = "dynamic_island_event_torch"
        const val KEY_EVENT_BATTERY = "dynamic_island_event_battery"
        const val KEY_EVENT_TIMERS = "dynamic_island_event_timers"
        const val KEY_EVENT_BIOMETRICS = "dynamic_island_event_biometrics"
        const val KEY_EVENT_RECORDING = "dynamic_island_event_recording"

        const val KEY_KEYGUARD_ENABLED = "ax_dynamic_bar_keyguard_enabled"
        const val KEY_KEYGUARD_BATTERY_CHIP_MODE = "ax_dynamic_bar_keyguard_battery_chip_mode"

        // Legacy / compat constants
        const val KEY_ENABLED = KEY_DYNAMIC_ISLAND_ENABLED
        const val DYNAMIC_ISLAND_CUTOUT_WIDTH = KEY_CUTOUT_WIDTH_OFFSET
        const val DYNAMIC_ISLAND_CUTOUT_HEIGHT = KEY_CUTOUT_HEIGHT_OFFSET
        const val DYNAMIC_ISLAND_CUTOUT_OFFSET_X = "dynamic_island_cutout_offset_x"
        const val DYNAMIC_ISLAND_CUTOUT_OFFSET_Y = "dynamic_island_cutout_offset_y"
    }

    private val _isEnabled = MutableStateFlow(true)
    @get:JvmName("getIsEnabled") val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _isKeyguardEnabled = MutableStateFlow(true)
    val isKeyguardEnabled: StateFlow<Boolean> = _isKeyguardEnabled.asStateFlow()

    private val _keyguardBatteryChipMode = MutableStateFlow(1)
    val keyguardBatteryChipMode: StateFlow<Int> = _keyguardBatteryChipMode.asStateFlow()

    private val _cutoutType = MutableStateFlow("center")
    val cutoutType: StateFlow<String> = _cutoutType.asStateFlow()

    private val _cutoutWidthOffset = MutableStateFlow(0)
    val cutoutWidthOffset: StateFlow<Int> = _cutoutWidthOffset.asStateFlow()
    val cutoutWidth: StateFlow<Int> = _cutoutWidthOffset.asStateFlow()

    private val _cutoutHeightOffset = MutableStateFlow(0)
    val cutoutHeightOffset: StateFlow<Int> = _cutoutHeightOffset.asStateFlow()
    val cutoutHeight: StateFlow<Int> = _cutoutHeightOffset.asStateFlow()

    private val _cutoutOffsetX = MutableStateFlow(0)
    val cutoutOffsetX: StateFlow<Int> = _cutoutOffsetX.asStateFlow()

    private val _cutoutOffsetY = MutableStateFlow(0)
    val cutoutOffsetY: StateFlow<Int> = _cutoutOffsetY.asStateFlow()

    private val _eventMedia = MutableStateFlow(true)
    val eventMedia: StateFlow<Boolean> = _eventMedia.asStateFlow()

    private val _eventCalls = MutableStateFlow(true)
    val eventCalls: StateFlow<Boolean> = _eventCalls.asStateFlow()

    private val _eventTorch = MutableStateFlow(true)
    val eventTorch: StateFlow<Boolean> = _eventTorch.asStateFlow()

    private val _eventBattery = MutableStateFlow(true)
    val eventBattery: StateFlow<Boolean> = _eventBattery.asStateFlow()

    private val _eventTimers = MutableStateFlow(true)
    val eventTimers: StateFlow<Boolean> = _eventTimers.asStateFlow()

    private val _eventBiometrics = MutableStateFlow(true)
    val eventBiometrics: StateFlow<Boolean> = _eventBiometrics.asStateFlow()

    private val _eventRecording = MutableStateFlow(true)
    val eventRecording: StateFlow<Boolean> = _eventRecording.asStateFlow()

    private val _disabledEventTypes = MutableStateFlow<Set<String>>(emptySet())
    val disabledEventTypes: StateFlow<Set<String>> = _disabledEventTypes.asStateFlow()

    init {
        refresh()
    }

    private val settingsObserver =
        object : ContentObserver(mainHandler) {
            override fun onChange(selfChange: Boolean) {
                refresh()
            }
        }

    private var initialized = false

    fun init() {
        if (initialized) return
        initialized = true
        refresh()
        val allKeys = listOf(
            KEY_DYNAMIC_ISLAND_ENABLED,
            KEY_CUTOUT_TYPE,
            KEY_CUTOUT_WIDTH_OFFSET,
            KEY_CUTOUT_HEIGHT_OFFSET,
            KEY_EVENT_MEDIA,
            KEY_EVENT_CALLS,
            KEY_EVENT_TORCH,
            KEY_EVENT_BATTERY,
            KEY_EVENT_TIMERS,
            KEY_EVENT_BIOMETRICS,
            KEY_EVENT_RECORDING,
            KEY_KEYGUARD_ENABLED,
            KEY_KEYGUARD_BATTERY_CHIP_MODE,
            DYNAMIC_ISLAND_CUTOUT_OFFSET_X,
            DYNAMIC_ISLAND_CUTOUT_OFFSET_Y,
        )
        for (key in allKeys) {
            systemSettings.registerContentObserverForUserSync(
                key,
                false,
                settingsObserver,
                UserHandle.USER_ALL,
            )
        }
    }

    fun destroy() {
        if (!initialized) return
        initialized = false
        systemSettings.getContentResolver().unregisterContentObserver(settingsObserver)
    }

    private fun refresh() {
        _isEnabled.value =
            systemSettings.getIntForUser(KEY_DYNAMIC_ISLAND_ENABLED, 1, UserHandle.USER_CURRENT) == 1
        _isKeyguardEnabled.value =
            systemSettings.getIntForUser(KEY_KEYGUARD_ENABLED, 1, UserHandle.USER_CURRENT) == 1
        _keyguardBatteryChipMode.value =
            systemSettings.getIntForUser(KEY_KEYGUARD_BATTERY_CHIP_MODE, 1, UserHandle.USER_CURRENT)

        _cutoutType.value =
            systemSettings.getStringForUser(KEY_CUTOUT_TYPE, UserHandle.USER_CURRENT) ?: "center"
        _cutoutWidthOffset.value =
            systemSettings.getIntForUser(KEY_CUTOUT_WIDTH_OFFSET, 0, UserHandle.USER_CURRENT)
        _cutoutHeightOffset.value =
            systemSettings.getIntForUser(KEY_CUTOUT_HEIGHT_OFFSET, 0, UserHandle.USER_CURRENT)
        _cutoutOffsetX.value =
            systemSettings.getIntForUser(DYNAMIC_ISLAND_CUTOUT_OFFSET_X, 0, UserHandle.USER_CURRENT)
        _cutoutOffsetY.value =
            systemSettings.getIntForUser(DYNAMIC_ISLAND_CUTOUT_OFFSET_Y, 0, UserHandle.USER_CURRENT)

        val media = systemSettings.getIntForUser(KEY_EVENT_MEDIA, 1, UserHandle.USER_CURRENT) == 1
        val calls = systemSettings.getIntForUser(KEY_EVENT_CALLS, 1, UserHandle.USER_CURRENT) == 1
        val torch = systemSettings.getIntForUser(KEY_EVENT_TORCH, 1, UserHandle.USER_CURRENT) == 1
        val battery = systemSettings.getIntForUser(KEY_EVENT_BATTERY, 1, UserHandle.USER_CURRENT) == 1
        val timers = systemSettings.getIntForUser(KEY_EVENT_TIMERS, 1, UserHandle.USER_CURRENT) == 1
        val biometrics = systemSettings.getIntForUser(KEY_EVENT_BIOMETRICS, 1, UserHandle.USER_CURRENT) == 1
        val recording = systemSettings.getIntForUser(KEY_EVENT_RECORDING, 1, UserHandle.USER_CURRENT) == 1

        _eventMedia.value = media
        _eventCalls.value = calls
        _eventTorch.value = torch
        _eventBattery.value = battery
        _eventTimers.value = timers
        _eventBiometrics.value = biometrics
        _eventRecording.value = recording

        val disabled = mutableSetOf<String>()
        if (!media) disabled.add("media")
        if (!calls) {
            disabled.add("call")
            disabled.add("promoted_ongoing")
        }
        if (!torch) disabled.add("torch")
        if (!battery) disabled.add("charging")
        if (!timers) {
            disabled.add("timer")
            disabled.add("stopwatch")
            disabled.add("alarm")
        }
        if (!biometrics) disabled.add("biometric_unlock")
        if (!recording) disabled.add("audio_recording")

        _disabledEventTypes.value = disabled
    }

    fun isEventEnabled(event: IslandEvent): Boolean {
        return when (event) {
            is IslandEvent.Media -> _eventMedia.value
            is IslandEvent.AudioRecording -> _eventRecording.value
            is IslandEvent.AospChip -> {
                if (event.active.key == "ScreenRecord") _eventRecording.value
                else if (event.active.key.startsWith("callChip-")) _eventCalls.value
                else true
            }
            is IslandEvent.PromotedOngoing -> _eventCalls.value
            is IslandEvent.Torch -> _eventTorch.value
            is IslandEvent.Charging -> _eventBattery.value
            is IslandEvent.Timer, is IslandEvent.Stopwatch, is IslandEvent.Alarm -> _eventTimers.value
            is IslandEvent.BiometricUnlock -> _eventBiometrics.value
            else -> {
                val typeId = EVENT_TYPE_IDS[event::class.java] ?: return true
                typeId !in _disabledEventTypes.value
            }
        }
    }
}
