package com.biprangshu.spendwise.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Home : Screen()

    @Serializable
    data object History : Screen()

    @Serializable
    data object Onboarding : Screen()

    @Serializable
    data object DatePicker : Screen()

    @Serializable
    data object Insights : Screen()
}
