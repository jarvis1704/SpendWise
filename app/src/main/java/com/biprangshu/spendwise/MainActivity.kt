package com.biprangshu.spendwise

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Insights
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
import androidx.activity.viewModels
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.biprangshu.spendwise.navigation.Screen
import com.biprangshu.spendwise.navigation.SpendWiseNavGraph
import com.biprangshu.spendwise.ui.MainViewModel
import com.biprangshu.spendwise.ui.biometric.BiometricLockScreen
import com.biprangshu.spendwise.ui.budgetend.BudgetEndScreen
import com.biprangshu.spendwise.ui.components.AddTransactionSheet
import com.biprangshu.spendwise.ui.theme.SpendWiseTheme
import android.content.Intent
import com.biprangshu.spendwise.util.showBiometricPrompt
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        splashScreen.setKeepOnScreenCondition {
            viewModel.startState.value == AppStartState.LOADING
        }
        enableEdgeToEdge()
        setContent {
            SpendWiseTheme {
                SpendWiseApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == ACTION_ADD_TRANSACTION ||
            intent?.getBooleanExtra(EXTRA_OPEN_ADD_TRANSACTION, false) == true) {
            viewModel.triggerAddTransaction(true)
        }
    }

    companion object {
        const val ACTION_ADD_TRANSACTION = "com.biprangshu.spendwise.action.ADD_TRANSACTION"
        const val EXTRA_OPEN_ADD_TRANSACTION = "EXTRA_OPEN_ADD_TRANSACTION"
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

    if (startState == AppStartState.LOADING) return

    //biometric trigger if enabled
    LaunchedEffect(startState, isBiometricEnabled, isAuthenticated) {
        if (isBiometricEnabled && !isAuthenticated) {
            showBiometricPrompt(
                activity = activity,
                onSuccess = { viewModel.onAuthenticationSuccess() },
                onError = { /* stays on lock screen */ }
            )
        }
    }

    //Biometric lock screen
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


    val showBudgetModalOnHome = startState == AppStartState.BUDGET_SET
    

    val showBudgetEndScreen = startState == AppStartState.PERIOD_ENDED

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val haptic = LocalHapticFeedback.current

    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val openAddTransactionTrigger by viewModel.openAddTransactionTrigger.collectAsStateWithLifecycle()

    var showAddTransaction by remember { mutableStateOf(false) }

    LaunchedEffect(openAddTransactionTrigger, startState, isAuthenticated, isBiometricEnabled) {
        if (openAddTransactionTrigger && startState != AppStartState.LOADING) {
            if (startState == AppStartState.HOME || startState == AppStartState.BUDGET_SET) {
                if (!isBiometricEnabled || isAuthenticated) {
                    showAddTransaction = true
                    viewModel.triggerAddTransaction(false)
                }
            } else {
                viewModel.triggerAddTransaction(false)
            }
        }
    }

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
    
    //budget end screen
    if (showBudgetEndScreen) {
        BudgetEndScreen(
            onSetupNewBudget = {
                viewModel.onBudgetEndScreenDismissed()
            },
            onDismiss = {
                viewModel.onBudgetEndScreenDismissed()
            }
        )
    }
}
