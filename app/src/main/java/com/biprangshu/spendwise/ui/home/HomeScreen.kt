package com.biprangshu.spendwise.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.biometric.BiometricManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biprangshu.spendwise.domain.model.ResidueDistributionMethod
import com.biprangshu.spendwise.ui.MainViewModel
import kotlinx.coroutines.launch
import com.biprangshu.spendwise.onboarding.BudgetSetModalSheet
import com.biprangshu.spendwise.ui.home.components.StreaksCard
import com.biprangshu.spendwise.ui.theme.colorExpense
import com.biprangshu.spendwise.ui.theme.colorIncome
import com.biprangshu.spendwise.ui.theme.progressGreen
import com.biprangshu.spendwise.ui.theme.progressRed
import com.biprangshu.spendwise.ui.theme.progressYellow
import com.biprangshu.spendwise.ui.theme.robotoFlexTopBarStyle
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    onShowAddTransaction: () -> Unit,
    onNavigateToDatePicker: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isBiometricEnabled by mainViewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showBudgetSheet by remember { mutableStateOf(false) }

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
                showBudgetSheet = true
            },
            isBiometricEnabled = isBiometricEnabled,
            onBiometricToggle = { mainViewModel.toggleBiometric(it) }
        )
    }

    if (showBudgetSheet) {
        BudgetSetModalSheet(
            onConfirmed = { showBudgetSheet = false },
            onDismissRequest = { showBudgetSheet = false },
            onNavigateToDatePicker = onNavigateToDatePicker,
            viewModel = hiltViewModel()
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

@Composable
private fun DailyBudgetHeroCard(
    remainingToday: Double,
    dailyBudget: Double,
    todayExpense: Double,
    totalBudget: Double,
    dailyProgress: Float,
    isOverspent: Boolean,
    currencySymbol: String
) {
    val progressColor = when {
        dailyProgress < 0.6f -> progressGreen
        dailyProgress < 0.85f -> progressYellow
        else -> progressRed
    }

    val animatedProgress by animateFloatAsState(
        targetValue = dailyProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isOverspent) "Overspent Today" else "Remaining Today",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                text = "$currencySymbol${formatAmount(remainingToday)}",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = if (isOverspent) {
                    colorExpense
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
            )

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Spent: $currencySymbol${formatAmount(todayExpense)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Budget: $currencySymbol${formatAmount(totalBudget)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun TotalIncomeCard(
    totalIncome: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "Income",
                    tint = colorIncome
                )
                Text(
                    text = "Income",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Text(
                text = "$currencySymbol${formatAmount(totalIncome)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorIncome
            )
        }
    }
}

@Composable
private fun TotalExpenseCard(
    totalExpense: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingDown,
                    contentDescription = "Expense",
                    tint = colorExpense
                )
                Text(
                    text = "Expense",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            Text(
                text = "$currencySymbol${formatAmount(totalExpense)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorExpense
            )
        }
    }
}

private fun formatAmount(amount: Double): String {
    return NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }.format(amount)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(
    onDismiss: () -> Unit,
    onChangeBudget: () -> Unit,
    isBiometricEnabled: Boolean,
    onBiometricToggle: (Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = {
            haptic.performHapticFeedback(HapticFeedbackType.GestureEnd)
            onDismiss()
        },
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                ListItem(
                    headlineContent = { Text("Change Your Budget") },
                    leadingContent = {
                        Icon(
                            Icons.Default.Wallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraLarge)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                            onChangeBudget()
                        }
                )

                Spacer(Modifier.height(8.dp))

                ListItem(
                    headlineContent = { Text("Biometric Authentication") },
                    supportingContent = { Text("Require fingerprint/face on launch") },
                    leadingContent = {
                        Icon(
                            Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    val manager = BiometricManager.from(context)
                                    val canAuth = manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                                    if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Biometric not available on this device",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                        return@Switch
                                    }
                                }
                                haptic.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                                onBiometricToggle(enabled)
                            }
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.clip(MaterialTheme.shapes.extraLarge)
                )

                Spacer(Modifier.height(16.dp))
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
