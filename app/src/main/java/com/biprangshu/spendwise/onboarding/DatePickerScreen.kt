package com.biprangshu.spendwise.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.biprangshu.spendwise.ui.components.datepicker.DatePicker
import com.biprangshu.spendwise.ui.components.datepicker.model.CalendarSelectionMode
import com.biprangshu.spendwise.ui.components.datepicker.model.CalendarState
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date

@Composable
fun DatePickerScreen(
    onClose: () -> Unit,
    onDateApplied: (LocalDate) -> Unit
) {
    val context = LocalContext.current
    val today = remember { LocalDate.now() }
    val tomorrow = remember {
        Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant())
    }
    val calendarState = remember {
        CalendarState(
            context = context,
            selectionMode = CalendarSelectionMode.RANGE,
            disableBeforeDate = tomorrow
        )
    }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(null) }
    val formatter = remember { DateTimeFormatter.ofPattern("d MMMM") }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
                FilledTonalButton(
                    onClick = { onDateApplied(selectedEndDate!!) },
                    enabled = selectedEndDate != null
                ) {
                    Text("\u2713 Apply")
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                val headerText = if (selectedEndDate != null) {
                    "${today.format(formatter)} \u2014 ${selectedEndDate!!.format(formatter)}"
                } else {
                    "Select end date"
                }
                Text(
                    text = headerText,
                    style = MaterialTheme.typography.headlineLarge
                )
                if (selectedEndDate != null) {
                    val days = ChronoUnit.DAYS.between(today, selectedEndDate) + 1
                    Text(
                        text = "$days days",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            DatePicker(
                calendarState = calendarState,
                onDayClicked = { date ->
                    calendarState.setSelectedDay(date)
                    selectedEndDate = date
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
}
