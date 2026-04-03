package com.biprangshu.spendwise

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.biprangshu.spendwise.navigation.Screen
import com.biprangshu.spendwise.navigation.SpendWiseNavGraph
import com.biprangshu.spendwise.ui.AppStartState
import com.biprangshu.spendwise.ui.MainViewModel
import com.biprangshu.spendwise.ui.budgetend.BudgetEndScreen
import com.biprangshu.spendwise.ui.components.AddTransactionSheet
import com.biprangshu.spendwise.ui.theme.SpendWiseTheme
import com.biprangshu.spendwise.util.showBiometricPrompt
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpendWiseTheme {
                SpendWiseApp()
            }
        }
    }
}

@Composable
private fun BiometricLockScreen(onUnlockClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "SpendWise",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Authentication required",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onUnlockClick) {
                Text("Authenticate")
            }
        }
    }
}

data class NavItem(
    val route: Screen,
    val label: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
)

private val navItems = listOf(
    NavItem(
        route = Screen.Home,
        label = "Home",
        filledIcon = Icons.Filled.Dashboard,
        outlinedIcon = Icons.Outlined.Dashboard
    ),
    NavItem(
        route = Screen.History,
        label = "History",
        filledIcon = Icons.Filled.History,
        outlinedIcon = Icons.Outlined.History
    ),
    NavItem(
        route = Screen.Insights,
        label = "Insights",
        filledIcon = Icons.Filled.Insights,
        outlinedIcon = Icons.Outlined.Insights
    )
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SpendWiseApp(
    viewModel: MainViewModel = hiltViewModel()
) {
    val startState by viewModel.startState.collectAsStateWithLifecycle()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle()
    val activity = LocalContext.current as FragmentActivity

    if (startState == AppStartState.LOADING) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    // Trigger biometric prompt when app is ready and auth is needed
    LaunchedEffect(startState, isBiometricEnabled, isAuthenticated) {
        if (isBiometricEnabled && !isAuthenticated) {
            showBiometricPrompt(
                activity = activity,
                onSuccess = { viewModel.onAuthenticationSuccess() },
                onError = { /* stays on lock screen */ }
            )
        }
    }

    // Lock screen gate
    if (isBiometricEnabled && !isAuthenticated) {
        BiometricLockScreen(
            onUnlockClick = {
                showBiometricPrompt(
                    activity = activity,
                    onSuccess = { viewModel.onAuthenticationSuccess() },
                    onError = {}
                )
            }
        )
        return
    }

    val startDestination: Screen = when (startState) {
        AppStartState.ONBOARDING -> Screen.Onboarding
        else -> Screen.Home
    }

    // Show budget modal on home when BUDGET_SET state, OR when period ended and user dismissed the end screen
    val showBudgetModalOnHome = startState == AppStartState.BUDGET_SET
    
    // Show budget end screen when period has expired
    val showBudgetEndScreen = startState == AppStartState.PERIOD_ENDED

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val haptic = LocalHapticFeedback.current

    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

    var showAddTransaction by remember { mutableStateOf(false) }

    val currentRoute = navBackStackEntry?.destination?.route
    val isHomeScreen = currentRoute?.contains("Home") == true
    val isOnSpecialScreen = currentRoute?.contains("Onboarding") == true ||
            currentRoute?.contains("DatePicker") == true

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isOnSpecialScreen) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 42.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    HorizontalFloatingToolbar(
                        expanded = true,
                        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(
                            toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            toolbarContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        navItems.forEach { item ->
                            val isSelected = when (item.route) {
                                Screen.Home -> currentRoute?.contains("Home") == true
                                Screen.History -> currentRoute?.contains("History") == true
                                Screen.Insights -> currentRoute?.contains("Insights") == true
                                else -> false
                            }

                            ToggleButton(
                                checked = isSelected,
                                onCheckedChange = {
                                    if (!isSelected) {
                                        haptic.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                modifier = Modifier.height(56.dp),
                                colors = ToggleButtonDefaults.toggleButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    checkedContainerColor = MaterialTheme.colorScheme.primary,
                                    checkedContentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shapes = ToggleButtonDefaults.shapes(
                                    CircleShape,
                                    CircleShape,
                                    CircleShape
                                ),
                            ) {
                                Crossfade(targetState = isSelected, label = "icon") { selected ->
                                    Icon(
                                        imageVector = if (selected) item.filledIcon else item.outlinedIcon,
                                        contentDescription = item.label
                                    )
                                }
                                AnimatedVisibility(
                                    visible = isSelected,
                                    enter = expandHorizontally(),
                                    exit = shrinkHorizontally()
                                ) {
                                    Row {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = item.label,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (isHomeScreen && !isOnSpecialScreen) {
                MediumFloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                        showAddTransaction = true
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Transaction"
                    )
                }
            }
        }
    ) { innerPadding ->
        SpendWiseNavGraph(
            navController = navController,
            innerPadding = innerPadding,
            onShowAddTransaction = { showAddTransaction = true },
            startDestination = startDestination,
            showBudgetModalOnHome = showBudgetModalOnHome
        )
    }

    if (showAddTransaction) {
        AddTransactionSheet(
            onDismiss = { showAddTransaction = false },
            onSave = { transaction ->
                viewModel.addTransaction(transaction)
            },
            currencySymbol = currencySymbol
        )
    }
    
    // Budget End Screen - shown when a budget period has ended
    if (showBudgetEndScreen) {
        BudgetEndScreen(
            onSetupNewBudget = {
                // Dismiss the budget end screen, which will trigger BUDGET_SET state
                // and show the BudgetSetModalSheet via showBudgetModalOnHome
                viewModel.onBudgetEndScreenDismissed()
            },
            onDismiss = {
                // User swiped down to dismiss - also proceed to budget setup
                viewModel.onBudgetEndScreenDismissed()
            }
        )
    }
}
