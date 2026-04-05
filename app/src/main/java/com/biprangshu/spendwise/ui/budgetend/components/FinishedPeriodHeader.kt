package com.biprangshu.spendwise.ui.budgetend.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.biprangshu.spendwise.R
import com.biprangshu.spendwise.util.combineColors


@Composable
fun FinishedPeriodHeader(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    hasSpends: Boolean = false
) {
    val localDensity = LocalDensity.current
    var headerSize by remember { mutableStateOf(Size(0f, 0f)) }
    
    // Convert scroll value to dp for parallax
    val scrollDp = with(localDensity) { scrollState.value.toDp() }
    
    // Star color: blend secondaryContainer with surface
    val starColor = combineColors(
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.surface,
        0.5f
    )
    
    // Infinite rotation animations for stars
    val infiniteTransition1 = rememberInfiniteTransition(label = "star1Rotation")
    val angleStar1 by infiniteTransition1.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "angleStar1"
    )
    
    val infiniteTransition2 = rememberInfiniteTransition(label = "star2Rotation")
    val angleStar2 by infiniteTransition2.animateFloat(
        initialValue = -50f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "angleStar2"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 36.dp)
            .onGloballyPositioned { coordinates ->
                headerSize = Size(
                    coordinates.size.width.toFloat(),
                    coordinates.size.height.toFloat()
                )
            },
        contentAlignment = Alignment.Center
    ) {
        val halfWidthDp = with(localDensity) { (headerSize.width / 2).toDp() }
        val halfHeightDp = with(localDensity) { (headerSize.height / 2).toDp() }
        
        // Text content with parallax scroll
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .absoluteOffset(y = scrollDp * 0.25f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))
            
            Text(
                text = "Period Ended",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (!hasSpends) {
                    "Wow, you haven't spent anything!"
                } else {
                    "Here are the statistics for this period"
                },
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(64.dp))
        }
        
        // Star 1 - top right, parallax with scroll
        Icon(
            painter = painterResource(id = R.drawable.shape_soft_star_1),
            contentDescription = null,
            tint = starColor,
            modifier = Modifier
                .requiredSize(256.dp)
                .absoluteOffset(
                    x = halfWidthDp * 0.7f,
                    y = -halfHeightDp * 0.6f + scrollDp * 0.35f
                )
                .rotate(angleStar1)
                .zIndex(-1f)
        )
        
        // Star 2 - bottom left, parallax with scroll
        Icon(
            painter = painterResource(id = R.drawable.shape_soft_star_2),
            contentDescription = null,
            tint = starColor,
            modifier = Modifier
                .requiredSize(256.dp)
                .absoluteOffset(
                    x = -halfWidthDp * 0.7f,
                    y = halfHeightDp * 0.6f + scrollDp * 0.6f
                )
                .rotate(angleStar2)
                .zIndex(-1f)
        )
    }
}
