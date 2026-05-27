/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.shivathapaa.nepalidatepickerkmp

import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerColors
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import kotlinx.coroutines.launch

/**
 * NNepaliDateRangePickerWithEnglishDate let people select a range of Nepali dates and can be embedded into dialogs.
 * See [NepaliDatePickerDialog]
 *
 * Nepali date picker lets you pick Nepali dates via a calendar UI which displays both Nepali and
 * English dates.
 *
 * @param state state of the Nepali date range picker. See [rememberNepaliDateRangePickerState].
 * @param modifier the [Modifier] to be applied to this Nepali date range picker.
 * @param englishDateLocale the locale [NepaliDateLocale] for the english date
 * @param title the title to be displayed in the Nepali date range picker
 * @param headline the headline to be displayed in the Nepali date range picker
 * @param showModeToggle the boolean to let user toggle between Date Picker and Date Input
 * @param showTodayButton the control to either show or hide `TODAY` button for navigating to today's date.
 * It is also affected by [showYearPickerAndMonthNavigation].
 * @param showMonthsVertically the control to either show months vertically or horizontally.
 * Note: When set to `true` (vertical display), the date picker uses vertical scrolling. This can
 * lead to runtime errors if placed within a composable that also uses vertical scrolling, such as
 * a `Column` with vertical scroll or a `LazyColumn`. Ensure your layout accommodates the vertical
 * scrolling behavior to avoid these errors.
 * @param showYearPickerAndMonthNavigation the control to either show or hide `YearPicker` button and
 * `MonthNavigation` buttons. [showTodayButton] is affected by this.
 * @param colors [NepaliDatePickerColors] that will be used to resolve the colors used for this date
 * picker in different states. See [NepaliDatePickerDefaults.colors].
 *
 * Example usage:
 * ```
 * val defaultNepaliDateRangePickerState = rememberNepaliDateRangePickerState()
 *
 * NepaliDateRangePickerWithEnglishDate(defaultNepaliDateRangePickerState)
 * ```
 *
 * @see NepaliDatePickerDialog
 * @see NepaliDatePicker
 * @see rememberNepaliDateRangePickerState
 */
@ExperimentalNepaliDatePickerApi
@Composable
fun NepaliDateRangePickerWithEnglishDate(
    state: NepaliDateRangePickerState,
    modifier: Modifier = Modifier,
    englishDateLocale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultLocale,
    title: (@Composable () -> Unit)? = {
        NepaliDatePickerDefaults.NepaliDateRangePickerTitle(
            modifier = Modifier.padding(NepaliDateRangePickerTitlePadding),
            language = state.locale.language,
            displayMode = state.displayMode
        )
    },
    headline: (@Composable () -> Unit)? = {
        NepaliDatePickerDefaults.NepaliDateRangePickerHeadlineWithEnglishDate(
            modifier = Modifier.padding(NepaliEnglishDateRangePickerHeadlinePadding),
            selectedNepaliStartDate = state.selectedStartNepaliDate,
            selectedNepaliEndDate = state.selectedEndNepaliDate,
            selectedEnglishStartDate = state.selectedStartEnglishDate,
            selectedEnglishEndDate = state.selectedEndEnglishDate,
            englishLocale = englishDateLocale,
            locale = state.locale
        )
    },
    showModeToggle: Boolean = true,
    showTodayButton: Boolean = true,
    showMonthsVertically: Boolean = true,
    showYearPickerAndMonthNavigation: Boolean = true,
    colors: NepaliDatePickerColors = NepaliDatePickerDefaults.colors()
) {
    val calendarModel = NepaliCalendarModel(state.locale)
    val today = calendarModel.todayNepaliSimpleDate

    NepaliDateEntryContainer(
        modifier = modifier,
        title = title,
        headline = headline,
        modeToggleButton =
            if (showModeToggle) {
                {
                    NepaliDisplayModeToggleButton(
                        modifier = Modifier.padding(NepaliDatePickerModeTogglePadding),
                        displayMode = state.displayMode,
                        onDisplayModeChange = { displayMode -> state.displayMode = displayMode },
                    )
                }
            } else {
                null
            },
        colors = colors,
        headlineTextStyle = MaterialTheme.typography.titleLarge.copy(fontSize = 21.sp),
        headerMinHeight = NepaliRangeSelectionHeaderContainerHeight - NepaliRangePickerHeaderHeightOffset,
    ) {
        SwitchableNepaliDateRangeEntryContent(
            selectedNepaliStartDate = state.selectedStartNepaliDate,
            selectedNepaliEndDate = state.selectedEndNepaliDate,
            onDatesSelectionChange = { startNepaliCalendar, endNepaliCalendar ->
                try {
                    state.setSelection(
                        startNepaliDate = startNepaliCalendar,
                        endNepaliDate = endNepaliCalendar
                    )
                } catch (iae: IllegalArgumentException) {
                    // By default, ignore exceptions that setSelection throws.
                }
            },
            calendarModel = calendarModel,
            colors = colors,
            nepaliSelectableDates = state.nepaliSelectableDates,
            yearRange = state.yearRange,
            language = state.locale.language,
            nepaliDisplayMode = state.displayMode
        ) {
            NepaliEnglishDateRangePicker(
                selectedNepaliStartDate = state.selectedStartNepaliDate,
                selectedNepaliEndDate = state.selectedEndNepaliDate,
                displayedMonth = state.displayedMonth,
                onDatesSelectionChange = { startNepaliCalendar, endNepaliCalendar ->
                    try {
                        state.setSelection(
                            startNepaliDate = startNepaliCalendar,
                            endNepaliDate = endNepaliCalendar
                        )
                    } catch (iae: IllegalArgumentException) {
                        // By default, ignore exceptions that setSelection throws.
                    }
                },
                calendarModel = calendarModel,
                colors = colors,
                showTodayButton = showTodayButton,
                nepaliSelectableDates = state.nepaliSelectableDates,
                yearRange = state.yearRange,
                onDisplayedMonthChange = {
                    state.displayedMonth = it
                },
                today = today,
                englishDateLocale = englishDateLocale,
                showMonthsVertically = showMonthsVertically,
                showYearPickerAndMonthNavigation = showYearPickerAndMonthNavigation
            )
        }
    }
}

@Composable
private fun NepaliEnglishDateRangePicker(
    selectedNepaliStartDate: CustomCalendar?,
    selectedNepaliEndDate: CustomCalendar?,
    displayedMonth: NepaliMonthCalendar,
    onDatesSelectionChange: (startNepaliCalendar: CustomCalendar?, endNepaliCalendar: CustomCalendar?) -> Unit,
    onDisplayedMonthChange: (NepaliMonthCalendar) -> Unit,
    calendarModel: NepaliCalendarModel,
    yearRange: IntRange,
    nepaliSelectableDates: NepaliSelectableDates,
    showTodayButton: Boolean,
    colors: NepaliDatePickerColors,
    today: SimpleDate,
    englishDateLocale: NepaliDateLocale,
    showMonthsVertically: Boolean,
    showYearPickerAndMonthNavigation: Boolean
) {
    val displayedMonthIndex by remember(displayedMonth) {
        derivedStateOf { displayedMonth.indexIn(yearRange) }
    }
    val initialIndex = today.indexIn(yearRange)

    val isToday by remember(displayedMonthIndex) {
        derivedStateOf { displayedMonthIndex == initialIndex }
    }

    val englishDateLanguage = englishDateLocale.language

    val monthsListState = rememberLazyListState(initialFirstVisibleItemIndex = displayedMonthIndex)

    val coroutineScope = rememberCoroutineScope()
    var yearPickerVisible by rememberSaveable { mutableStateOf(false) }

    val chosenLanguage = calendarModel.locale.language
    val weekDayFormat = calendarModel.locale.weekDayName
    val fullMonthName = chosenLanguage.months[displayedMonth.month - 1].full
    val fullYear = calendarModel.localizeNumber(
        stringToLocalize = displayedMonth.year.toString(), locale = chosenLanguage
    )
    val formattedMonthYear = "$fullMonthName $fullYear"

    val englishCalendar by remember(displayedMonth) {
        derivedStateOf {
            calendarModel.convertToEnglishDate(
                displayedMonth.year,
                displayedMonth.month,
                2
            )
        }
    }

    val currentEnglishMonth = englishCalendar.month
    val currentEnglishMonthName =
        calendarModel.getEnglishMonthName(
            currentEnglishMonth,
            englishDateLanguage,
            NameFormat.SHORT
        )
    val fullEnglishYear = calendarModel.localizeNumber(
        stringToLocalize = englishCalendar.year.toString(), locale = englishDateLanguage
    )

    val nextMonth = currentEnglishMonth + 1
    // Adjust to ensure the month is within the 1-12 range
    val adjustedNextMonth = if (nextMonth > 12) nextMonth - 12 else nextMonth
    val nextMonthName =
        calendarModel.getEnglishMonthName(adjustedNextMonth, englishDateLanguage, NameFormat.SHORT)

    val formattedEnglishMonthYear = "$currentEnglishMonthName/$nextMonthName $fullEnglishYear"

    Column {
        if (showYearPickerAndMonthNavigation) {
            NepaliEnglishMonthsNavigation(
                modifier = Modifier.padding(horizontal = DatePickerHorizontalPadding),
                isToday = isToday,
                todayText = chosenLanguage.today,
                nextAvailable = monthsListState.canScrollForward,
                previousAvailable = monthsListState.canScrollBackward,
                yearPickerVisible = yearPickerVisible,
                yearPickerText = formattedMonthYear,
                englishMonthYearText = formattedEnglishMonthYear,
                showTodayButton = showTodayButton,
                onNextClicked = {
                    coroutineScope.launch {
                        try {
                            monthsListState.animateScrollToItem(
                                monthsListState.firstVisibleItemIndex + 1
                            )
                        } catch (_: IllegalArgumentException) {
                            // Ignore. This may happen if the user clicked the "next" arrow fast while
                            // the list was still animating to the next item.
                        }
                    }
                },
                onPreviousClicked = {
                    coroutineScope.launch {
                        try {
                            monthsListState.animateScrollToItem(
                                monthsListState.firstVisibleItemIndex - 1
                            )
                        } catch (_: IllegalArgumentException) {
                            // Ignore. This may happen if the user clicked the "previous" arrow fast
                            // while  the list was still animating to the previous item.
                        }
                    }
                },
                onTodayClicked = {
                    coroutineScope.launch { monthsListState.scrollToItem(initialIndex) }
                },
                onYearPickerButtonClicked = { yearPickerVisible = !yearPickerVisible },
                colors = colors
            )
        }

        Box {
            Column(modifier = Modifier.padding(horizontal = DatePickerHorizontalPadding)) {
                NepaliWeekDays(
                    colors = colors, language = chosenLanguage, weekDayFormat = weekDayFormat
                )

                HorizontalVerticalMonthsList(
                    lazyListState = monthsListState,
                    selectedNepaliStartDate = selectedNepaliStartDate,
                    selectedNepaliEndDate = selectedNepaliEndDate,
                    onDatesSelectionChange = onDatesSelectionChange,
                    onDisplayedMonthChange = onDisplayedMonthChange,
                    calendarModel = calendarModel,
                    chosenLanguage = chosenLanguage,
                    yearRange = yearRange,
                    englishDateLanguage = englishDateLanguage,
                    colors = colors,
                    today = today,
                    nepaliSelectableDates = nepaliSelectableDates,
                    showMonthsVertically = showMonthsVertically
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = yearPickerVisible,
                modifier = Modifier.clipToBounds(),
                enter = expandVertically() + fadeIn(initialAlpha = 0.6f),
                exit = shrinkVertically() + fadeOut()
            ) {
                val nepaliYearPickerModifier: Modifier = if (showMonthsVertically) {
                    Modifier.padding(horizontal = DatePickerHorizontalPadding)
                } else {
                    // Keep the height the same as the monthly calendar + weekdays height, and
                    // take into account the thickness of the divider that will be composed
                    // below it.
                    Modifier.requiredHeight(
                        RecommendedSizeForAccessibility * (NepaliMaxCalendarRows + 1) - 1.dp
                    ).padding(horizontal = DatePickerHorizontalPadding)
                }

                Column {
                    NepaliYearPicker(
                        modifier = nepaliYearPickerModifier,
                        currentYear = today.year,
                        displayedYear = displayedMonth.year,
                        onYearSelected = { year ->
                            // Switch back to the monthly calendar and scroll to the selected year.
                            yearPickerVisible = !yearPickerVisible
                            coroutineScope.launch {
                                // Scroll to the selected year (maintaining the month of year).
                                // A LaunchEffect at the MonthsList will take care of rest and will
                                // update the state's displayedMonth to the month we scrolled to.
                                monthsListState.scrollToItem(
                                    (year - yearRange.first) * 12 + displayedMonth.month - 1
                                )
                            }
                        },
                        nepaliSelectableDates = nepaliSelectableDates,
                        calendarModel = calendarModel,
                        yearRange = yearRange,
                        colors = colors
                    )
                    HorizontalDivider(color = colors.dividerColor)
                }
            }
        }
    }
}

@Composable
private fun HorizontalVerticalMonthsList(
    today: SimpleDate,
    lazyListState: LazyListState,
    selectedNepaliStartDate: CustomCalendar?,
    selectedNepaliEndDate: CustomCalendar?,
    chosenLanguage: NepaliDatePickerLang,
    onDatesSelectionChange: (startNepaliCalendar: CustomCalendar?, endNepaliCalendar: CustomCalendar?) -> Unit,
    onDisplayedMonthChange: (NepaliMonthCalendar) -> Unit,
    calendarModel: NepaliCalendarModel,
    englishDateLanguage: NepaliDatePickerLang,
    yearRange: IntRange,
    nepaliSelectableDates: NepaliSelectableDates,
    showMonthsVertically: Boolean,
    colors: NepaliDatePickerColors
) {
    val firstMonth by remember(yearRange) {
        derivedStateOf {
            calendarModel.getNepaliMonth(
                nepaliYear = yearRange.first, nepaliMonth = 1
            )
        }
    }

    ProvideTextStyle(MaterialTheme.typography.bodyLarge) {
        // The updateDateSelection will invoke the onDatesSelectionChange with the proper
        // selection according to the current state.
        val onDateSelectionChange = { nepaliCalendar: CustomCalendar ->
            updateNepaliDateSelection(
                nepaliCalendar = nepaliCalendar,
                currentNepaliStartDate = selectedNepaliStartDate,
                currentNepaliEndDate = selectedNepaliEndDate,
                onDatesSelectionChange = onDatesSelectionChange,
                compareDates = calendarModel::compareDates
            )
        }

        if (showMonthsVertically) {
            VerticalNepaliEnglishMonthView(
                lazyListState = lazyListState,
                today = today,
                yearRange = yearRange,
                calendarModel = calendarModel,
                chosenLanguage = chosenLanguage,
                firstMonth = firstMonth,
                englishDateLanguage = englishDateLanguage,
                selectedNepaliStartDate = selectedNepaliStartDate,
                selectedNepaliEndDate = selectedNepaliEndDate,
                onDateSelectionChange = onDateSelectionChange,
                nepaliSelectableDates = nepaliSelectableDates,
                colors = colors
            )
        } else {
            HorizontalNepaliEnglishMonthView(
                lazyListState = lazyListState,
                today = today,
                yearRange = yearRange,
                calendarModel = calendarModel,
                englishDateLanguage = englishDateLanguage,
                firstMonth = firstMonth,
                selectedNepaliStartDate = selectedNepaliStartDate,
                selectedNepaliEndDate = selectedNepaliEndDate,
                onDateSelectionChange = onDateSelectionChange,
                nepaliSelectableDates = nepaliSelectableDates,
                colors = colors
            )
        }

    }

    LaunchedEffect(lazyListState) {
        updateDisplayedMonth(
            lazyListState = lazyListState,
            onDisplayedMonthChange = onDisplayedMonthChange,
            calendarModel = calendarModel,
            yearRange = yearRange
        )
    }
}


@Composable
private fun HorizontalNepaliEnglishMonthView(
    lazyListState: LazyListState,
    today: SimpleDate,
    yearRange: IntRange,
    calendarModel: NepaliCalendarModel,
    englishDateLanguage: NepaliDatePickerLang,
    firstMonth: NepaliMonthCalendar,
    selectedNepaliStartDate: CustomCalendar?,
    selectedNepaliEndDate: CustomCalendar?,
    onDateSelectionChange: (CustomCalendar) -> Unit,
    nepaliSelectableDates: NepaliSelectableDates,
    colors: NepaliDatePickerColors
) {
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState)

    LazyRow(
        modifier = Modifier, state = lazyListState, flingBehavior = snapFlingBehavior
    ) {
        items(numberOfMonthsInRange(yearRange)) { index ->
            val monthCalendar = remember(index, calendarModel, firstMonth) {
                calendarModel.plusNepaliMonths(
                    fromNepaliCalendar = firstMonth, addedMonthsCount = index
                )
            }

            Column(modifier = Modifier.fillParentMaxWidth()) {
                val nepaliRangeSelectionInfo: NepaliSelectedRangeInfo? =
                    if (selectedNepaliStartDate != null && selectedNepaliEndDate != null) {
                        remember(selectedNepaliStartDate, selectedNepaliEndDate) {
                            NepaliSelectedRangeInfo.calculateRangeInfo(
                                nepaliMonthCalendar = monthCalendar,
                                startNepaliCalendar = selectedNepaliStartDate,
                                endNepaliCalendar = selectedNepaliEndDate,
                                compareDates = calendarModel::compareDates
                            )
                        }
                    } else {
                        null
                    }

                NepaliEnglishMonth(
                    monthCalendar = monthCalendar,
                    onDateSelectionChange = onDateSelectionChange,
                    todayDate = today,
                    startDate = selectedNepaliStartDate,
                    endDate = selectedNepaliEndDate,
                    calendarModel = calendarModel,
                    nepaliSelectedRangeInfo = nepaliRangeSelectionInfo,
                    nepaliSelectableDates = nepaliSelectableDates,
                    englishDateLanguage = englishDateLanguage,
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun VerticalNepaliEnglishMonthView(
    lazyListState: LazyListState,
    today: SimpleDate,
    yearRange: IntRange,
    calendarModel: NepaliCalendarModel,
    firstMonth: NepaliMonthCalendar,
    chosenLanguage: NepaliDatePickerLang,
    englishDateLanguage: NepaliDatePickerLang,
    selectedNepaliStartDate: CustomCalendar?,
    selectedNepaliEndDate: CustomCalendar?,
    onDateSelectionChange: (CustomCalendar) -> Unit,
    nepaliSelectableDates: NepaliSelectableDates,
    colors: NepaliDatePickerColors
) {
    LazyColumn(
        modifier = Modifier, state = lazyListState
    ) {
        items(numberOfMonthsInRange(yearRange)) { index ->
            val monthCalendar = remember(index, calendarModel, firstMonth) {
                calendarModel.plusNepaliMonths(
                    fromNepaliCalendar = firstMonth, addedMonthsCount = index
                )
            }

            val monthName =
                calendarModel.getNepaliMonthName(
                    monthCalendar.month,
                    NameFormat.FULL,
                    chosenLanguage
                )
            val year =
                calendarModel.localizeNumber(monthCalendar.year.toString(), chosenLanguage)

            Column(modifier = Modifier.fillParentMaxWidth()) {
                ProvideTextStyle(MaterialTheme.typography.titleSmall) {
                    Text(
                        text = "$monthName $year",
                        modifier = Modifier.padding(paddingValues = CalendarMonthSubheadPadding),
                        color = colors.subheadContentColor
                    )
                }
                val nepaliRangeSelectionInfo: NepaliSelectedRangeInfo? =
                    if (selectedNepaliStartDate != null && selectedNepaliEndDate != null) {
                        remember(selectedNepaliStartDate, selectedNepaliEndDate) {
                            NepaliSelectedRangeInfo.calculateRangeInfo(
                                nepaliMonthCalendar = monthCalendar,
                                startNepaliCalendar = selectedNepaliStartDate,
                                endNepaliCalendar = selectedNepaliEndDate,
                                compareDates = calendarModel::compareDates
                            )
                        }
                    } else {
                        null
                    }

                NepaliEnglishMonth(
                    monthCalendar = monthCalendar,
                    onDateSelectionChange = onDateSelectionChange,
                    englishDateLanguage = englishDateLanguage,
                    todayDate = today,
                    startDate = selectedNepaliStartDate,
                    endDate = selectedNepaliEndDate,
                    calendarModel = calendarModel,
                    nepaliSelectedRangeInfo = nepaliRangeSelectionInfo,
                    nepaliSelectableDates = nepaliSelectableDates,
                    colors = colors
                )
            }
        }
    }
}

private val NepaliEnglishDateRangePickerHeadlinePadding =
    PaddingValues(start = 24.dp, end = 12.dp, bottom = 4.dp)