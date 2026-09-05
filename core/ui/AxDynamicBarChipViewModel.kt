package com.android.systemui.axdynamicbar.ui

import androidx.compose.ui.graphics.Color
import com.android.systemui.animation.Expandable
import com.android.systemui.axdynamicbar.domain.AxDynamicBarInteractor
import com.android.systemui.axdynamicbar.model.IslandEvent
import com.android.systemui.privacy.PrivacyItem
import com.android.systemui.privacy.PrivacyType
import com.android.systemui.shade.data.repository.PrivacyChipRepository
import com.android.systemui.statusbar.chips.ui.model.OngoingActivityChipModel
import com.android.systemui.biometrics.AuthController
import com.android.systemui.biometrics.domain.interactor.UdfpsOverlayInteractor
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.dagger.qualifiers.Application
import com.android.systemui.statusbar.KeyguardIndicationController
import com.android.systemui.statusbar.pipeline.battery.domain.interactor.BatteryInteractor
import com.android.systemui.statusbar.policy.BatteryController
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.math.roundToInt

enum class PrivacyGlowType {
    NONE,
    CAMERA,
    MICROPHONE,
    SCREEN_RECORD,
}

data class PrivacyGlowState(
    val isActive: Boolean = false,
    val type: PrivacyGlowType = PrivacyGlowType.NONE,
    val color: Color = Color.Transparent,
)

data class AxDynamicBarChipState(
    val event: IslandEvent,
    val secondaryEvent: IslandEvent? = null,
    val eventCount: Int,
    val pinnedIndex: Int,
    val allEvents: List<IslandEvent>,
)

data class KeyguardBatteryInfo(
    val level: Int,
    val isCharging: Boolean,
    val isPowerSave: Boolean,
    val isWireless: Boolean,
    val timeRemaining: String?,
)

data class AxDynamicBarChipBounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val screenWidth: Int,
) {
    val centerXFraction: Float
        get() =
            if (screenWidth > 0) {
                (((left + right) / 2f) / screenWidth).coerceIn(0f, 1f)
            } else {
                0.5f
            }
}

@OptIn(ExperimentalCoroutinesApi::class)
@SysUISingleton
class AxDynamicBarChipViewModel
@Inject
constructor(
    @Application private val applicationScope: CoroutineScope,
    val interactor: AxDynamicBarInteractor,
    batteryInteractor: BatteryInteractor,
    private val batteryController: BatteryController,
    authController: AuthController,
    udfpsOverlayInteractor: UdfpsOverlayInteractor,
    val keyguardExpansion: AxDynamicBarKeyguardExpansion,
    val statusBarExpansion: AxDynamicBarStatusBarExpansion,
    private val keyguardIndicationController: KeyguardIndicationController,
    private val privacyChipRepository: PrivacyChipRepository,
) {
    val isLowUdfps: StateFlow<Boolean> =
        udfpsOverlayInteractor.udfpsOverlayParams
            .map { params ->
                if (!authController.isUdfpsSupported) return@map false
                val sensorBottom = params.sensorBounds.bottom
                val displayHeight = params.naturalDisplayHeight
                if (displayHeight <= 0) return@map false
                sensorBottom > displayHeight * LOW_UDFPS_THRESHOLD
            }
            .distinctUntilChanged()
            .stateIn(applicationScope, SharingStarted.Eagerly, false)

    val chipState: StateFlow<AxDynamicBarChipState?> =
        interactor.uiState
            .map { uiState ->
                if (!uiState.shouldShow) return@map null
                val topEvent = uiState.topEvent ?: return@map null
                val secondaryEvent = uiState.activeEvents.firstOrNull { it.id != topEvent.id }
                    ?: uiState.events.firstOrNull { it.id != topEvent.id }
                AxDynamicBarChipState(
                    event = topEvent,
                    secondaryEvent = secondaryEvent,
                    eventCount = uiState.activeEvents.size,
                    pinnedIndex = uiState.pinnedEventIndex,
                    allEvents = uiState.events,
                )
            }
            .distinctUntilChanged()
            .stateIn(applicationScope, SharingStarted.Lazily, null)

    val privacyGlowState: StateFlow<PrivacyGlowState> =
        combine(
            privacyChipRepository.privacyItems,
            interactor.uiState,
        ) { items, uiState ->
            val hasCamera = items.any { it.privacyType == PrivacyType.TYPE_CAMERA && !it.paused }
            val hasMic = items.any { it.privacyType == PrivacyType.TYPE_MICROPHONE && !it.paused }
            val hasMediaProj = items.any { it.privacyType == PrivacyType.TYPE_MEDIA_PROJECTION && !it.paused }
            val hasAudioRecording = uiState.events.any { it is IslandEvent.AudioRecording }
            val hasScreenRecord = uiState.events.any { it is IslandEvent.AospChip && it.active.key == "ScreenRecord" }

            when {
                hasCamera -> PrivacyGlowState(
                    isActive = true,
                    type = PrivacyGlowType.CAMERA,
                    color = Color(0xFF00E676),
                )
                hasMic || hasAudioRecording -> PrivacyGlowState(
                    isActive = true,
                    type = PrivacyGlowType.MICROPHONE,
                    color = Color(0xFFFF9100),
                )
                hasMediaProj || hasScreenRecord -> PrivacyGlowState(
                    isActive = true,
                    type = PrivacyGlowType.SCREEN_RECORD,
                    color = Color(0xFFFF5252),
                )
                else -> PrivacyGlowState(isActive = false, type = PrivacyGlowType.NONE, color = Color.Transparent)
            }
        }
        .distinctUntilChanged()
        .stateIn(applicationScope, SharingStarted.Lazily, PrivacyGlowState())

    val isEnabled: StateFlow<Boolean> = interactor.settings.isEnabled
    val isKeyguardEnabled: StateFlow<Boolean> = interactor.settings.isKeyguardEnabled
    val keyguardBatteryChipMode: StateFlow<Int> = interactor.settings.keyguardBatteryChipMode

    val cutoutType: StateFlow<String> = interactor.settings.cutoutType
    val cutoutWidth: StateFlow<Int> = interactor.settings.cutoutWidth
    val cutoutHeight: StateFlow<Int> = interactor.settings.cutoutHeight
    val cutoutOffsetX: StateFlow<Int> = interactor.settings.cutoutOffsetX
    val cutoutOffsetY: StateFlow<Int> = interactor.settings.cutoutOffsetY

    val keyguardBatteryInfo: StateFlow<KeyguardBatteryInfo> =
        combine(
            batteryInteractor.level,
            batteryInteractor.isCharging,
            batteryInteractor.powerSave,
            batteryInteractor.batteryTimeRemainingEstimate,
        ) { level, charging, powerSave, timeRemaining ->
            KeyguardBatteryInfo(
                level = level ?: 0,
                isCharging = charging,
                isPowerSave = powerSave,
                isWireless = batteryController.isPluggedInWireless,
                timeRemaining = timeRemaining,
            )
        }.stateIn(
            applicationScope,
            SharingStarted.Lazily,
            KeyguardBatteryInfo(0, false, false, false, null),
        )

    // Re-compute charging string only when battery state changes. Polling this while idle is costly
    // because KeyguardIndicationController formats through Resources on the main thread.
    val batteryString: StateFlow<String> =
        keyguardBatteryInfo
            .map {
                if (it.isCharging) {
                    formatChargingString(keyguardIndicationController.powerChargingString)
                } else {
                    ""
                }
            }
            .distinctUntilChanged()
            .stateIn(applicationScope, SharingStarted.Lazily, "")

    val isOnKeyguard: StateFlow<Boolean> = interactor.isOnKeyguard

    val isKeyguardFadingAway: StateFlow<Boolean> = interactor.isKeyguardFadingAway

    val isBouncerShowing: StateFlow<Boolean> = interactor.isBouncerShowing

    private val _keyguardCarrierText = MutableStateFlow("")
    val keyguardCarrierText: StateFlow<String> = _keyguardCarrierText.asStateFlow()

    fun updateKeyguardCarrierText(text: String) {
        _keyguardCarrierText.value = text
    }

    private val _chipCenterXFraction = MutableStateFlow(0.5f)
    val chipCenterXFraction: StateFlow<Float> = _chipCenterXFraction.asStateFlow()

    private val _chipBounds = MutableStateFlow<AxDynamicBarChipBounds?>(null)
    val chipBounds: StateFlow<AxDynamicBarChipBounds?> = _chipBounds.asStateFlow()

    fun updateChipCenterX(fraction: Float) {
        _chipCenterXFraction.value = fraction
    }

    fun updateChipBounds(left: Float, top: Float, right: Float, bottom: Float, screenWidth: Float) {
        val bounds =
            AxDynamicBarChipBounds(
                left = left.roundToInt(),
                top = top.roundToInt(),
                right = right.roundToInt(),
                bottom = bottom.roundToInt(),
                screenWidth = screenWidth.roundToInt(),
            )
        if (_chipBounds.value != bounds) {
            _chipBounds.value = bounds
            _chipCenterXFraction.value = bounds.centerXFraction
        }
    }

    val isExpanded: StateFlow<Boolean> = statusBarExpansion.isExpanded

    val isKeyguardExpanded: StateFlow<Boolean> = keyguardExpansion.isExpanded

    fun cycleNext() = interactor.cycleNext()

    fun cyclePrev() = interactor.cyclePrev()

    fun pinEvent(event: IslandEvent) = interactor.pinEvent(event)

    fun dismissEvent(event: IslandEvent) = interactor.dismissEvent(event)

    fun togglePlayPause() = interactor.togglePlayPause()

    fun skipNext() = interactor.skipNext()

    fun skipPrev() = interactor.skipPrev()

    fun toggleTorch() = interactor.toggleTorch()

    fun launchNotificationFromKeyguard(event: IslandEvent.Notification) {
        interactor.launchNotificationDismissingKeyguard(event)
    }

    fun handleAospChipTap(event: IslandEvent.AospChip, expandable: Expandable): Boolean {
        val active = event.active
        return when (val behavior = active.clickBehavior) {
            is OngoingActivityChipModel.ClickBehavior.ShowHeadsUpNotification -> {
                behavior.onClick()
                true
            }
            is OngoingActivityChipModel.ClickBehavior.HideHeadsUpNotification -> {
                behavior.onClick()
                true
            }
            is OngoingActivityChipModel.ClickBehavior.ExpandAction -> {
                behavior.onClick(expandable)
                true
            }
            is OngoingActivityChipModel.ClickBehavior.None -> false
        }
    }

    companion object {
        private const val LOW_UDFPS_THRESHOLD = 0.93f
    }

    private fun formatChargingString(text: String?): String {
        val cleaned = text?.trim()
        return if (cleaned.isNullOrEmpty()) "" else cleaned
    }
}
