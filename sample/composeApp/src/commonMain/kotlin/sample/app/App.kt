/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package sample.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateField
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerDialog
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerDocked
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerFullScreenDialog
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerWithEnglishDate
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangeField
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangePickerWithEnglishDate
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangeTextField
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateTextField
import dev.shivathapaa.nepalidatepickerkmp.NepaliWheelDatePicker
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import dev.shivathapaa.nepalidatepickerkmp.holiday.NepaliWeekend
import dev.shivathapaa.nepalidatepickerkmp.holiday.NoOpHolidayProvider
import dev.shivathapaa.nepalidatepickerkmp.holiday.addWorkingDays
import dev.shivathapaa.nepalidatepickerkmp.holiday.nextWorkingDay
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDatePickerState
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDateRangePickerState

private val NepaliLocale = NepaliDateLocale(language = NepaliDatePickerLang.NEPALI)

@Composable
fun App() {
    MaterialTheme {
        Scaffold { padding ->
            SampleGallery(modifier = Modifier.padding(padding))
        }
    }
}

/**
 * A single scrollable gallery that exercises every public composable and the core utilities, so the
 * sample doubles as living documentation for the library.
 */
@OptIn(ExperimentalNepaliDatePickerApi::class)
@Composable
fun SampleGallery(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            "Nepali Date Picker",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Every picker, field, and core utility in one gallery.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        // ── Calendar-grid pickers ────────────────────────────────────────────
        Section(
            "Date picker",
            "Single BS date. Toggle the pencil icon for text-input mode."
        ) {
            NepaliDatePicker(state = rememberNepaliDatePickerState())
        }

        Section(
            "Date picker in Nepali",
            "Same picker, Nepali language + Devanagari digits."
        ) {
            NepaliDatePicker(state = rememberNepaliDatePickerState(locale = NepaliLocale))
        }

        Section(
            "Date picker with English date",
            "Each cell shows the Nepali day with its Gregorian day."
        ) {
            NepaliDatePickerWithEnglishDate(state = rememberNepaliDatePickerState())
        }

        Section(
            "Date range picker",
            "Start and end selection. Months laid out vertically."
        ) {
            NepaliDateRangePicker(
                state = rememberNepaliDateRangePickerState(),
                showMonthsVertically = true
            )
        }

        Section(
            "Date range picker with English date",
            "Dual BS + AD range selection."
        ) {
            NepaliDateRangePickerWithEnglishDate(
                state = rememberNepaliDateRangePickerState(),
                showMonthsVertically = false
            )
        }

        // ── Alternative experiences ──────────────────────────────────────────
        Section(
            "Wheel picker",
            "Three snapping columns. Best for birth dates and dates far from today."
        ) {
            var selected by remember { mutableStateOf<SimpleDate?>(null) }
            NepaliWheelDatePicker(onDateChange = { selected = it.toSimpleDate() })
            SelectedDateText(selected)
        }

        Section(
            "Docked picker",
            "Compact field with a dropdown calendar. The Material3 forms pattern."
        ) {
            NepaliDatePickerDocked(
                state = rememberNepaliDatePickerState(),
                label = { Text("Pick a date") }
            )
        }

        // ── Dialogs ──────────────────────────────────────────────────────────
        Section("Dialogs", "Modal and full-screen hosts.") {
            DialogDemos()
        }

        // ── Text fields ──────────────────────────────────────────────────────
        Section(
            "Text fields",
            "Type a date directly. Accepts Latin and Devanagari digits."
        ) {
            TextFieldDemos()
        }

        // ── Core utilities (no UI needed) ────────────────────────────────────
        Section(
            "Core utilities",
            "NepaliDateConverter works without any Compose dependency."
        ) {
            UtilitiesPanel()
        }

        Spacer(Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalNepaliDatePickerApi::class)
@Composable
private fun DialogDemos() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        var showModal by rememberSaveable { mutableStateOf(false) }
        var showFullScreen by rememberSaveable { mutableStateOf(false) }
        val singleState = rememberNepaliDatePickerState()
        val rangeState = rememberNepaliDateRangePickerState()

        Button(onClick = { showModal = true }) { Text("Modal date dialog") }
        Button(onClick = { showFullScreen = true }) { Text("Full-screen range dialog") }

        if (showModal) {
            NepaliDatePickerDialog(
                onDismissRequest = { showModal = false },
                confirmButton = {
                    NepaliDatePickerDefaults.DialogButton("OK", { showModal = false })
                },
                dismissButton = {
                    NepaliDatePickerDefaults.DialogButton("Cancel", { showModal = false })
                }
            ) {
                NepaliDatePicker(state = singleState)
            }
        }

        if (showFullScreen) {
            NepaliDatePickerFullScreenDialog(
                onDismissRequest = { showFullScreen = false },
                confirmButton = {
                    NepaliDatePickerDefaults.DialogButton("OK", { showFullScreen = false })
                },
                dismissButton = {
                    NepaliDatePickerDefaults.DialogButton("Cancel", { showFullScreen = false })
                },
                title = { Text("Select a range") }
            ) {
                NepaliDateRangePicker(state = rangeState, showMonthsVertically = true)
            }
        }
    }
}

@OptIn(ExperimentalNepaliDatePickerApi::class)
@Composable
private fun TextFieldDemos() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        var single by rememberSaveable(stateSaver = SimpleDateSaver) { mutableStateOf<SimpleDate?>(null) }
        NepaliDateTextField(
            value = single,
            onValueChange = { single = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Date (text only)") }
        )

        var withPicker by rememberSaveable(stateSaver = SimpleDateSaver) { mutableStateOf<SimpleDate?>(null) }
        NepaliDateField(
            value = withPicker,
            onValueChange = { withPicker = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Date (text + calendar icon)") }
        )

        var start by rememberSaveable(stateSaver = SimpleDateSaver) { mutableStateOf<SimpleDate?>(null) }
        var end by rememberSaveable(stateSaver = SimpleDateSaver) { mutableStateOf<SimpleDate?>(null) }
        NepaliDateRangeField(
            startValue = start,
            endValue = end,
            onRangeChange = { s, e -> start = s; end = e },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun UtilitiesPanel() {
    val lines = remember {
        val todayBs = NepaliDateConverter.todayNepaliCalendar
        val todayAd = NepaliDateConverter.todayEnglishSimpleDate
        val fullLocale = NepaliDateLocale(dateFormat = NepaliDateFormatStyle.FULL)
        val nepaliFull = NepaliDateConverter.formatNepaliDate(
            todayBs, fullLocale.copy(language = NepaliDatePickerLang.NEPALI)
        )
        val converted = NepaliDateConverter.convertEnglishToNepali(2024, 3, 21)
        val daysBetween = NepaliDateConverter.getNepaliDaysInBetween(
            SimpleDate(2081, 1, 1), SimpleDate(2081, 12, 30)
        )
        val nextWork = NepaliDateConverter.nextWorkingDay(
            todayBs.toSimpleDate(), NoOpHolidayProvider, NepaliWeekend.Default
        )
        val plusTen = NepaliDateConverter.addWorkingDays(
            todayBs.toSimpleDate(), 10, NoOpHolidayProvider, NepaliWeekend.Default
        )
        val time = NepaliDateConverter.currentTime
        val iso = NepaliDateConverter.formatNepaliDateTimeToIsoFormat(todayBs.toSimpleDate(), time)

        listOf(
            "Today (BS)" to "${todayBs.year}/${todayBs.month}/${todayBs.dayOfMonth}",
            "Today (AD)" to "${todayAd.year}/${todayAd.month}/${todayAd.dayOfMonth}",
            "Formatted (Nepali, full)" to nepaliFull,
            "2024-03-21 AD in BS" to "${converted.year}/${converted.month}/${converted.dayOfMonth}",
            "Days in BS 2081" to daysBetween.toString(),
            "Next working day" to "${nextWork.year}/${nextWork.month}/${nextWork.dayOfMonth}",
            "+10 working days" to "${plusTen.year}/${plusTen.month}/${plusTen.dayOfMonth}",
            "Time (Nepali)" to NepaliDateConverter.getFormattedTimeInNepali(time),
            "ISO 8601" to iso
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lines.forEach { (label, value) ->
            Row(label = label, value = value)
        }
    }
}

@Composable
private fun Row(label: String, value: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SelectedDateText(date: SimpleDate?) {
    date?.let {
        Text(
            "Selected: ${it.year}/${it.month}/${it.dayOfMonth}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun Section(title: String, subtitle: String? = null, content: @Composable () -> Unit) {
    Card(modifier = Modifier.widthIn(max = 420.dp).padding(horizontal = 12.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }
            HorizontalDivider()
            content()
        }
    }
}

/** Saves a nullable [SimpleDate] as "y,m,d" (or empty) across configuration changes. */
private val SimpleDateSaver = androidx.compose.runtime.saveable.Saver<SimpleDate?, String>(
    save = { it?.let { d -> "${d.year},${d.month},${d.dayOfMonth}" } ?: "" },
    restore = { s ->
        s.split(",").takeIf { it.size == 3 }?.let {
            runCatching { SimpleDate(it[0].toInt(), it[1].toInt(), it[2].toInt()) }.getOrNull()
        }
    }
)
