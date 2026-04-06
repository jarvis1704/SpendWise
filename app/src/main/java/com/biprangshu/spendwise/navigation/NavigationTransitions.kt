package com.biprangshu.spendwise.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import kotlin.reflect.KClass

private val BOTTOM_NAV_ORDER: List<KClass<out Screen>> = listOf(
    Screen.Home::class,
    Screen.History::class,
    Screen.Insights::class,
)

private val enterSlideSpec = tween<IntOffset>(durationMillis = 350, easing = FastOutSlowInEasing)
private val exitSlideSpec = tween<IntOffset>(durationMillis = 250, easing = FastOutLinearInEasing)
private val fadeEnterSpec = tween<Float>(durationMillis = 350, easing = FastOutSlowInEasing)
private val fadeExitSpec = tween<Float>(durationMillis = 250, easing = FastOutLinearInEasing)

fun NavDestination.bottomNavIndex(): Int =
    BOTTOM_NAV_ORDER.indexOfFirst { hasRoute(it) }

fun navEnterTransition(from: Int, to: Int): EnterTransition {
    if (from == -1 || to == -1) return fadeIn(fadeEnterSpec)
    return if (to > from) {
        slideInHorizontally(enterSlideSpec) { it } + fadeIn(fadeEnterSpec)
    } else {
        slideInHorizontally(enterSlideSpec) { -it } + fadeIn(fadeEnterSpec)
    }
}

fun navExitTransition(from: Int, to: Int): ExitTransition {
    if (from == -1 || to == -1) return fadeOut(fadeExitSpec)
    return if (to > from) {
        slideOutHorizontally(exitSlideSpec) { -it } + fadeOut(fadeExitSpec)
    } else {
        slideOutHorizontally(exitSlideSpec) { it } + fadeOut(fadeExitSpec)
    }
}
