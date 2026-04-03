package com.biprangshu.spendwise.ui.components.datepicker.model

import java.time.YearMonth

data class Month(
    val yearMonth: YearMonth,
    val weeks: List<Week>
)
