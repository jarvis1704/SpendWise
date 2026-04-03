package com.biprangshu.spendwise.ui.history

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biprangshu.spendwise.domain.model.Transaction
import com.biprangshu.spendwise.ui.components.ListItemPosition
import com.biprangshu.spendwise.ui.components.TransactionListItem
import com.biprangshu.spendwise.ui.theme.robotoFlexTopBarStyle
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
    innerPadding: PaddingValues,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxSize()) {
        HistoryScreenContent(
            uiState = uiState,
            innerPadding = innerPadding,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onDeleteTransaction = { transaction ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.deleteTransaction(transaction)
                coroutineScope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "Transaction deleted",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.restoreTransaction(transaction)
                    }
                }
            }
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = innerPadding.calculateBottomPadding() + 8.dp)
        )
    }
}

@Composable
private fun HistoryScreenContent(
    uiState: HistoryUiState,
    innerPadding: PaddingValues,
    onSearchQueryChange: (String) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp
                )
        ) {
            // Screen Title
            Text(
                text = "History",
                style = robotoFlexTopBarStyle,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search transactions...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Transaction List
            if (uiState.transactions.isEmpty() && !uiState.isLoading) {
                EmptyTransactionState()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        bottom = innerPadding.calculateBottomPadding() + 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    itemsIndexed(
                        items = uiState.transactions,
                        key = { _, transaction -> transaction.id }
                    ) { index, transaction ->
                        val position = when {
                            uiState.transactions.size == 1 -> ListItemPosition.SINGLE
                            index == 0 -> ListItemPosition.TOP
                            index == uiState.transactions.lastIndex -> ListItemPosition.BOTTOM
                            else -> ListItemPosition.MIDDLE
                        }

                        val dismissState = rememberSwipeToDismissBoxState(
                            positionalThreshold = { totalDistance -> totalDistance * 0.4f }
                        )

                        LaunchedEffect(dismissState.currentValue) {
                            if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                                onDeleteTransaction(transaction)
                            }
                        }

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = true,
                            enableDismissFromEndToStart = true,
                            backgroundContent = {
                                SwipeDeleteBackground(
                                    dismissDirection = dismissState.dismissDirection,
                                    position = position
                                )
                            },
                            modifier = Modifier.animateItem()
                        ) {
                            TransactionListItem(
                                transaction = transaction,
                                currencySymbol = "₹",
                                position = position,
                                onClick = { }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeDeleteBackground(
    dismissDirection: SwipeToDismissBoxValue?,
    position: ListItemPosition
) {
    val shape = when (position) {
        ListItemPosition.TOP -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
        ListItemPosition.MIDDLE -> RoundedCornerShape(4.dp)
        ListItemPosition.BOTTOM -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
        ListItemPosition.SINGLE -> MaterialTheme.shapes.extraLarge
    }

    val isActive = dismissDirection != null && dismissDirection != SwipeToDismissBoxValue.Settled

    val bgColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.errorContainer
                      else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "swipe_bg"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.75f,
        label = "icon_scale"
    )
    val iconAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        label = "icon_alpha"
    )
    val iconAlignment = if (dismissDirection == SwipeToDismissBoxValue.StartToEnd)
        Alignment.CenterStart else Alignment.CenterEnd

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(bgColor)
            .padding(horizontal = 24.dp),
        contentAlignment = iconAlignment
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete transaction",
            tint = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = iconAlpha),
            modifier = Modifier
                .size(24.dp)
                .scale(iconScale)
        )
    }
}

@Composable
private fun EmptyTransactionState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No transactions yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap + to add your first transaction",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
