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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerColors
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import kotlinx.coroutines.launch

/**
 * NepaliDatePickerWithEnglishDate lets user select a date and preferably should be embedded into Dialogs.
 * Check [NepaliDatePickerDialog].
 *
 * Nepali date picker lets you pick a Nepali date via a calendar UI which displays both Nepali and
 * English dates.
 *
 * @param state state of the date picker. See [rememberNepaliDatePickerState].
 * @param modifier the [Modifier] to be applied to this date picker
 * @param englishDateLocale the locale [NepaliDateLocale] for the english date
 * @param title the title to be displayed in the date picker
 * @param headline the headline to be displayed in the date picker
 * @param showModeToggle the boolean to let user toggle between Date Picker and Date Input
 * @param showTodayButton the boolean to control either to show `TODAY` button or not
 * @param colors [NepaliDatePickerColors] that will be used to resolve the colors used for this date
 * picker in different states. See [NepaliDatePickerDefaults.colors].
 *
 * Example usage:
 * ```
 * val defaultNepaliDatePickerState = rememberNepaliDatePickerState()
 *
 * NepaliDatePickerWithEnglishDate(state = defaultNepaliDatePickerState)
 * ```
 *
 * @see NepaliDatePickerDialog
 * @see NepaliDatePicker
 * @see NepaliDateRangePicker
 * @see NepaliDateRangePickerWithEnglishDate
 */
@Composable
fun NepaliDatePickerWithEnglishDate(
    state: NepaliDatePickerState,
    modifier: Modifier = Modifier,
    englishDateLocale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultLocale,
    title: (@Composable () -> Unit)? = {
        NepaliDatePickerDefaults.NepaliDatePickerTitle(
            modifier = Modifier.padding(NepaliDatePickerTitlePadding),
            language = state.locale.language,
            displayMode = state.displayMode
        )
    },
    headline: (@Composable () -> Unit)? = {
        NepaliDatePickerDefaults.NepaliDatePickerHeadlineWithEnglishDate(
            modifier = Modifier.padding(NepaliDatePickerHeadlinePadding),
            selectedDate = state.selectedDate,
            selectedEnglishDate = state.selectedEnglishDate,
            locale = state.locale,
            englishLocale = englishDateLocale,
            displayMode = state.displayMode
        )
    },
    showModeToggle: Boolean = true,
    showTodayButton: Boolean = true,
    colors: NepaliDatePickerColors = NepaliDatePickerDefaults.colors()
) {
    val calendarModel = remember(state.locale) { NepaliCalendarModel(state.locale) }
    // `today` reads the wall clock; remember it so it isn't recomputed on every recomposition.
    val today = remember(calendarModel) { calendarModel.todayNepaliSimpleDate }

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
                        language = state.locale.language
                    )
                }
            } else {
                null
            },
        colors = colors,
        headerMinHeight = HeaderContainerHeight
    ) {
        SwitchableNepaliDateEntryContent(
            selectedDate = state.selectedDate,
            nepaliSelectableDates = state.nepaliSelectableDates,
            onDateSelectionChange = { customCalendar -> state.selectedDate = customCalendar },
            calendarModel = calendarModel,
            yearRange = state.yearRange,
            colors = colors,
            language = state.locale.language,
            nepaliDisplayMode = state.displayMode,
        ) {
            NepaliDatePicker(
                selectedDate = state.selectedDate,
                nepaliSelectableDates = state.nepaliSelectableDates,
                displayedMonth = state.displayedMonth,
                onDateSelectionChange = { customCalendar -> state.selectedDate = customCalendar },
                onDisplayedMonthChange = { month ->
                    state.displayedMonth = month
                },
                calendarModel = calendarModel,
                yearRange = state.yearRange,
                showTodayButton = showTodayButton,
                englishDateLocale = englishDateLocale,
                colors = colors,
                today = today
            )
        }
    }
}

@Composable
private fun NepaliDatePicker(
    selectedDate: CustomCalendar?,
    displayedMonth: NepaliMonthCalendar,
    onDateSelectionChange: (CustomCalendar) -> Unit,
    onDisplayedMonthChange: (NepaliMonthCalendar) -> Unit,
    calendarModel: NepaliCalendarModel,
    yearRange: IntRange,
    nepaliSelectableDates: NepaliSelectableDates,
    showTodayButton: Boolean,
    englishDateLocale: NepaliDateLocale,
    colors: NepaliDatePickerColors,
    today: SimpleDate
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
        NepaliMonthsNavigation(
            modifier = Modifier.padding(horizontal = DatePickerHorizontalPadding),
            isToday = isToday,
            todayText = chosenLanguage.today,
            nextAvailable = monthsListState.canScrollForward,
            previousAvailable = monthsListState.canScrollBackward,
            yearPickerVisible = yearPickerVisible,
            yearPickerText = formattedMonthYear,
            yearPickerSubtitle = formattedEnglishMonthYear,
            previousMonthContentDescription = chosenLanguage.previousMonthContentDescription,
            nextMonthContentDescription = chosenLanguage.nextMonthContentDescription,
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
        Box {
            Column(modifier = Modifier.padding(horizontal = DatePickerHorizontalPadding)) {
                NepaliWeekDays(
                    colors = colors,
                    language = chosenLanguage,
                    weekDayFormat = weekDayFormat
                )
                NepaliHorizontalMonthList(
                    today = today,
                    lazyListState = monthsListState,
                    yearRange = yearRange,
                    onDateSelectionChange = onDateSelectionChange,
                    onDisplayedMonthChange = onDisplayedMonthChange,
                    selectedDate = selectedDate,
                    nepaliSelectableDates = nepaliSelectableDates,
                    calendarModel = calendarModel,
                    englishDateLanguage = englishDateLanguage,
                    colors = colors
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = yearPickerVisible,
                modifier = Modifier.clipToBounds(),
                enter = expandVertically() + fadeIn(initialAlpha = 0.6f),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    NepaliYearPicker(
                        // Keep the height the same as the monthly calendar + weekdays height, and
                        // take into account the thickness of the divider that will be composed
                        // below it.
                        modifier = Modifier.requiredHeight(
                            RecommendedSizeForAccessibility * (NepaliMaxCalendarRows + 1) - HalfDaySizeWithDivider
                        ).padding(horizontal = DatePickerHorizontalPadding),
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
private fun NepaliHorizontalMonthList(
    today: SimpleDate,
    lazyListState: LazyListState,
    yearRange: IntRange,
    onDateSelectionChange: (CustomCalendar) -> Unit,
    onDisplayedMonthChange: (NepaliMonthCalendar) -> Unit,
    selectedDate: CustomCalendar?,
    calendarModel: NepaliCalendarModel,
    nepaliSelectableDates: NepaliSelectableDates,
    englishDateLanguage: NepaliDatePickerLang,
    colors: NepaliDatePickerColors,
) {
    val firstMonth by remember(yearRange) {
        derivedStateOf {
            calendarModel.getNepaliMonth(
                nepaliYear = yearRange.first, nepaliMonth = 1
            )
        }
    }
    val snapFlingBehavior = rememberCustomSnapFlingBehavior(lazyListState = lazyListState)

    LazyRow(
        modifier = Modifier, state = lazyListState, flingBehavior = snapFlingBehavior
    ) {
        items(
            count = numberOfMonthsInRange(yearRange = yearRange),
            key = { index: Int -> index }) { index ->
            val monthCalendar = remember(index, calendarModel, firstMonth) {
                calendarModel.plusNepaliMonths(
                    fromNepaliCalendar = firstMonth, addedMonthsCount = index
                )
            }
            Box(
                modifier = Modifier.fillParentMaxWidth()
            ) {
                NepaliMonth(
                    monthCalendar = monthCalendar,
                    todayDate = today,
                    startDate = selectedDate,
                    endDate = null,
                    calendarModel = calendarModel,
                    onDateSelectionChange = onDateSelectionChange,
                    nepaliSelectableDates = nepaliSelectableDates,
                    colors = colors,
                    dayShape = RoundedCornerShape(4.dp),
                    englishDateLanguage = englishDateLanguage
                )
            }
        }
    }

    LaunchedEffect(lazyListState) {
        updateDisplayedMonth(
            lazyListState = lazyListState,
            calendarModel = calendarModel,
            onDisplayedMonthChange = onDisplayedMonthChange,
            yearRange = yearRange
        )
    }
}

private val NepaliDatePickerHeadlinePadding =
    PaddingValues(start = 24.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
private val HalfDaySizeWithDivider = 25.dp