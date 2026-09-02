/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package sample.app

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
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate

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
fun CustomCalendar?.readout(): String? = this?.let { "${it.year}/${it.month}/${it.dayOfMonth}" }

fun SimpleDate?.readout(): String? = this?.let { "${it.year}/${it.month}/${it.dayOfMonth}" }
