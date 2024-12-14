package com.inoo.chatty.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.material3.Icon
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.delay

enum class ToastType {
    SUCCESS,
    ERROR,
    INFO,
    WARNING
}

@Suppress("DEPRECATION")
@Composable
fun CustomToast(
    message: String,
    type: ToastType = ToastType.INFO,
    duration: Long = 2000L,
    onDismiss: () -> Unit,
    placement: Alignment = Alignment.TopCenter,
    showProgress: Boolean = true,
    action: (@Composable () -> Unit)? = null
) {
    var isVisible by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(1f) }

    val gradientColors = when (type) {
        ToastType.SUCCESS -> listOf(Color(0xFF4CAF50), Color(0xFF45A049))
        ToastType.ERROR -> listOf(Color(0xFFE53935), Color(0xFFD32F2F))
        ToastType.INFO -> listOf(Color(0xFF2196F3), Color(0xFF1976D2))
        ToastType.WARNING -> listOf(Color(0xFFFFA726), Color(0xFFFB8C00))
    }

    val icon = when (type) {
        ToastType.SUCCESS -> Icons.Filled.Check
        ToastType.ERROR -> Icons.Filled.Warning
        ToastType.INFO -> Icons.Filled.Info
        ToastType.WARNING -> Icons.Filled.Warning
    }

    LaunchedEffect(key1 = true) {
        if (showProgress) {
            animate(
                initialValue = 1f,
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = duration.toInt(),
                    easing = LinearEasing
                )
            ) { value, _ ->
                progress = value
            }
        }
        delay(duration)
        isVisible = false
        onDismiss()
    }

    if (isVisible) {
        Popup(
            alignment = placement
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = expandVertically(
                    expandFrom = if (placement == Alignment.TopCenter) Alignment.Top else Alignment.Bottom
                ) + fadeIn(
                    initialAlpha = 0.3f
                ),
                exit = shrinkVertically(
                    shrinkTowards = if (placement == Alignment.TopCenter) Alignment.Top else Alignment.Bottom
                ) + fadeOut()
            ) {
                Box {
                    Surface(
                        modifier = Modifier
                            .padding(16.dp)
                            .wrapContentSize()
                            .clickable {
                                isVisible = false
                                onDismiss()
                            },
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 8.dp,
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.horizontalGradient(gradientColors)
                                )
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    val rotation = remember { Animatable(0f) }
                                    LaunchedEffect(Unit) {
                                        rotation.animateTo(
                                            targetValue = 360f,
                                            animationSpec = tween(500, easing = FastOutSlowInEasing)
                                        )
                                    }

                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier
                                            .graphicsLayer(rotationZ = rotation.value)
                                            .size(24.dp)
                                    )

                                    Text(
                                        text = message,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    action?.let {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        it()
                                    }
                                }

                                if (showProgress) {
                                    LinearProgressIndicator(
                                        progress = progress,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(2.dp),
                                        color = Color.White.copy(alpha = 0.8f),
                                        trackColor = Color.White.copy(alpha = 0.2f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShowToast(
    message: String,
    type: ToastType = ToastType.INFO,
    duration: Long = 2000L,
    onDismiss: () -> Unit = {},
    placement: Alignment = Alignment.TopCenter,
    showProgress: Boolean = true,
    action: (@Composable () -> Unit)? = null
) {
    CustomToast(
        message = message,
        type = type,
        duration = duration,
        onDismiss = onDismiss,
        placement = placement,
        showProgress = showProgress,
        action = action
    )
}