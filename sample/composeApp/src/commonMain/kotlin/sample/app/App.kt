package sample.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.DisplayMode
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerDialog
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerState
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerWithEnglishDate
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangePickerState
import dev.shivathapaa.nepalidatepickerkmp.NepaliDateRangePickerWithEnglishDate
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDatePickerState
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDateRangePickerState

@Composable
fun App() {
    Scaffold { paddingValues ->
        SamplePickers(modifier = Modifier.padding(paddingValues))
    }
}

@OptIn(ExperimentalNepaliDatePickerApi::class)
@Composable
fun SamplePickers(
    modifier: Modifier = Modifier
) {
    var showMonthVerticallyInRangePicker by rememberSaveable { mutableStateOf(true) }

    var showTodayButton by rememberSaveable { mutableStateOf(true) }
    var showRangePickerWithYearPickerAndNavigation by rememberSaveable { mutableStateOf(true) }
    val layoutSimpleState = rememberNepaliDatePickerState(
        locale = NepaliDateLocale(dateFormat = NepaliDateFormatStyle.MEDIUM)
    )
    val layoutSimpleStateWithEnglish = rememberNepaliDatePickerState(
        locale = NepaliDateLocale(
            dateFormat = NepaliDateFormatStyle.FULL,
            weekDayName = NameFormat.MEDIUM
        )
    )
    val layoutRangeSimpleState = rememberNepaliDateRangePickerState(
        locale = NepaliDateLocale(
            dateFormat = NepaliDateFormatStyle.SHORT_MDY
        )
    )
    val layoutRangeSimpleStateWithEnglish = rememberNepaliDateRangePickerState()

    Column(
        modifier = modifier.fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        val englishValLocale = when (4) {
            1 -> NepaliDateLocale(
                dateFormat = NepaliDateFormatStyle.COMPACT_MDY,
                weekDayName = NameFormat.SHORT,
                monthName = NameFormat.FULL
            )

            2 -> NepaliDateLocale(
                dateFormat = NepaliDateFormatStyle.SHORT_YMD,
                weekDayName = NameFormat.MEDIUM,
            )

            3 -> NepaliDateLocale(
                dateFormat = NepaliDateFormatStyle.MEDIUM,
                weekDayName = NameFormat.FULL,
                monthName = NameFormat.FULL
            )

            4 -> NepaliDateLocale(
                dateFormat = NepaliDateFormatStyle.LONG,
                weekDayName = NameFormat.SHORT
            )

            5 -> NepaliDateLocale(
                dateFormat = NepaliDateFormatStyle.FULL,
                weekDayName = NameFormat.FULL,
                monthName = NameFormat.FULL
            )

            else -> NepaliDatePickerDefaults.DefaultLocale
        }

        val firstState = if (true) rememberNepaliDatePickerState(
            yearRange = IntRange(2079, 2084),
            locale = englishValLocale
        ) else rememberNepaliDatePickerState(locale = englishValLocale)

        NepaliDatePickerForScreenShots(
            modifier = Modifier,
            localeInt = 4,
            withYearRange = true,
            showTodayButton = true,
            firstState
        )
        val englishLocale = when (5) {
            1 -> NepaliDateLocale(
                language = NepaliDatePickerLang.NEPALI,
                dateFormat = NepaliDateFormatStyle.COMPACT_MDY,
                weekDayName = NameFormat.SHORT,
                monthName = NameFormat.FULL
            )

            2 -> NepaliDateLocale(
                language = NepaliDatePickerLang.NEPALI,
                dateFormat = NepaliDateFormatStyle.SHORT_YMD,
                weekDayName = NameFormat.MEDIUM,
            )

            3 -> NepaliDateLocale(
                language = NepaliDatePickerLang.NEPALI,
                dateFormat = NepaliDateFormatStyle.MEDIUM,
                weekDayName = NameFormat.FULL,
                monthName = NameFormat.SHORT
            )

            4 -> NepaliDateLocale(
                language = NepaliDatePickerLang.NEPALI,
                dateFormat = NepaliDateFormatStyle.LONG,
                weekDayName = NameFormat.SHORT
            )

            5 -> NepaliDateLocale(
                language = NepaliDatePickerLang.NEPALI,
                dateFormat = NepaliDateFormatStyle.FULL,
                weekDayName = NameFormat.SHORT,
                monthName = NameFormat.FULL
            )

            else -> NepaliDateLocale(
                language = NepaliDatePickerLang.NEPALI
            )
        }

        val state = if (true) rememberNepaliDatePickerState(
            nepaliSelectableDates = NepaliDateConverter.DateRangeSelectable(
                SimpleDate(2079, 3, 21),
                SimpleDate(2084, 5, 5)
            ),
            locale = englishLocale
        ) else rememberNepaliDatePickerState(locale = englishLocale)

        NepaliDatePickerForNepaliScreenShots(
            modifier = Modifier,
            localeInt = 5,
            withSelectables = true,
            showTodayButton = true,
            state = state
        )

        val todayDate = NepaliDateConverter.todayNepaliCalendar

        val nepaliDateLocale = NepaliDateLocale(
            language = NepaliDatePickerLang.NEPALI,
            dateFormat = NepaliDateFormatStyle.MEDIUM
        )

        val englishDateLocale = NepaliDateLocale(
            language = NepaliDatePickerLang.ENGLISH,
            dateFormat = NepaliDateFormatStyle.SHORT_MDY,
            monthName = NameFormat.SHORT,
            weekDayName = NameFormat.SHORT
        )

        val nepaliDatePickerState = rememberNepaliDatePickerState(
            nepaliSelectableDates = NepaliDateConverter.BeforeDateSelectable(todayDate.toSimpleDate()),
            initialDisplayedMonth = SimpleDate(2081, 7),
            locale = nepaliDateLocale
        )

        val englishDatePickerState = rememberNepaliDatePickerState(
            nepaliSelectableDates = NepaliDateConverter.DateRangeSelectable(
                SimpleDate(2079, 2, 11),
                SimpleDate(2086, 7, 22)
            ),
            locale = englishDateLocale
        )

        DialogsOfDatePickerWithEnglishDate(
            modifier = Modifier,
            nepaliDatePickerState, englishDatePickerState
        )

        val defaultState = rememberNepaliDateRangePickerState()
        val defaultStateWithNepali =
            rememberNepaliDateRangePickerState(
                locale = NepaliDatePickerDefaults.DefaultRangePickerLocale.copy(
                    language = NepaliDatePickerLang.NEPALI,
                    dateFormat = NepaliDateFormatStyle.SHORT_YMD
                )
            )

        val secondState =
            rememberNepaliDateRangePickerState()
        val secondStateWithNepali =
            rememberNepaliDateRangePickerState(
                locale = NepaliDatePickerDefaults.DefaultRangePickerLocale.copy(
                    language = NepaliDatePickerLang.NEPALI
                ),
                initialDisplayMode = DisplayMode.Input
            )
        FilterChipForToggle(
            isSelected = showMonthVerticallyInRangePicker,
            onClick = { showMonthVerticallyInRangePicker = !showMonthVerticallyInRangePicker },
            text = "Show months vertically in Range Picker"
        )

        FilterChipForToggle(
            isSelected = showTodayButton,
            onClick = { showTodayButton = !showTodayButton },
            text = "Show Today Button in Range Picker"
        )
        FilterChipForToggle(
            isSelected = showRangePickerWithYearPickerAndNavigation, onClick = {
                showRangePickerWithYearPickerAndNavigation =
                    !showRangePickerWithYearPickerAndNavigation
            }, text = "Show year picker & month navigation in Range Picker"
        )

        NepaliDateRangePickerDialogs(
            modifier = Modifier,
            monthsVertically = showMonthVerticallyInRangePicker,
            showTodayButton = showTodayButton,
            showRangePickerWithYearPickerAndNavigation = showRangePickerWithYearPickerAndNavigation,
            defaultState,
            defaultStateWithNepali,
            secondState,
            secondStateWithNepali
        )

        Spacer(modifier = Modifier.height(24.dp))

        NepaliDatePicker(state = layoutSimpleState)

        Spacer(modifier = Modifier.height(24.dp))

        NepaliDatePickerWithEnglishDate(layoutSimpleStateWithEnglish)

        Spacer(modifier = Modifier.height(24.dp))

        val colors = NepaliDatePickerDefaults.colors()
        Box(Modifier.background(colors.containerColor)) {
            NepaliDateRangePicker(
                colors = colors,
                state = layoutRangeSimpleState,
                showMonthsVertically = false,
                showTodayButton = showTodayButton,
                showYearPickerAndMonthNavigation = showRangePickerWithYearPickerAndNavigation
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(Modifier.background(colors.containerColor)) {
            NepaliDateRangePickerWithEnglishDate(
                colors = colors,
                state = layoutRangeSimpleStateWithEnglish,
                showMonthsVertically = false,
                showTodayButton = showTodayButton,
                showYearPickerAndMonthNavigation = showRangePickerWithYearPickerAndNavigation
            )
        }
    }
}

@Composable
private fun FilterChipForToggle(
    modifier: Modifier = Modifier, isSelected: Boolean, onClick: () -> Unit, text: String
) {
    FilterChip(
        modifier = modifier.padding(horizontal = 16.dp),
        selected = isSelected,
        onClick = onClick,
        label = { Text(text, textAlign = TextAlign.Center) },
        trailingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            {
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        }
    )
}

@OptIn(ExperimentalNepaliDatePickerApi::class)
@Composable
fun DialogsOfDatePickerWithEnglishDate(
    modifier: Modifier = Modifier,
    nepaliDatePickerState: NepaliDatePickerState,
    englishDatePickerState: NepaliDatePickerState
) {
    var showNepaliDialog by rememberSaveable { mutableStateOf(false) }
    var showEnglishDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { showEnglishDialog = true }) {
            Text("Select date with EngNe")
        }

        Button(onClick = { showNepaliDialog = true }) {
            Text("मिति छान्नुहोस् with EngNe")
        }

        if (showNepaliDialog) {
            NepaliDatePickerDialog(
                onDismissRequest = { showNepaliDialog = false },
                confirmButton = {
                    NepaliDatePickerDefaults.DialogButton(
                        text = "ठीक छ",
                        onButtonClick = { showNepaliDialog = false }
                    )
                },
                dismissButton = {
                    NepaliDatePickerDefaults.DialogButton(
                        text = "रद्द गर्नुहोस्",
                        onButtonClick = { showNepaliDialog = false }
                    )
                }
            ) {
                NepaliDatePickerWithEnglishDate(
                    englishDateLocale = NepaliDateLocale(
                        language = NepaliDatePickerLang.NEPALI,
                        dateFormat = NepaliDateFormatStyle.FULL,
                        weekDayName = NameFormat.FULL,
                        monthName = NameFormat.MEDIUM
                    ),
                    showTodayButton = true,
                    state = nepaliDatePickerState
                )
            }
        }

        if (showEnglishDialog) {
            NepaliDatePickerDialog(
                onDismissRequest = { showEnglishDialog = false },
                confirmButton = {
                    NepaliDatePickerDefaults.DialogButton(
                        text = "OK",
                        onButtonClick = { showEnglishDialog = false }
                    )
                },
                dismissButton = {
                    NepaliDatePickerDefaults.DialogButton(
                        text = "Cancel",
                        onButtonClick = { showEnglishDialog = false }
                    )
                }
            ) {
                NepaliDatePickerWithEnglishDate(
                    englishDateLocale = NepaliDateLocale(
                        language = NepaliDatePickerLang.ENGLISH,
                        dateFormat = NepaliDateFormatStyle.LONG
                    ),
                    showTodayButton = true,
                    state = englishDatePickerState
                )
            }
        }
    }
}

@OptIn(ExperimentalNepaliDatePickerApi::class)
@Composable
fun NepaliDateRangePickerDialogs(
    modifier: Modifier = Modifier,
    monthsVertically: Boolean,
    showTodayButton: Boolean,
    showRangePickerWithYearPickerAndNavigation: Boolean,
    defaultState: NepaliDateRangePickerState,
    defaultStateWithNepali: NepaliDateRangePickerState,
    secondState: NepaliDateRangePickerState,
    secondStateWithNepali: NepaliDateRangePickerState
) {
    var showDateRangePickerDialogWithEnglish by rememberSaveable { mutableStateOf(false) }
    var showDateRangePickerDialogWithEnglishInNepali by rememberSaveable { mutableStateOf(false) }

    var showSimpleDateRangePickerDialog by rememberSaveable { mutableStateOf(false) }
    var showSimpleDateRangePickerDialogWithNepali by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { showSimpleDateRangePickerDialog = true }) {
            Text("Simple Nepali Date Range Picker")
        }
        Button(onClick = { showSimpleDateRangePickerDialogWithNepali = true }) {
            Text(
                "Simple Nepali Date Range Picker in Nepali\n(Short format)",
                textAlign = TextAlign.Center
            )
        }

        Button(onClick = { showDateRangePickerDialogWithEnglish = true }) {
            Text("Nepali Date Range Picker with English")
        }

        Button(onClick = { showDateRangePickerDialogWithEnglishInNepali = true }) {
            Text("Nepali Date Range Picker with English in Nepali")
        }

        if (showSimpleDateRangePickerDialog) {
            NepaliDatePickerDialog(
                onDismissRequest = { showSimpleDateRangePickerDialog = false },
                confirmButton = {
                    NepaliDatePickerDefaults.DialogButton(
                        "OK",
                        onButtonClick = { showSimpleDateRangePickerDialog = false })
                },
                dismissButton = {
                    NepaliDatePickerDefaults.DialogButton(
                        "Cancel",
                        onButtonClick = { showSimpleDateRangePickerDialog = false }
                    )
                }
            ) {
                NepaliDateRangePicker(
                    defaultState,
                    showMonthsVertically = monthsVertically,
                    showYearPickerAndMonthNavigation = showRangePickerWithYearPickerAndNavigation,
                    showTodayButton = showTodayButton
                )
            }
        }

        if (showSimpleDateRangePickerDialogWithNepali) {
            NepaliDatePickerDialog(
                onDismissRequest = { showSimpleDateRangePickerDialogWithNepali = false },
                confirmButton = {
                    NepaliDatePickerDefaults.DialogButton(
                        "OK",
                        onButtonClick = { showSimpleDateRangePickerDialogWithNepali = false })
                },
                dismissButton = {
                    NepaliDatePickerDefaults.DialogButton(
                        "Cancel",
                        onButtonClick = { showSimpleDateRangePickerDialogWithNepali = false }
                    )
                }
            ) {
                NepaliDateRangePicker(
                    defaultStateWithNepali,
                    showMonthsVertically = monthsVertically,
                    showYearPickerAndMonthNavigation = showRangePickerWithYearPickerAndNavigation,
                    showTodayButton = showTodayButton
                )
            }
        }

        if (showDateRangePickerDialogWithEnglish) {
            NepaliDatePickerDialog(
                onDismissRequest = { showDateRangePickerDialogWithEnglish = false },
                confirmButton = {
                    NepaliDatePickerDefaults.DialogButton(
                        "OK",
                        onButtonClick = { showDateRangePickerDialogWithEnglish = false })
                },
                dismissButton = {
                    NepaliDatePickerDefaults.DialogButton(
                        "Cancel",
                        onButtonClick = { showDateRangePickerDialogWithEnglish = false }
                    )
                }
            ) {
                NepaliDateRangePickerWithEnglishDate(
                    secondState,
                    showMonthsVertically = monthsVertically,
                    showYearPickerAndMonthNavigation = showRangePickerWithYearPickerAndNavigation,
                    showTodayButton = showTodayButton
                )
            }
        }
        if (showDateRangePickerDialogWithEnglishInNepali) {
            NepaliDatePickerDialog(
                onDismissRequest = { showDateRangePickerDialogWithEnglishInNepali = false },
                confirmButton = {
                    NepaliDatePickerDefaults.DialogButton(
                        "OK",
                        onButtonClick = { showDateRangePickerDialogWithEnglishInNepali = false })
                },
                dismissButton = {
                    NepaliDatePickerDefaults.DialogButton(
                        "Cancel",
                        onButtonClick = { showDateRangePickerDialogWithEnglishInNepali = false }
                    )
                }
            ) {
                NepaliDateRangePickerWithEnglishDate(
                    secondStateWithNepali,
                    showMonthsVertically = monthsVertically,
                    showYearPickerAndMonthNavigation = showRangePickerWithYearPickerAndNavigation,
                    showTodayButton = showTodayButton,
                    englishDateLocale = NepaliDatePickerDefaults.DefaultRangePickerLocale.copy(
                        language = NepaliDatePickerLang.NEPALI
                    )
                )
            }
        }
    }
}

@Composable
fun NepaliDatePickerForScreenShots(
    modifier: Modifier = Modifier,
    localeInt: Int,
    withYearRange: Boolean = false,
    showTodayButton: Boolean = true,
    state: NepaliDatePickerState,
) {
    var showNepaliDatePickerDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {


        Button({ showNepaliDatePickerDialog = true }) {
            Text("Select date")
        }

        if (showNepaliDatePickerDialog) {
            NepaliDatePickerDialog(confirmButton = {
                TextButton(onClick = { showNepaliDatePickerDialog = false }) {
                    Text(text = "OK")
                }
            }, dismissButton = {
                TextButton(onClick = { showNepaliDatePickerDialog = false }) {
                    Text(text = "Cancel")
                }
            }, onDismissRequest = { showNepaliDatePickerDialog = false }) {
                NepaliDatePicker(state = state, showTodayButton = showTodayButton)
            }
        }
    }
}

@Composable
fun NepaliDatePickerForNepaliScreenShots(
    modifier: Modifier = Modifier,
    localeInt: Int,
    withSelectables: Boolean = false,
    showTodayButton: Boolean = true,
    state: NepaliDatePickerState
) {
    var showNepaliDatePickerDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {


        Button({ showNepaliDatePickerDialog = true }) {
            Text("क्यालेन्डरबाट मिति छान्नुहोस्")
        }


        if (showNepaliDatePickerDialog) {
            NepaliDatePickerDialog(confirmButton = {
                TextButton(onClick = { showNepaliDatePickerDialog = false }) {
                    Text(text = "ठीक छ")
                }
            }, dismissButton = {
                TextButton(onClick = { showNepaliDatePickerDialog = false }) {
                    Text(text = "रद्द गर्नुहोस्")
                }
            }, onDismissRequest = { showNepaliDatePickerDialog = false }) {
                NepaliDatePicker(state = state, showTodayButton = showTodayButton)
            }
        }
    }
}