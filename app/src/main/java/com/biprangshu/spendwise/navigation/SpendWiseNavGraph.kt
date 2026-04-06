package com.biprangshu.spendwise.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.biprangshu.spendwise.onboarding.BudgetSetModalSheet
import com.biprangshu.spendwise.onboarding.BudgetSetViewModel
import com.biprangshu.spendwise.onboarding.DatePickerScreen
import com.biprangshu.spendwise.onboarding.OnboardingScreen
import com.biprangshu.spendwise.ui.history.HistoryScreen
import com.biprangshu.spendwise.ui.home.HomeScreen
import com.biprangshu.spendwise.ui.insights.InsightsScreen
import java.time.LocalDate

@Composable
fun SpendWiseNavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    onShowAddTransaction: () -> Unit,
    startDestination: Screen = Screen.Home,
    showBudgetModalOnHome: Boolean = false
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            navEnterTransition(
                initialState.destination.bottomNavIndex(),
                targetState.destination.bottomNavIndex()
            )
        },
        exitTransition = {
            navExitTransition(
                initialState.destination.bottomNavIndex(),
                targetState.destination.bottomNavIndex()
            )
        },
        popEnterTransition = {
            navEnterTransition(
                initialState.destination.bottomNavIndex(),
                targetState.destination.bottomNavIndex()
            )
        },
        popExitTransition = {
            navExitTransition(
                initialState.destination.bottomNavIndex(),
                targetState.destination.bottomNavIndex()
            )
        }
    ) {
        composable<Screen.Onboarding> { backStackEntry ->
            val showBudgetModal by backStackEntry.savedStateHandle
                .getStateFlow("show_budget_modal", false)
                .collectAsStateWithLifecycle()
            val endDateEpoch by backStackEntry.savedStateHandle
                .getStateFlow("end_date_epoch", -1L)
                .collectAsStateWithLifecycle()
            val budgetViewModel: BudgetSetViewModel = hiltViewModel()

            LaunchedEffect(endDateEpoch) {
                if (endDateEpoch >= 0L) {
                    budgetViewModel.onDateSelected(LocalDate.ofEpochDay(endDateEpoch))
                    backStackEntry.savedStateHandle["end_date_epoch"] = -1L
                }
            }

            OnboardingScreen(
                onOnboardComplete = {
                    backStackEntry.savedStateHandle["show_budget_modal"] = true
                }
            )

            if (showBudgetModal) {
                BudgetSetModalSheet(
                    onConfirmed = {
                        navController.navigate(Screen.Home) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToDatePicker = { navController.navigate(Screen.DatePicker) },
                    viewModel = budgetViewModel
                )
            }
        }
        composable<Screen.DatePicker> {
            DatePickerScreen(
                onClose = { navController.popBackStack() },
                onDateApplied = { date ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("end_date_epoch", date.toEpochDay())
                    navController.popBackStack()
                }
            )
        }
        composable<Screen.Home> { backStackEntry ->
            val endDateEpoch by backStackEntry.savedStateHandle
                .getStateFlow("end_date_epoch", -1L)
                .collectAsStateWithLifecycle()
            val showBudgetFromSettings by backStackEntry.savedStateHandle
                .getStateFlow("show_budget_sheet", false)
                .collectAsStateWithLifecycle()
            val budgetViewModel: BudgetSetViewModel = hiltViewModel()

            LaunchedEffect(endDateEpoch) {
                if (endDateEpoch >= 0L) {
                    budgetViewModel.onDateSelected(LocalDate.ofEpochDay(endDateEpoch))
                    backStackEntry.savedStateHandle["end_date_epoch"] = -1L
                }
            }

            HomeScreen(
                innerPadding = innerPadding,
                onShowAddTransaction = onShowAddTransaction,
                onNavigateToDatePicker = { navController.navigate(Screen.DatePicker) },
                onChangeBudget = { backStackEntry.savedStateHandle["show_budget_sheet"] = true }
            )

            if (showBudgetFromSettings || showBudgetModalOnHome) {
                BudgetSetModalSheet(
                    onConfirmed = { backStackEntry.savedStateHandle["show_budget_sheet"] = false },
                    onDismissRequest = { backStackEntry.savedStateHandle["show_budget_sheet"] = false },
                    onNavigateToDatePicker = { navController.navigate(Screen.DatePicker) },
                    viewModel = budgetViewModel
                )
            }
        }
        composable<Screen.History> {
            HistoryScreen(
                innerPadding = innerPadding
            )
        }
        composable<Screen.Insights> {
            InsightsScreen(innerPadding = innerPadding)
        }
    }
}
