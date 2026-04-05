package com.biprangshu.spendwise.ui.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biprangshu.spendwise.domain.model.ResidueDistributionMethod
import com.biprangshu.spendwise.ui.MainViewModel
import com.biprangshu.spendwise.ui.home.components.DailyBudgetHeroCard
import com.biprangshu.spendwise.ui.home.components.SettingsSheet
import com.biprangshu.spendwise.ui.home.components.StreaksCard
import com.biprangshu.spendwise.ui.home.components.TotalExpenseCard
import com.biprangshu.spendwise.ui.home.components.TotalIncomeCard
import com.biprangshu.spendwise.ui.theme.robotoFlexTopBarStyle
import com.biprangshu.spendwise.util.NotificationHelper
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    onShowAddTransaction: () -> Unit,
    onNavigateToDatePicker: () -> Unit,
    onChangeBudget: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isBiometricEnabled by mainViewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val isNotificationsEnabled by mainViewModel.isNotificationsEnabled.collectAsStateWithLifecycle()
    val notified50 by viewModel.notifiedThreshold50.collectAsStateWithLifecycle()
    val notified90 by viewModel.notifiedThreshold90.collectAsStateWithLifecycle()
    val notified100 by viewModel.notifiedThreshold100.collectAsStateWithLifecycle()
    var showSettingsSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current

    //notification permission for andorid 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* outcome handled by hasNotificationPermission() at post time */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationHelper.hasNotificationPermission(context)
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }


    LaunchedEffect(uiState.periodExpense, uiState.totalBudget, isNotificationsEnabled) {
        if (uiState.totalBudget <= 0 || !isNotificationsEnabled) return@LaunchedEffect
        val ratio = uiState.periodExpense / uiState.totalBudget
        when {
            ratio >= 1.0 && !notified100 -> {
                NotificationHelper.postBudgetThresholdNotification(
                    context, 100, uiState.currencySymbol,
                    uiState.periodExpense, uiState.totalBudget
                )
                viewModel.markThresholdNotified(100)
            }
            ratio >= 0.9 && !notified90 -> {
                NotificationHelper.postBudgetThresholdNotification(
                    context, 90, uiState.currencySymbol,
                    uiState.periodExpense, uiState.totalBudget
                )
                viewModel.markThresholdNotified(90)
            }
            ratio >= 0.5 && !notified50 -> {
                NotificationHelper.postBudgetThresholdNotification(
                    context, 50, uiState.currencySymbol,
                    uiState.periodExpense, uiState.totalBudget
                )
                viewModel.markThresholdNotified(50)
            }
        }
    }

    HomeScreenContent(
        uiState = uiState,
        innerPadding = innerPadding,
        onSettingsClick = { showSettingsSheet = true }
    )

    if (showSettingsSheet) {
        SettingsSheet(
            onDismiss = { showSettingsSheet = false },
            onChangeBudget = {
                showSettingsSheet = false
                onChangeBudget()
            },
            isBiometricEnabled = isBiometricEnabled,
            onBiometricToggle = { mainViewModel.toggleBiometric(it) },
            isNotificationsEnabled = isNotificationsEnabled,
            onNotificationsToggle = { mainViewModel.toggleNotifications(it) }
        )
    }

    if (uiState.showResidueDialog) {
        val currencySymbol = uiState.currencySymbol
        val residue = uiState.yesterdayResidue
        AlertDialog(
            onDismissRequest = { viewModel.dismissResidueDialog(ResidueDistributionMethod.DISTRIBUTE) },
            title = { Text("Distribute yesterday's residue?") },
            text = {
                Text(
                    "You have ${currencySymbol}${String.format("%.2f", residue)} unspent from yesterday. " +
                        "Distribute across remaining days or add to today only?"
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissResidueDialog(ResidueDistributionMethod.ADD_TO_CURRENT) }) {
                    Text("Add to today")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissResidueDialog(ResidueDistributionMethod.DISTRIBUTE) }) {
                    Text("Distribute")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    innerPadding: PaddingValues,
    onSettingsClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = innerPadding.calculateTopPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + 24.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Title
                Text(
                    text = "SpendWise",
                    style = robotoFlexTopBarStyle,
                    color = MaterialTheme.colorScheme.primary
                )

                val haptic = LocalHapticFeedback.current
                IconButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                    onSettingsClick()
                }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Daily Budget Hero Card
            DailyBudgetHeroCard(
                remainingToday = uiState.remainingToday,
                dailyBudget = uiState.dailyBudget,
                todayExpense = uiState.todayExpense,
                dailyProgress = uiState.dailyProgress,
                isOverspent = uiState.isOverspent,
                currencySymbol = uiState.currencySymbol,
                totalBudget = uiState.totalBudget
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Income and Expense Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TotalIncomeCard(
                    totalIncome = uiState.totalIncome,
                    currencySymbol = uiState.currencySymbol,
                    modifier = Modifier.weight(1f)
                )
                TotalExpenseCard(
                    totalExpense = uiState.totalExpense,
                    currencySymbol = uiState.currencySymbol,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Streaks Card
            StreaksCard(
                streakData = uiState.streakData,
                hasBudgetSet = uiState.totalBudget > 0
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}





private fun formatAmount(amount: Double): String {
    return NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }.format(amount)
}

