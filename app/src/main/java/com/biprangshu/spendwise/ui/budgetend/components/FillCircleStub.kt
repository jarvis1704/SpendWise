package com.biprangshu.spendwise.ui.budgetend.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biprangshu.spendwise.ui.components.confetti.ConfettiController
import kotlinx.coroutines.launch


@Composable
fun FillCircleStub(
    confettiController: ConfettiController,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    
    // Track position for confetti ejection
    var confettiEjectPosition by remember { mutableStateOf(Offset.Zero) }
    
    // Interaction source for press detection
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Scale animation for press effect
    val scale = remember { Animatable(1f) }
    
    // Animate scale on press
    DisposableEffect(isPressed) {
        if (isPressed) {
            coroutineScope.launch {
                scale.animateTo(
                    targetValue = 1.5f,
                    animationSpec = tween(durationMillis = 20, easing = LinearEasing)
                )
            }
        } else {
            coroutineScope.launch {
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 120, easing = LinearEasing)
                )
            }
        }
        onDispose { }
    }
    
    Card(
        modifier = modifier
            .size(100.dp)
            .aspectRatio(1f)
            .clip(CircleShape)
            .onGloballyPositioned { coordinates ->
                // Get center position in window for confetti ejection
                val position = coordinates.positionInWindow()
                confettiEjectPosition = Offset(
                    x = position.x + coordinates.size.width / 2,
                    y = position.y + coordinates.size.height / 2
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple()
            ) {
                // Spawn confetti
                confettiController.spawn(
                    ejectPoint = confettiEjectPosition,
                    ejectAngle = 270f, // Upward
                    angleSpread = 80f,
                    force = 600f,
                    particleCount = 30..60
                )
                
                // Haptic feedback
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Apply the squish/bounce effect
                .rotate(-45f)
                .scale(scaleX = 1f, scaleY = scale.value)
                .rotate(45f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "\uD83C\uDF89", // 🎉 Party emoji
                fontSize = 48.sp
            )
        }
    }
}
