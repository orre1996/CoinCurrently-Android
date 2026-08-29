package com.cryptium.coincurrently.ui.main.General.Components.Chart

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp

private const val HOVER_LONG_PRESS_DELAY_MS = 200L

@Composable
fun MultiTouchView(
    modifier: Modifier = Modifier,
    onTouchesChanged: (List<Dp>) -> Unit,
) {
    Box(
        modifier = modifier
            .semantics { invisibleToUser() }
            .pointerInput(Unit) {
                val handler = Handler(Looper.getMainLooper())
                val touchSlop = viewConfiguration.touchSlop

                awaitEachGesture {
                    val activeTouches = linkedMapOf<PointerId, Float>()
                    val initialPositions = mutableMapOf<PointerId, Offset>()
                    var hoverActivated = false
                    var scrollDetected = false
                    var longPressRunnable: Runnable? = null

                    fun sortedTouchXs(): List<Dp> {
                        return activeTouches.values
                            .sorted()
                            .take(2)
                            .map { x -> with(density) { x.toDp() } }
                    }

                    fun publishTouches() {
                        onTouchesChanged(if (hoverActivated) sortedTouchXs() else emptyList())
                    }

                    fun cancelLongPress() {
                        longPressRunnable?.let { handler.removeCallbacks(it) }
                        longPressRunnable = null
                    }

                    fun checkForScroll(change: PointerInputChange) {
                        if (scrollDetected || hoverActivated) return

                        val initial = initialPositions[change.id] ?: return
                        if ((change.position - initial).getDistance() > touchSlop) {
                            scrollDetected = true
                            cancelLongPress()
                        }
                    }

                    fun scheduleLongPress() {
                        if (scrollDetected) return

                        cancelLongPress()
                        longPressRunnable = Runnable {
                            if (activeTouches.isNotEmpty() && !scrollDetected) {
                                hoverActivated = true
                                publishTouches()
                            }
                        }
                        handler.postDelayed(longPressRunnable!!, HOVER_LONG_PRESS_DELAY_MS)
                    }

                    try {
                        do {
                            val event = awaitPointerEvent()
                            event.changes.forEach { change ->
                                if (change.pressed) {
                                    if (!initialPositions.containsKey(change.id)) {
                                        initialPositions[change.id] = change.position
                                    }
                                    activeTouches[change.id] = change.position.x
                                    checkForScroll(change)
                                } else {
                                    activeTouches.remove(change.id)
                                    initialPositions.remove(change.id)
                                }
                            }

                            when {
                                activeTouches.isEmpty() -> {
                                    cancelLongPress()
                                    hoverActivated = false
                                    onTouchesChanged(emptyList())
                                }

                                scrollDetected && !hoverActivated -> {
                                    cancelLongPress()
                                    onTouchesChanged(emptyList())
                                }

                                activeTouches.size >= 2 -> {
                                    cancelLongPress()
                                    hoverActivated = true
                                    event.changes.forEach { it.consume() }
                                    publishTouches()
                                }

                                !hoverActivated && longPressRunnable == null && !scrollDetected -> {
                                    scheduleLongPress()
                                }

                                hoverActivated -> {
                                    event.changes.forEach { it.consume() }
                                    publishTouches()
                                }
                            }
                        } while (activeTouches.isNotEmpty())
                    } finally {
                        cancelLongPress()
                        onTouchesChanged(emptyList())
                    }
                }
            },
    )
}
