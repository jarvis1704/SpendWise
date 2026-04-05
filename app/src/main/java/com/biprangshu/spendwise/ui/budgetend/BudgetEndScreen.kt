package com.biprangshu.spendwise.ui.budgetend

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biprangshu.spendwise.ui.budgetend.components.CategoriesChartCard
import com.biprangshu.spendwise.ui.budgetend.components.FillCircleStub
import com.biprangshu.spendwise.ui.budgetend.components.FinishedPeriodHeader
import com.biprangshu.spendwise.ui.budgetend.components.MinMaxSpendRow
import com.biprangshu.spendwise.ui.budgetend.components.RestAndSpentBudgetCard
import com.biprangshu.spendwise.ui.budgetend.components.SpendsCalendar
import com.biprangshu.spendwise.ui.budgetend.components.SpendsCountCard
import com.biprangshu.spendwise.ui.budgetend.components.WholeBudgetCard
import com.biprangshu.spendwise.ui.components.confetti.ConfettiController
import com.biprangshu.spendwise.ui.components.confetti.ConfettiOverlay
import com.biprangshu.spendwise.ui.components.confetti.rememberConfettiController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEndScreen(
    onSetupNewBudget: () -> Unit,
    onDismiss: () -> Unit,
    viewModel: BudgetEndViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val confettiController = rememberConfettiController()
    val haptic = LocalHapticFeedback.current
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                LoadingState()
            } else {
                BudgetEndContent(
                    uiState = uiState,
                    confettiController = confettiController,
                    onSetupNewBudget = {
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        onSetupNewBudget()
                    }
                )
            }
            
            // Confetti overlay renders on top
            ConfettiOverlay(
                controller = confettiController,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularWavyProgressIndicator()
    }
}

@Composable
private fun BudgetEndContent(
    uiState: BudgetEndUiState,
    confettiController: ConfettiController,
    onSetupNewBudget: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Surface(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Animated header with rotating stars
                    FinishedPeriodHeader(
                        scrollState = scrollState,
                        hasSpends = uiState.hasSpends
                    )
                    
                    // Budget overview card
                    if (uiState.startPeriodDate != null && uiState.finishPeriodDate != null) {
                        WholeBudgetCard(
                            totalBudget = uiState.totalBudget,
                            currencySymbol = uiState.currencySymbol,
                            startDate = uiState.startPeriodDate,
                            finishDate = uiState.finishPeriodDate,
                            actualFinishDate = uiState.actualFinishDate
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (uiState.hasSpends) {
                        // Row with RestAndSpent + Confetti button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                        ) {
                            RestAndSpentBudgetCard(
                                totalBudget = uiState.totalBudget,
                                totalSpent = uiState.totalSpent,
                                currencySymbol = uiState.currencySymbol,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // Confetti celebration button
                            FillCircleStub(
                                confettiController = confettiController,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Min/Max spend cards
                        MinMaxSpendRow(
                            minTransaction = uiState.minSpend,
                            maxTransaction = uiState.maxSpend,
                            currencySymbol = uiState.currencySymbol
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Transaction count card
                        SpendsCountCard(count = uiState.spendCount)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Calendar heatmap
                        if (uiState.startPeriodDate != null && uiState.finishPeriodDate != null) {
                            SpendsCalendar(
                                dailySpending = uiState.dailySpending,
                                startDate = uiState.startPeriodDate,
                                finishDate = uiState.actualFinishDate ?: uiState.finishPeriodDate,
                                totalBudget = uiState.totalBudget
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Category donut chart
                        CategoriesChartCard(
                            categoryBreakdown = uiState.categoryBreakdown,
                            currencySymbol = uiState.currencySymbol
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                // Extra space for the fixed bottom button
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        
        // Fixed bottom "Set up new budget" button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = onSetupNewBudget,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                shape = CircleShape
            ) {
                Text(
                    text = "Set up new budget",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null
                )
            }
        }
    }
}
