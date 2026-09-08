package com.android.systemui.axdynamicbar.ui.compose

import android.content.res.Configuration
import android.graphics.Rect
import android.view.HapticFeedbackConstants
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.android.systemui.common.shared.model.ContentDescription
import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.compose.animation.Expandable
import com.android.compose.animation.rememberExpandableController
import com.android.systemui.animation.Expandable as SystemUiExpandable
import com.android.compose.animation.scene.ContentKey
import com.android.compose.animation.scene.ElementKey
import com.android.compose.animation.scene.ElementMatcher
import com.android.compose.animation.scene.SceneKey
import com.android.compose.animation.scene.SceneTransitionLayout
import com.android.compose.animation.scene.TransitionBuilder
import com.android.compose.animation.scene.transitions
import com.android.systemui.axdynamicbar.model.IslandEvent
import com.android.systemui.axdynamicbar.model.RecordingState
import com.android.systemui.axdynamicbar.shared.formatCountdownLong
import com.android.systemui.statusbar.chips.ui.model.OngoingActivityChipModel
import com.android.systemui.statusbar.chips.ui.viewmodel.formatTimeRemainingData
import com.android.systemui.statusbar.chips.ui.viewmodel.rememberChronometerState
import com.android.systemui.statusbar.chips.ui.viewmodel.rememberTimeRemainingState
import com.android.systemui.statusbar.chips.ui.viewmodel.toFormatter
import com.android.systemui.axdynamicbar.shared.AlphaIconBg
import com.android.systemui.axdynamicbar.shared.AlphaSecondary
import com.android.systemui.axdynamicbar.shared.AlphaTertiary
import com.android.systemui.axdynamicbar.shared.AxDynamicBarTheme
import com.android.systemui.axdynamicbar.shared.CardBg
import com.android.systemui.axdynamicbar.shared.OnCardText
import com.android.systemui.axdynamicbar.shared.PillPrimary
import com.android.systemui.axdynamicbar.shared.ShapeXl
import com.android.systemui.axdynamicbar.shared.ShapeXs
import com.android.systemui.axdynamicbar.shared.SizeBadge
import com.android.systemui.axdynamicbar.shared.SpaceMd
import com.android.systemui.axdynamicbar.shared.SpaceSm
import com.android.systemui.axdynamicbar.shared.SpaceXs
import com.android.systemui.axdynamicbar.shared.TsBadge
import com.android.systemui.axdynamicbar.shared.chipAccentColorFor
import com.android.systemui.axdynamicbar.shared.compactMediaWaveColor
import com.android.systemui.axdynamicbar.shared.toScaledBitmap
import com.android.systemui.axdynamicbar.ui.AxDynamicBarChipState
import com.android.systemui.axdynamicbar.ui.AxDynamicBarChipViewModel
import com.android.systemui.res.R
import com.android.systemui.statusbar.chips.StatusBarChipsReturnAnimations
import kotlin.math.abs

private val ChipShape = RoundedCornerShape(percent = 50)
private val ChipHeight = 24.dp

@Composable
fun AxDynamicBarChip(
    viewModel: AxDynamicBarChipViewModel,
    modifier: Modifier = Modifier,
    ignoreKeyguard: Boolean = false,
    isFullscreen: Boolean = false,
) {
    AxDynamicBarTheme {
        AxDynamicBarChipContent(viewModel, modifier, ignoreKeyguard, isFullscreen)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AxDynamicBarChipContent(
    viewModel: AxDynamicBarChipViewModel,
    modifier: Modifier,
    ignoreKeyguard: Boolean,
    isFullscreen: Boolean,
) {
    val state by viewModel.chipState.collectAsStateWithLifecycle()
    val isOnKeyguard by viewModel.isOnKeyguard.collectAsStateWithLifecycle()
    val isEnabled by viewModel.isEnabled.collectAsStateWithLifecycle()
    val keyguardCarrier by viewModel.keyguardCarrierText.collectAsStateWithLifecycle()
    
    val carrierName = if (isOnKeyguard && ignoreKeyguard) keyguardCarrier.takeIf { it.isNotBlank() } else null
    val chipTextMaxWidth = dimensionResource(R.dimen.ongoing_activity_chip_max_text_width)
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidthPx = with(density) {
        configuration.screenWidthDp.dp.toPx()
    }
    val anchorView = LocalView.current
    var anchorBounds by remember { mutableStateOf<Rect?>(null) }
    val boundsExpandable = rememberBoundsExpandable(anchorBounds)

    val touchSlop = LocalViewConfiguration.current.touchSlop
    val longPressTimeoutMillis = LocalViewConfiguration.current.longPressTimeoutMillis
    val expandActionLabel = stringResource(R.string.status_bar_chip_custom_a11y_action_expand_notification)
    val transitionControllerFactory =
        (state?.event as? IslandEvent.AospChip)?.active?.transitionManager?.controllerFactory
    val expandableController =
        rememberExpandableController(
            color = Color.Transparent,
            shape = ChipShape,
            transitionControllerFactory = transitionControllerFactory,
        )

    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f),
        label = "chip_press_scale",
    )

    val motionScheme = MaterialTheme.motionScheme
    val cutoutType by viewModel.cutoutType.collectAsStateWithLifecycle()
    val landscapeMode by viewModel.landscapeMode.collectAsStateWithLifecycle()
    val suppressFullscreen by viewModel.suppressFullscreen.collectAsStateWithLifecycle()
    val animationStyle by viewModel.animationStyle.collectAsStateWithLifecycle()
    val islandScaleSetting by viewModel.scale.collectAsStateWithLifecycle()
    val userScale = (islandScaleSetting / 100f).coerceIn(0.5f, 1.5f)

    val isCenterCutout = cutoutType == "center"

    val isVisible = isEnabled &&
        state != null &&
        (ignoreKeyguard || !isOnKeyguard) &&
        (!isLandscape || landscapeMode) &&
        (!suppressFullscreen || !isFullscreen)

    val context = androidx.compose.ui.platform.LocalContext.current

    val enterSpec = when (animationStyle) {
        1 -> fadeIn(motionScheme.defaultEffectsSpec())
        2 -> fadeIn(tween(150)) + scaleIn(initialScale = 0.9f, animationSpec = tween(150))
        else -> fadeIn(motionScheme.defaultEffectsSpec()) + scaleIn(initialScale = 0.8f, animationSpec = motionScheme.defaultSpatialSpec())
    }
    val exitSpec = when (animationStyle) {
        1 -> fadeOut(motionScheme.fastEffectsSpec())
        2 -> fadeOut(tween(100)) + scaleOut(targetScale = 0.9f, animationSpec = tween(100))
        else -> fadeOut(motionScheme.fastEffectsSpec()) + scaleOut(targetScale = 0.8f, animationSpec = motionScheme.fastSpatialSpec())
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = enterSpec,
        exit = exitSpec,
        modifier = modifier
            .semantics {
                role = Role.Button
                contentDescription = when (val ev = state?.event) {
                    is IslandEvent.Media -> {
                        val state = if (ev.isPlaying) "Media" else "Paused"
                        val title = ev.track.ifBlank { context.getString(R.string.ax_dynamic_bar_music) }
                        "$state: $title${ev.artist.takeIf { it.isNotBlank() }?.let { " - $it" }.orEmpty()}"
                    }
                    is IslandEvent.Notification -> ev.title ?: ev.appName
                    is IslandEvent.Timer -> "Timer"
                    is IslandEvent.Stopwatch -> "Stopwatch"
                    is IslandEvent.Torch -> "Flashlight"
                    is IslandEvent.AudioRecording -> "Recording"
                    is IslandEvent.Charging -> "Charging ${ev.level}%"
                    is IslandEvent.Bluetooth -> "Bluetooth: ${ev.deviceTypeLabel}"
                    is IslandEvent.Hotspot -> "Hotspot"
                    is IslandEvent.AospChip -> {
                        val iconCd = when (val ic = ev.active.icon) {
                            is OngoingActivityChipModel.ChipIcon.StatusBarNotificationIcon -> ic.contentDescription
                            is OngoingActivityChipModel.ChipIcon.SingleColorIcon -> ic.impl.contentDescription
                            null -> null
                        }
                        (iconCd as? ContentDescription.Loaded)?.description
                            ?: (iconCd as? ContentDescription.Resource)?.let {
                                context.getString(it.res)
                            }
                            ?: ev.active.logName
                    }
                    else -> "Dynamic Island"
                }
                customActions = listOf(
                    CustomAccessibilityAction(expandActionLabel) {
                        anchorView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        state?.event?.let { ev ->
                            viewModel.executeLongPressAction(ev, boundsExpandable)
                        } ?: viewModel.statusBarExpansion.expand(boundsExpandable)
                        true
                    }
                )
            }
            .pointerInput(viewModel) {
                awaitEachGesture {
                    val down = awaitFirstDown(pass = PointerEventPass.Initial)
                    isPressed = true
                    val startX = down.position.x
                    val startY = down.position.y
                    var dragging = false
                    var totalDx = 0f
                    var isLongPress = false

                    // System long press timeout for stationary long press
                    val releasedOrMoved = withTimeoutOrNull(longPressTimeoutMillis) {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull() ?: break
                            if (!change.pressed) {
                                return@withTimeoutOrNull change
                            }
                            val dx = change.position.x - startX
                            val dy = change.position.y - startY
                            if (abs(dx) > touchSlop || abs(dy) > touchSlop) {
                                if (abs(dx) >= abs(dy)) {
                                    dragging = true
                                    totalDx = dx
                                    change.consume()
                                }
                                return@withTimeoutOrNull change
                            }
                        }
                        null
                    }

                    if (releasedOrMoved == null) {
                        // Timed out while holding down stationary -> Long press!
                        isLongPress = true
                        anchorView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        state?.event?.let { ev ->
                            viewModel.executeLongPressAction(ev, boundsExpandable)
                        } ?: viewModel.statusBarExpansion.expand(boundsExpandable)
                        // Wait for pointer release before finishing gesture
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull() ?: break
                            if (!change.pressed) {
                                change.consume()
                                break
                            }
                        }
                        isPressed = false
                    } else if (releasedOrMoved.pressed) {
                        // Moved beyond touch slop before timeout -> Dragging or gesture abort
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull() ?: break
                            if (!change.pressed) {
                                if (dragging) {
                                    change.consume()
                                    anchorView.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    if (totalDx > 0) viewModel.cyclePrev()
                                    else viewModel.cycleNext()
                                }
                                break
                            }
                            if (dragging) {
                                totalDx = change.position.x - startX
                                change.consume()
                            }
                        }
                        isPressed = false
                    } else {
                        // Released before timeout and within slop -> Tap!
                        isPressed = false
                        releasedOrMoved.consume()
                        anchorView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        val current = state?.event
                        if (current is IslandEvent.AospChip) {
                            if (!viewModel.handleAospChipTap(current, SystemUiExpandable(expandableController.transitionSource))) {
                                viewModel.executeTapAction(current, SystemUiExpandable(expandableController.transitionSource))
                            }
                        } else if (current != null) {
                            viewModel.executeTapAction(current, SystemUiExpandable(expandableController.transitionSource))
                        }
                    }
                }
            }
    ) {
        state?.let { chipState ->
            val displayEvent = chipState.event
            val isAlert = false
            val event = displayEvent
            val chipVisibilityModifier =
                if (
                    (displayEvent as? IslandEvent.AospChip)
                        ?.active
                        ?.transitionManager
                        ?.hideChipForTransition == true
                ) {
                    Modifier.graphicsLayer { alpha = 0f }
                } else {
                    Modifier
                }
            val rawAccent = chipAccentColorFor(event)
            val accent by animateColorAsState(
                rawAccent,
                MaterialTheme.motionScheme.fastEffectsSpec(),
                label = "accent",
            )
            val contentColor by animateColorAsState(
                OnCardText,
                MaterialTheme.motionScheme.fastEffectsSpec(),
                label = "content",
            )
            val cutoutType by viewModel.cutoutType.collectAsStateWithLifecycle()
            val cutoutWidthSetting by viewModel.cutoutWidth.collectAsStateWithLifecycle()
            val cutoutHeightSetting by viewModel.cutoutHeight.collectAsStateWithLifecycle()
            val cutoutOffsetXSetting by viewModel.cutoutOffsetX.collectAsStateWithLifecycle()
            val cutoutOffsetYSetting by viewModel.cutoutOffsetY.collectAsStateWithLifecycle()
            val hideTextBehindCutout by viewModel.hideTextBehindCutout.collectAsStateWithLifecycle()

            val privacyGlowState by viewModel.privacyGlowState.collectAsStateWithLifecycle()
            val glowTransition = rememberInfiniteTransition(label = "privacy_glow_pulse")
            val glowPulseAlpha by glowTransition.animateFloat(
                initialValue = 0.40f,
                targetValue = 0.95f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "privacy_glow_alpha",
            )

            val privacyGlowModifier = if (privacyGlowState.isActive) {
                Modifier.drawWithContent {
                    drawContent()
                    val strokeW = 1.6.dp.toPx()
                    val glowColor = privacyGlowState.color.copy(alpha = glowPulseAlpha)
                    val outerStrokeW = 2.4.dp.toPx()
                    // Soft aura
                    drawRoundRect(
                        color = glowColor.copy(alpha = glowPulseAlpha * 0.35f),
                        topLeft = Offset(-1.5.dp.toPx(), -1.5.dp.toPx()),
                        size = Size(size.width + 3.dp.toPx(), size.height + 3.dp.toPx()),
                        cornerRadius = CornerRadius((size.height + 3.dp.toPx()) / 2f),
                        style = Stroke(width = outerStrokeW),
                    )
                    // Sharp contour ring
                    drawRoundRect(
                        color = glowColor,
                        topLeft = Offset(strokeW / 2f, strokeW / 2f),
                        size = Size(size.width - strokeW, size.height - strokeW),
                        cornerRadius = CornerRadius((size.height - strokeW) / 2f),
                        style = Stroke(width = strokeW),
                    )
                }
            } else {
                Modifier
            }

            val chipHeightDp = ((ChipHeight.value + cutoutHeightSetting) * userScale).coerceIn(16f, 48f).dp
            val chipMinWidthDp =
                if (cutoutType == "center") {
                    ((72f + cutoutWidthSetting) * userScale).coerceAtLeast(38f).dp
                } else if (cutoutWidthSetting != 0) {
                    ((48f + cutoutWidthSetting) * userScale).coerceAtLeast(24f).dp
                } else {
                    Dp.Unspecified
                }
            val chipMaxWidthDp =
                if (cutoutType == "center") {
                    ((180f + cutoutWidthSetting) * userScale).coerceIn(90f, 320f).dp
                } else {
                    ((120f + cutoutWidthSetting) * userScale).coerceIn(60f, 220f).dp
                }
            val safeOffsetX = cutoutOffsetXSetting.coerceIn(-50, 50)
            val safeOffsetY = cutoutOffsetYSetting.coerceIn(-50, 50)

            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment =
                    when (cutoutType) {
                        "left" -> Alignment.CenterStart
                        "right" -> Alignment.CenterEnd
                        else -> Alignment.Center
                    },
            ) {
                Row(
                    modifier =
                        Modifier.onGloballyPositioned { coords ->
                            val bounds = coords.screenBounds(anchorView)
                            anchorBounds = bounds
                            if (screenWidthPx > 0f) {
                                viewModel.updateChipBounds(
                                    bounds.left.toFloat(),
                                    bounds.top.toFloat(),
                                    bounds.right.toFloat(),
                                    bounds.bottom.toFloat(),
                                    screenWidthPx,
                                )
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box {
                        Row(
                            modifier =
                                Modifier.height(chipHeightDp)
                                    .widthIn(min = chipMinWidthDp, max = chipMaxWidthDp)
                                    .offset(x = safeOffsetX.dp, y = safeOffsetY.dp)
                                    .graphicsLayer {
                                        scaleX = pressScale
                                        scaleY = pressScale
                                    }
                                    .then(chipVisibilityModifier)
                                    .then(privacyGlowModifier)
                                    .clip(ChipShape)
                                    .background(CardBg)
                                    .padding(
                                        start = SpaceSm,
                                        end = if (cutoutType == "center") SpaceSm else SpaceMd,
                                    ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (cutoutType == "center") {
                                CenterCutoutCompactPillContent(
                                    event = event,
                                    chipState = chipState,
                                    contentColor = contentColor,
                                    accent = accent,
                                    cutoutWidthSetting = cutoutWidthSetting,
                                    hideTextBehindCutout = hideTextBehindCutout,
                                    chipTextMaxWidth = chipTextMaxWidth,
                                    carrierName = carrierName,
                                )
                            } else {
                                if (carrierName != null) {
                                    Text(
                                        text = carrierName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = contentColor.copy(alpha = AlphaSecondary),
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 56.dp),
                                    )
                                    Text(
                                        text = " · ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = contentColor.copy(alpha = AlphaTertiary),
                                    )
                                }
                                ChipEventSceneContent(
                                    event = event,
                                    isAlert = isAlert,
                                    chipState = chipState,
                                    contentColor = contentColor,
                                    accent = accent,
                                    chipTextMaxWidth = chipTextMaxWidth,
                                    modifier = Modifier.weight(1f, fill = false),
                                )
                            }
                        }
                        if (event is IslandEvent.AospChip) {
                            Expandable(
                                controller = expandableController,
                                modifier = Modifier.matchParentSize().graphicsLayer { alpha = 0f },
                                onClick = null,
                                useModifierBasedImplementation = StatusBarChipsReturnAnimations.isEnabled,
                                defaultMinSize = false,
                            ) {
                                Box(Modifier.fillMaxSize())
                            }
                        }
                    }

                    // Secondary Satellite Capsule for Dual-Capsule Split Physics
                    AnimatedVisibility(
                        visible = chipState.secondaryEvent != null,
                        enter = fadeIn(motionScheme.defaultEffectsSpec()) + scaleIn(initialScale = 0.5f, animationSpec = motionScheme.defaultSpatialSpec()),
                        exit = fadeOut(motionScheme.fastEffectsSpec()) + scaleOut(targetScale = 0.5f, animationSpec = motionScheme.fastSpatialSpec()),
                    ) {
                        chipState.secondaryEvent?.let { secondaryEvent ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(Modifier.width(6.dp))
                                SatelliteCapsule(
                                    secondaryEvent = secondaryEvent,
                                    chipHeightDp = chipHeightDp,
                                    privacyGlowModifier = privacyGlowModifier,
                                    modifier = Modifier.offset(x = safeOffsetX.dp, y = safeOffsetY.dp)
                                        .pointerInput(secondaryEvent) {
                                            awaitEachGesture {
                                                val down = awaitFirstDown(pass = PointerEventPass.Initial)
                                                val startX = down.position.x
                                                val startY = down.position.y
                                                while (true) {
                                                    val pointerEvent = awaitPointerEvent(PointerEventPass.Initial)
                                                    val change = pointerEvent.changes.firstOrNull() ?: break
                                                    if (!change.pressed) {
                                                        change.consume()
                                                        anchorView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                        viewModel.pinEvent(secondaryEvent)
                                                        break
                                                    }
                                                    val dx = abs(change.position.x - startX)
                                                    val dy = abs(change.position.y - startY)
                                                    if (dx > touchSlop || dy > touchSlop) break
                                                }
                                            }
                                        },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private object ChipEventScenes {
    val Alert = SceneKey("ax_dynamic_bar_chip_alert")
    val Media = SceneKey("ax_dynamic_bar_chip_media")
    val Sports = SceneKey("ax_dynamic_bar_chip_sports")
    val AospChip = SceneKey("ax_dynamic_bar_chip_aosp")
    val Timer = SceneKey("ax_dynamic_bar_chip_timer")
    val Stopwatch = SceneKey("ax_dynamic_bar_chip_stopwatch")
    val AudioRecording = SceneKey("ax_dynamic_bar_chip_audio_recording")
    val Notification = SceneKey("ax_dynamic_bar_chip_notification")
    val AppSwitch = SceneKey("ax_dynamic_bar_chip_app_switch")
    val Default = SceneKey("ax_dynamic_bar_chip_default")
    val All = listOf(
        Alert,
        Media,
        Sports,
        AospChip,
        Timer,
        Stopwatch,
        AudioRecording,
        Notification,
        AppSwitch,
        Default,
    )
}

private object ChipEventElements {
    val AlertContent = ElementKey("ax_dynamic_bar_chip_alert_content")
    val MediaContent = ElementKey("ax_dynamic_bar_chip_media_content")
    val SportsContent = ElementKey("ax_dynamic_bar_chip_sports_content")
    val AospChipContent = ElementKey("ax_dynamic_bar_chip_aosp_content")
    val TimerContent = ElementKey("ax_dynamic_bar_chip_timer_content")
    val StopwatchContent = ElementKey("ax_dynamic_bar_chip_stopwatch_content")
    val AudioRecordingContent = ElementKey("ax_dynamic_bar_chip_audio_recording_content")
    val NotificationContent = ElementKey("ax_dynamic_bar_chip_notification_content")
    val AppSwitchContent = ElementKey("ax_dynamic_bar_chip_app_switch_content")
    val DefaultContent = ElementKey("ax_dynamic_bar_chip_default_content")
    val All = listOf(
        AlertContent,
        MediaContent,
        SportsContent,
        AospChipContent,
        TimerContent,
        StopwatchContent,
        AudioRecordingContent,
        NotificationContent,
        AppSwitchContent,
        DefaultContent,
    )
    val Content =
        object : ElementMatcher {
            override fun matches(key: ElementKey, content: ContentKey): Boolean = key in All
        }
}

private val ChipDefaultSceneElements = listOf(
    ChipEventScenes.AospChip to ChipEventElements.AospChipContent,
    ChipEventScenes.Timer to ChipEventElements.TimerContent,
    ChipEventScenes.Stopwatch to ChipEventElements.StopwatchContent,
    ChipEventScenes.AudioRecording to ChipEventElements.AudioRecordingContent,
    ChipEventScenes.Notification to ChipEventElements.NotificationContent,
    ChipEventScenes.AppSwitch to ChipEventElements.AppSwitchContent,
    ChipEventScenes.Default to ChipEventElements.DefaultContent,
)

private val ChipEventTransitions = transitions {
    ChipEventScenes.All.forEach { from(it) { chipEventTransition() } }
}

private fun TransitionBuilder.chipEventTransition() {
    fade(ChipEventElements.Content)
}

@Composable
private fun ChipEventSceneContent(
    event: IslandEvent,
    isAlert: Boolean,
    chipState: AxDynamicBarChipState,
    contentColor: Color,
    accent: Color,
    chipTextMaxWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val targetScene = chipEventSceneFor(event, isAlert)
    val sceneState = rememberDynamicBarEventSceneState(targetScene, event, ChipEventTransitions)

    NoOpBackDispatcherOwner {
        SceneTransitionLayout(state = sceneState.layoutState, modifier = modifier) {
            scene(ChipEventScenes.Alert) {
                (sceneState.eventFor(ChipEventScenes.Alert) as? IslandEvent.Notification)?.let {
                    ChipAlertEventContent(
                        it,
                        contentColor,
                        chipTextMaxWidth,
                        Modifier.element(ChipEventElements.AlertContent),
                    )
                }
            }
            scene(ChipEventScenes.Media) {
                (sceneState.eventFor(ChipEventScenes.Media) as? IslandEvent.Media)?.let {
                    ChipDefaultEventContent(
                        it,
                        chipState,
                        contentColor,
                        accent,
                        chipTextMaxWidth,
                        Modifier.element(ChipEventElements.MediaContent),
                    )
                }
            }
            scene(ChipEventScenes.Sports) {
                (sceneState.eventFor(ChipEventScenes.Sports) as? IslandEvent.Sports)?.let {
                    ChipSportsEventContent(
                        it,
                        contentColor,
                        Modifier.element(ChipEventElements.SportsContent),
                    )
                }
            }
            ChipDefaultSceneElements.forEach { (sceneKey, elementKey) ->
                scene(sceneKey) {
                    sceneState.eventFor(sceneKey)?.let {
                        ChipDefaultEventContent(
                            it,
                            chipState,
                            contentColor,
                            accent,
                            chipTextMaxWidth,
                            Modifier.element(elementKey),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipAlertEventContent(
    event: IslandEvent.Notification,
    contentColor: Color,
    chipTextMaxWidth: Dp,
    modifier: Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        event.appIcon?.let { icon ->
            Image(
                bitmap = icon.toScaledBitmap(16.dp),
                contentDescription = null,
                modifier = Modifier.size(16.dp).clip(ShapeXs),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(SpaceXs))
        }
        Text(
            text = event.appName ?: "",
            style = PillPrimary,
            color = contentColor,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = chipTextMaxWidth).basicMarquee(iterations = 1),
        )
    }
}

@Composable
private fun ChipSportsEventContent(
    event: IslandEvent.Sports,
    contentColor: Color,
    modifier: Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        StatusBarSportsTeamBadge(event.team1Name, event.team1Icon, contentColor)
        Spacer(Modifier.width(SpaceXs))
        Text(
            if (event.score1.isNotEmpty()) "${event.score1} - ${event.score2}"
            else stringResource(R.string.ax_dynamic_bar_sports_vs),
            style = PillPrimary,
            color = contentColor,
            maxLines = 1,
            softWrap = false,
        )
        Spacer(Modifier.width(SpaceXs))
        StatusBarSportsTeamBadge(event.team2Name, event.team2Icon, contentColor)
    }
}

@Composable
private fun ChipDefaultEventContent(
    event: IslandEvent,
    chipState: AxDynamicBarChipState,
    contentColor: Color,
    accent: Color,
    chipTextMaxWidth: Dp,
    modifier: Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        PillEventIcon(event, tint = contentColor, animated = false)
        Spacer(Modifier.width(SpaceXs))
        if (event is IslandEvent.Media) {
            CompactAudioWaveformVisualizer(
                isPlaying = event.isPlaying,
                color = compactMediaWaveColor(event),
                modifier = Modifier.size(width = 10.dp, height = 8.dp),
            )
        } else {
            PillEventText(
                event,
                Modifier.weight(1f, fill = false).widthIn(max = chipTextMaxWidth),
                overrideColor = contentColor,
            )
        }
        if (chipState.secondaryEvent == null) {
            ChipEventCountBadge(chipState, accent, contentColor)
        }
    }
}

@Composable
private fun CenterCutoutCompactPillContent(
    event: IslandEvent,
    chipState: AxDynamicBarChipState,
    contentColor: Color,
    accent: Color,
    cutoutWidthSetting: Int,
    hideTextBehindCutout: Boolean,
    chipTextMaxWidth: Dp,
    carrierName: String?,
) {
    // 1. Leading Slot (left of camera): Event icon, album art, call avatar
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (carrierName != null) {
            Text(
                text = carrierName,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = AlphaSecondary),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 48.dp),
            )
            Text(
                text = " · ",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = AlphaTertiary),
            )
        }
        CenterCutoutLeadingSlot(event, contentColor)
    }

    // 2. Camera Cutout Dead-Zone Spacer: zero text/graphics drawn under physical lens when enabled
    if (hideTextBehindCutout) {
        Spacer(modifier = Modifier.width((34f + cutoutWidthSetting).coerceAtLeast(16f).dp))
    } else {
        Spacer(modifier = Modifier.width(SpaceXs))
    }

    // 3. Trailing Slot (right of camera): Animated equalizer bars, call timer, countdown, badge text
    Row(verticalAlignment = Alignment.CenterVertically) {
        CenterCutoutTrailingSlot(
            event = event,
            chipState = chipState,
            contentColor = contentColor,
            accent = accent,
            chipTextMaxWidth = chipTextMaxWidth,
        )
    }
}

@Composable
private fun CenterCutoutLeadingSlot(
    event: IslandEvent,
    contentColor: Color,
) {
    val a11yDesc = when (event) {
        is IslandEvent.Media -> if (event.isPlaying) stringResource(R.string.ax_dynamic_bar_media_playing) else stringResource(R.string.ax_dynamic_bar_media_paused)
        is IslandEvent.Notification -> event.appName.ifEmpty { stringResource(R.string.ax_dynamic_bar_open) }
        is IslandEvent.Sports -> "${event.team1Name} vs ${event.team2Name}"
        else -> stringResource(R.string.ax_dynamic_bar_desc_island)
    }

    Box(modifier = Modifier.semantics {
        contentDescription = a11yDesc
        role = Role.Button
    }) {
        when (event) {
            is IslandEvent.Media -> PillEventIcon(event, tint = contentColor, animated = false)
            is IslandEvent.Notification -> {
                if (event.appIcon != null) {
                    Image(
                        bitmap = event.appIcon.toScaledBitmap(16.dp),
                        contentDescription = a11yDesc,
                        modifier = Modifier.size(16.dp).clip(ShapeXs),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    PillEventIcon(event, tint = contentColor, animated = false)
                }
            }
            is IslandEvent.Sports -> {
                StatusBarSportsTeamBadge(event.team1Name, event.team1Icon, contentColor)
            }
            else -> {
                PillEventIcon(event, tint = contentColor, animated = false)
            }
        }
    }
}

@Composable
private fun CenterCutoutTrailingSlot(
    event: IslandEvent,
    chipState: AxDynamicBarChipState,
    contentColor: Color,
    accent: Color,
    chipTextMaxWidth: Dp,
) {
    when (event) {
        is IslandEvent.Media -> {
            CompactAudioWaveformVisualizer(
                isPlaying = event.isPlaying,
                color = compactMediaWaveColor(event),
                modifier = Modifier.size(width = 10.dp, height = 8.dp),
            )
            if (chipState.secondaryEvent == null && chipState.eventCount > 1) {
                ChipEventCountBadge(chipState, accent, contentColor)
            }
        }
        is IslandEvent.AospChip -> {
            when (val c = event.active.content) {
                is OngoingActivityChipModel.Content.Timer -> {
                    val timerState =
                        rememberChronometerState(
                            chronometer = c.value,
                            formatter = c.format.toFormatter(),
                            timeSource = c.timeSource,
                        )
                    timerState.currentTimeText?.let { text ->
                        Text(
                            text = text,
                            style = PillPrimary,
                            color = contentColor,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
                is OngoingActivityChipModel.Content.Countdown -> {
                    Text(
                        text = formatCountdownLong(c.secondsUntilStarted * 1000L),
                        style = PillPrimary,
                        color = contentColor,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
                is OngoingActivityChipModel.Content.ShortTimeDelta -> {
                    val deltaState =
                        rememberTimeRemainingState(
                            futureTimeMillis = c.time,
                            timeSource = c.timeSource,
                        )
                    val text = deltaState.timeRemainingData?.let { formatTimeRemainingData(it) } ?: ""
                    if (text.isNotBlank()) {
                        Text(
                            text = text,
                            style = PillPrimary,
                            color = contentColor,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
                is OngoingActivityChipModel.Content.Text -> {
                    Text(
                        text = c.text,
                        style = PillPrimary,
                        color = contentColor,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = chipTextMaxWidth),
                    )
                }
                is OngoingActivityChipModel.Content.TextVariants -> {
                    val text = c.textVariants.firstOrNull() ?: ""
                    if (text.isNotBlank()) {
                        Text(
                            text = text,
                            style = PillPrimary,
                            color = contentColor,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = chipTextMaxWidth),
                        )
                    }
                }
                is OngoingActivityChipModel.Content.IconOnly -> {}
            }
            if (chipState.secondaryEvent == null && chipState.eventCount > 1) {
                ChipEventCountBadge(chipState, accent, contentColor)
            }
        }
        is IslandEvent.Timer -> {
            if (event.endTimeMs > 0L) {
                if (event.isPaused) {
                    Text(
                        text = stringResource(R.string.ax_dynamic_bar_paused),
                        style = PillPrimary,
                        color = contentColor,
                        maxLines = 1,
                        softWrap = false,
                    )
                } else {
                    var remainingMs by
                        remember(event.id, event.endTimeMs, event.isPaused) {
                            mutableLongStateOf((event.endTimeMs - System.currentTimeMillis()).coerceAtLeast(0L))
                        }
                    LaunchedEffect(event.id, event.endTimeMs, event.isPaused) {
                        while (isActive && remainingMs > 0L) {
                            delay(500)
                            remainingMs = (event.endTimeMs - System.currentTimeMillis()).coerceAtLeast(0L)
                        }
                    }
                    Text(
                        text = formatCountdownLong(remainingMs),
                        style = PillPrimary,
                        color = contentColor,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            } else {
                Text(
                    text = event.label.ifEmpty { stringResource(R.string.ax_dynamic_bar_timer) },
                    style = PillPrimary,
                    color = contentColor,
                    maxLines = 1,
                    softWrap = false,
                )
            }
            if (chipState.secondaryEvent == null && chipState.eventCount > 1) {
                ChipEventCountBadge(chipState, accent, contentColor)
            }
        }
        is IslandEvent.Stopwatch -> {
            if (!event.isRunning) {
                Text(
                    text = stringResource(R.string.ax_dynamic_bar_paused),
                    style = PillPrimary,
                    color = contentColor,
                    maxLines = 1,
                    softWrap = false,
                )
            } else {
                var elapsedMs by
                    remember(event.id, event.startTimeMs, event.isRunning) {
                        mutableLongStateOf((System.currentTimeMillis() - event.startTimeMs).coerceAtLeast(0L))
                    }
                LaunchedEffect(event.id, event.startTimeMs, event.isRunning) {
                    while (isActive && event.isRunning) {
                        delay(1000)
                        elapsedMs = (System.currentTimeMillis() - event.startTimeMs).coerceAtLeast(0L)
                    }
                }
                Text(
                    text = formatCountdownLong(elapsedMs),
                    style = PillPrimary,
                    color = contentColor,
                    maxLines = 1,
                    softWrap = false,
                )
            }
            if (chipState.secondaryEvent == null && chipState.eventCount > 1) {
                ChipEventCountBadge(chipState, accent, contentColor)
            }
        }
        is IslandEvent.AudioRecording -> {
            PulsingDot(color = contentColor, size = 6.dp)
            var elapsedMs by
                remember(event.id, event.startTimeMs, event.state, event.pausedDurationMs) {
                    mutableLongStateOf((System.currentTimeMillis() - event.startTimeMs - event.pausedDurationMs).coerceAtLeast(0L))
                }
            LaunchedEffect(event.id, event.startTimeMs, event.state, event.pausedDurationMs) {
                if (event.state == RecordingState.RECORDING) {
                    while (isActive && event.state == RecordingState.RECORDING) {
                        delay(500)
                        elapsedMs =
                            (System.currentTimeMillis() - event.startTimeMs - event.pausedDurationMs)
                                .coerceAtLeast(0L)
                    }
                }
            }
            Spacer(Modifier.width(SpaceXs))
            Text(
                text = formatCountdownLong(elapsedMs),
                style = PillPrimary,
                color = contentColor,
                maxLines = 1,
                softWrap = false,
            )
            if (chipState.secondaryEvent == null && chipState.eventCount > 1) {
                ChipEventCountBadge(chipState, accent, contentColor)
            }
        }
        is IslandEvent.Sports -> {
            Text(
                text =
                    if (event.score1.isNotEmpty()) "${event.score1} - ${event.score2}"
                    else stringResource(R.string.ax_dynamic_bar_sports_vs),
                style = PillPrimary,
                color = contentColor,
                maxLines = 1,
                softWrap = false,
            )
            Spacer(Modifier.width(SpaceXs))
            StatusBarSportsTeamBadge(event.team2Name, event.team2Icon, contentColor)
        }
        is IslandEvent.Charging -> {
            Text(
                text = "${event.level}%",
                style = PillPrimary,
                color = contentColor,
                maxLines = 1,
                softWrap = false,
            )
        }
        is IslandEvent.Torch -> {
            val label =
                if (event.supportsLevel) "${(event.level.toFloat() / event.maxLevel * 100).toInt()}%"
                else stringResource(R.string.ax_dynamic_bar_on)
            Text(
                text = label,
                style = PillPrimary,
                color = contentColor,
                maxLines = 1,
                softWrap = false,
            )
        }
        is IslandEvent.BiometricUnlock -> {
            Text(
                text = stringResource(R.string.ax_dynamic_bar_unlocked),
                style = PillPrimary,
                color = contentColor,
                maxLines = 1,
                softWrap = false,
            )
        }
        is IslandEvent.Notification -> {
            if (chipState.eventCount > 1) {
                ChipEventCountBadge(chipState, accent, contentColor)
            } else {
                Text(
                    text = event.appName ?: "",
                    style = PillPrimary,
                    color = contentColor,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = chipTextMaxWidth),
                )
            }
        }
        else -> {
            if (chipState.secondaryEvent == null && chipState.eventCount > 1) {
                ChipEventCountBadge(chipState, accent, contentColor)
            } else {
                PillEventText(
                    event,
                    Modifier.widthIn(max = chipTextMaxWidth),
                    overrideColor = contentColor,
                )
            }
        }
    }
}

@Composable
private fun SatelliteCapsule(
    secondaryEvent: IslandEvent,
    chipHeightDp: Dp,
    privacyGlowModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
) {
    val secAccent = chipAccentColorFor(secondaryEvent)
    val animatedSecAccent by animateColorAsState(
        secAccent,
        MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "sec_accent",
    )
    val secContentColor by animateColorAsState(
        OnCardText,
        MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "sec_content",
    )

    Box(
        modifier = modifier
            .height(chipHeightDp)
            .widthIn(min = chipHeightDp)
            .then(privacyGlowModifier)
            .clip(ChipShape)
            .background(CardBg)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        SecondaryCapsuleContent(secondaryEvent, secContentColor)
    }
}

@Composable
private fun SecondaryCapsuleContent(
    event: IslandEvent,
    contentColor: Color,
) {
    when (event) {
        is IslandEvent.Timer -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PillEventIcon(event, tint = contentColor, animated = true)
                if (event.endTimeMs > 0L) {
                    if (event.isPaused) {
                        Text(
                            text = stringResource(R.string.ax_dynamic_bar_paused),
                            style = PillPrimary,
                            color = contentColor,
                            maxLines = 1,
                            softWrap = false,
                        )
                    } else {
                        var remainingMs by
                            remember(event.id, event.endTimeMs, event.isPaused) {
                                mutableLongStateOf((event.endTimeMs - System.currentTimeMillis()).coerceAtLeast(0L))
                            }
                        LaunchedEffect(event.id, event.endTimeMs, event.isPaused) {
                            while (isActive && remainingMs > 0L) {
                                delay(500)
                                remainingMs = (event.endTimeMs - System.currentTimeMillis()).coerceAtLeast(0L)
                            }
                        }
                        val text = formatCountdownLong(remainingMs)
                        if (text.isNotEmpty()) {
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = text,
                                style = PillPrimary,
                                color = contentColor,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }
                }
            }
        }
        is IslandEvent.Stopwatch -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PillEventIcon(event, tint = contentColor, animated = true)
            }
        }
        is IslandEvent.Media -> {
            CompactAudioWaveformVisualizer(
                isPlaying = event.isPlaying,
                color = compactMediaWaveColor(event),
                modifier = Modifier.size(width = 10.dp, height = 8.dp),
            )
        }
        is IslandEvent.AudioRecording -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PulsingDot(color = contentColor, size = 6.dp)
                Spacer(Modifier.width(2.dp))
                Icon(
                    Icons.Filled.Mic,
                    null,
                    tint = contentColor,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
        is IslandEvent.Torch -> {
            Icon(
                Icons.Filled.FlashlightOn,
                null,
                tint = contentColor,
                modifier = Modifier.size(13.dp),
            )
        }
        else -> {
            PillEventIcon(event, tint = contentColor, animated = true)
        }
    }
}

@Composable
private fun ChipEventCountBadge(
    chipState: AxDynamicBarChipState,
    accent: Color,
    contentColor: Color,
) {
    if (chipState.eventCount <= 1) return
    Spacer(Modifier.width(SpaceXs))
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(SizeBadge)
            .widthIn(min = SizeBadge)
            .background(lerp(accent, contentColor, 0.3f), RoundedCornerShape(SizeBadge / 2))
            .padding(horizontal = 3.dp),
    ) {
        Text(
            text = "${chipState.eventCount}",
            style = TsBadge,
            color = contentColor,
            maxLines = 1,
            softWrap = false,
        )
    }
}

private fun chipEventSceneFor(event: IslandEvent, isAlert: Boolean): SceneKey =
    when {
        isAlert && event is IslandEvent.Notification -> ChipEventScenes.Alert
        event is IslandEvent.Media -> ChipEventScenes.Media
        event is IslandEvent.Sports && event.team2Name.isNotEmpty() -> ChipEventScenes.Sports
        event is IslandEvent.AospChip -> ChipEventScenes.AospChip
        event is IslandEvent.Timer -> ChipEventScenes.Timer
        event is IslandEvent.Stopwatch -> ChipEventScenes.Stopwatch
        event is IslandEvent.AudioRecording -> ChipEventScenes.AudioRecording
        event is IslandEvent.Notification -> ChipEventScenes.Notification
        event is IslandEvent.AppSwitch -> ChipEventScenes.AppSwitch
        else -> ChipEventScenes.Default
    }

@Composable
private fun StatusBarSportsTeamBadge(name: String, icon: Drawable?, contentColor: Color) {
    val badgeSize = 16.dp
    if (icon != null) {
        Image(
            bitmap = icon.toScaledBitmap(badgeSize),
            contentDescription = name,
            modifier = Modifier.size(badgeSize).clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = Modifier.size(badgeSize).clip(CircleShape)
                .background(contentColor.copy(alpha = AlphaIconBg)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                name.take(2).uppercase(),
                style = TsBadge,
                color = contentColor,
            )
        }
    }
}
