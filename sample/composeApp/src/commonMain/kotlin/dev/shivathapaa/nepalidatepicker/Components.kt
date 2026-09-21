/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate

/**
 * Root of one showcase tab: a centered, vertically scrolling column so any tab can be as long as it
 * needs to be. Shared by every showcase so they all scroll and space consistently.
 */
@Composable
fun ShowcaseColumn(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

/** A titled card wrapping one demo, so every example reads the same way across tabs. */
@Composable
fun DemoSection(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth().padding(horizontal = 12.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                }
                HorizontalDivider()
                content()
            }
        )
    }
}

/** One "label: value" line, used by the utility tabs to print converter output. */
@Composable
fun LabeledValue(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        Text(value, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End)
    }
}

/** Small readout of a picked date under an inline picker. */
@Composable
fun SelectedText(text: String?) {
    if (text != null) {
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun VerticalGap(height: Int = 8) {
    Spacer(Modifier.height(height.dp))
}

/**
 * Height to give a range picker whose months are stacked vertically when it sits in a
 * [ShowcaseColumn].
 *
 * That layout is a lazy list, and a lazy list measured with no height ceiling fails rather than
 * guessing one, so a vertical range picker inside a scrolling parent has to be told how tall it is.
 * A picker hosted in a dialog does not need this, because the dialog already bounds it.
 */
val VerticalMonthsHeight = 480.dp

/** Saves a nullable [SimpleDate] as "y,m,d" (or empty) so field input survives configuration changes. */
val SimpleDateSaver: Saver<SimpleDate?, String> = Saver(
    save = { it?.let { d -> "${d.year},${d.month},${d.dayOfMonth}" } ?: "" },
    restore = { s ->
        s.split(",").takeIf { it.size == 3 }?.let {
            runCatching { SimpleDate(it[0].toInt(), it[1].toInt(), it[2].toInt()) }.getOrNull()
        }
    }
)

/** A remembered, config-change-safe nullable [SimpleDate] holder for the field demos. */
@Composable
fun rememberSimpleDateState(initial: SimpleDate? = null): MutableState<SimpleDate?> =
    rememberSaveable(stateSaver = SimpleDateSaver) { mutableStateOf(initial) }

/** Compact "year/month/day" readout for a picked calendar, or null when nothing is selected. */
fun CustomCalendar?.readout(): String? = this?.formatted()

fun SimpleDate?.readout(): String? = this?.formatted()

/** The same "year/month/day" text for a date that is always there, so it needs no null handling. */
fun CustomCalendar.formatted(): String = "$year/$month/$dayOfMonth"

fun SimpleDate.formatted(): String = "$year/$month/$dayOfMonth"

/** Day-of-week numbers, the library's 1-based-Sunday convention. */
const val Sunday = 1
const val Friday = 6
const val Saturday = 7

/** The Bikram Sambat date [days] days from [from], rolling over the month and year ends. */
fun offsetDate(from: SimpleDate, days: Int): SimpleDate =
    NepaliDateConverter
        .getNepaliCalendarAfterAdditionOrSubtraction(from.year, from.month, from.dayOfMonth, days)
        .toSimpleDate()

/**
 * The Bikram Sambat month [months] months from [from], or [from] unchanged when that would leave the
 * supported range.
 *
 * The conversion table bounds the calendar, and asking for a month outside it is an error rather
 * than a clamp, so a control that steps month by month has to stop itself at the ends.
 */
fun steppedMonth(
    model: NepaliCalendarModel,
    from: NepaliMonthCalendar,
    months: Int
): NepaliMonthCalendar =
    runCatching { model.plusNepaliMonths(from, months) }.getOrDefault(from)
