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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
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
import dev.shivathapaa.nepalidatepickerkmp.data.toNepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import kotlinx.coroutines.launch

/**
 * Nepali Date Range Picker let people select a range of Nepali dates and can be embedded into dialogs.
 * See [NepaliDatePickerDialog]
 *
 * Nepali date picker lets you pick Nepali dates via a calendar UI which displays Nepali dates.
 *
 * @param state state of the Nepali date range picker. See [rememberNepaliDateRangePickerState].
 * @param modifier the [Modifier] to be applied to this Nepali date range picker.
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
 *@param showYearPickerAndMonthNavigation the control to either show or hide `YearPicker` button and
 * `MonthNavigation` buttons. [showTodayButton] is affected by this.
 * @param colors [NepaliDatePickerColors] that will be used to resolve the colors used for this date
 * picker in different states. See [NepaliDatePickerDefaults.colors].
 *
 * Example usage:
 * ```
 * val defaultNepaliDateRangePickerState = rememberNepaliDateRangePickerState()
 *
 * NepaliDateRangePicker(defaultNepaliDateRangePickerState)
 * ```
 *
 * @see NepaliDatePickerDialog
 * @see NepaliDatePicker
 * @see rememberNepaliDateRangePickerState
 */
@ExperimentalNepaliDatePickerApi
@Composable
fun NepaliDateRangePicker(
    state: NepaliDateRangePickerState,
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = {
        NepaliDatePickerDefaults.NepaliDateRangePickerTitle(
            modifier = Modifier.padding(NepaliDateRangePickerTitlePadding),
            language = state.locale.language,
            displayMode = state.displayMode
        )
    },
    headline: (@Composable () -> Unit)? = {
        NepaliDatePickerDefaults.NepaliDateRangePickerHeadline(
            modifier = Modifier.padding(NepaliDateRangePickerHeadlinePadding),
            selectedStartDate = state.selectedStartNepaliDate,
            selectedEndDate = state.selectedEndNepaliDate,
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
    val today = calendarModel.today.toSimpleDate()

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
            NepaliDateRangePicker(
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
                showMonthsVertically = showMonthsVertically,
                showYearPickerAndMonthNavigation = showYearPickerAndMonthNavigation
            )
        }
    }
}

@Composable
internal fun SwitchableNepaliDateRangeEntryContent(
    selectedNepaliStartDate: CustomCalendar?,
    selectedNepaliEndDate: CustomCalendar?,
    onDatesSelectionChange: (startNepaliCalendar: CustomCalendar?, endNepaliCalendar: CustomCalendar?) -> Unit,
    calendarModel: NepaliCalendarModel,
    yearRange: IntRange,
    nepaliSelectableDates: NepaliSelectableDates,
    nepaliDisplayMode: DisplayMode,
    colors: NepaliDatePickerColors,
    language: NepaliDatePickerLang,
    pickerContent: @Composable () -> Unit
) {
    val parallaxTarget = with(LocalDensity.current) { -48.dp.roundToPx() }
    AnimatedContent(
        targetState = nepaliDisplayMode,
        transitionSpec = {
            if (targetState == DisplayMode.Input) {
                slideInVertically { height -> height } +
                        fadeIn(
                            animationSpec =
                                tween(
                                    durationMillis = (100.0).toInt(),
                                    delayMillis = (100.0).toInt()
                                )
                        ) togetherWith
                        fadeOut(tween(durationMillis = (100.0).toInt())) +
                        slideOutVertically(targetOffsetY = { _ -> parallaxTarget })
            } else {
                slideInVertically(
                    animationSpec = tween(delayMillis = (50.0).toInt()),
                    initialOffsetY = { _ -> parallaxTarget }
                ) +
                        fadeIn(
                            animationSpec =
                                tween(
                                    durationMillis = (100.0).toInt(),
                                    delayMillis = (100.0).toInt()
                                )
                        ) togetherWith
                        slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }) +
                        fadeOut(animationSpec = tween((100.0).toInt()))
            }
                .using(
                    SizeTransform(
                        clip = true,
                        sizeAnimationSpec = { _, _ ->
                            tween(
                                (500.0).toInt(),
                                easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
                            )
                        }
                    )
                )
        },
        label = "NepaliDatePickerDisplayModeAnimation"
    ) { mode ->
        when (mode) {
            DisplayMode.Picker ->
                pickerContent.invoke()

            DisplayMode.Input ->
                NepaliDateRangeInputContent(
                    selectedStartDate = selectedNepaliStartDate,
                    selectedEndDate = selectedNepaliEndDate,
                    onDatesSelectionChange = onDatesSelectionChange,
                    calendarModel = calendarModel,
                    yearRange = yearRange,
                    language = language,
                    nepaliSelectableDates = nepaliSelectableDates,
                    colors = colors
                )
        }
    }
}

@Composable
private fun NepaliDateRangePicker(
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

    Column {
        if (showYearPickerAndMonthNavigation) {
            NepaliMonthsNavigation(
                modifier = Modifier.padding(horizontal = DatePickerHorizontalPadding),
                isToday = isToday,
                todayText = chosenLanguage.today,
                nextAvailable = monthsListState.canScrollForward,
                previousAvailable = monthsListState.canScrollBackward,
                yearPickerVisible = yearPickerVisible,
                yearPickerText = formattedMonthYear,
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

                VerticalMonthsList(
                    lazyListState = monthsListState,
                    selectedNepaliStartDate = selectedNepaliStartDate,
                    selectedNepaliEndDate = selectedNepaliEndDate,
                    onDatesSelectionChange = onDatesSelectionChange,
                    onDisplayedMonthChange = onDisplayedMonthChange,
                    calendarModel = calendarModel,
                    yearRange = yearRange,
                    chosenLanguage = chosenLanguage,
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
private fun VerticalMonthsList(
    today: SimpleDate,
    lazyListState: LazyListState,
    selectedNepaliStartDate: CustomCalendar?,
    selectedNepaliEndDate: CustomCalendar?,
    onDatesSelectionChange: (startNepaliCalendar: CustomCalendar?, endNepaliCalendar: CustomCalendar?) -> Unit,
    onDisplayedMonthChange: (NepaliMonthCalendar) -> Unit,
    calendarModel: NepaliCalendarModel,
    yearRange: IntRange,
    chosenLanguage: NepaliDatePickerLang,
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
            VerticalMonthView(
                lazyListState = lazyListState,
                today = today,
                yearRange = yearRange,
                calendarModel = calendarModel,
                firstMonth = firstMonth,
                chosenLanguage = chosenLanguage,
                selectedNepaliStartDate = selectedNepaliStartDate,
                selectedNepaliEndDate = selectedNepaliEndDate,
                onDateSelectionChange = onDateSelectionChange,
                nepaliSelectableDates = nepaliSelectableDates,
                colors = colors
            )
        } else {
            HorizontalMonthView(
                lazyListState = lazyListState,
                today = today,
                yearRange = yearRange,
                calendarModel = calendarModel,
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
private fun HorizontalMonthView(
    lazyListState: LazyListState,
    today: SimpleDate,
    yearRange: IntRange,
    calendarModel: NepaliCalendarModel,
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

                NepaliMonth(
                    monthCalendar = monthCalendar,
                    onDateSelectionChange = onDateSelectionChange,
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

@Composable
private fun VerticalMonthView(
    lazyListState: LazyListState,
    today: SimpleDate,
    yearRange: IntRange,
    calendarModel: NepaliCalendarModel,
    firstMonth: NepaliMonthCalendar,
    chosenLanguage: NepaliDatePickerLang,
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

                NepaliMonth(
                    monthCalendar = monthCalendar,
                    onDateSelectionChange = onDateSelectionChange,
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

@Stable
interface NepaliDateRangePickerState {
    val selectedStartNepaliDate: CustomCalendar?
    val selectedEndNepaliDate: CustomCalendar?
    val selectedStartEnglishDate: CustomCalendar?
    val selectedEndEnglishDate: CustomCalendar?
    var displayedMonth: NepaliMonthCalendar
    var displayMode: DisplayMode
    val yearRange: IntRange
    val nepaliSelectableDates: NepaliSelectableDates
    val locale: NepaliDateLocale
    fun setSelection(
        startNepaliDate: CustomCalendar?, endNepaliDate: CustomCalendar?
    )
}

/**
 * Creates a [NepaliDateRangePickerState] for a [NepaliDateRangePicker] that is remembered across compositions.
 *
 * To create a date picker state outside composition, see the `NepaliDateRangePickerState` function.
 *
 * Using it is similar to the [rememberNepaliDatePickerState]. For detailed uses, checkout its examples.
 *
 * @param initialSelectedStartNepaliDate [SimpleDate] that represents an initial selection of a Nepali start date.
 * Provide a `null` to indicate no selection.
 * @param initialSelectedEndNepaliDate [SimpleDate] that represents an initial selection of a Nepali end date.
 * Provide a `null` to indicate no selection.
 * @param initialDisplayedMonth [SimpleDate] that represents an initial selection of a month
 * to be displayed to the user. By default, in case an `initialSelectedDate` is provided, the
 * initial displayed month would be the month of the selected date. Otherwise, in case `null`
 * is provided, the displayed month would be the current one.
 * @param yearRange an [IntRange] that holds the year range that the date picker will be limited to
 * @param nepaliSelectableDates a [NepaliSelectableDates] that is consulted to check if a date is
 * allowed. In case a date is not allowed to be selected, it will appear disabled in the UI. You can
 * checkout helper functions `BeforeDateSelectable`, `AfterDateSelectable`, and `DateRangeSelectable` in `NepaliDateConverter`.
 * @param locale an instance of [NepaliDateLocale] that is used to localize the date picker. It holds
 * the preference for the date picker formatted date and the language of the date picker.
 *
 * @throws [IllegalArgumentException] if the initial selected date or displayed month represent a
 *   year that is out of the year range.
 *
 * @see rememberNepaliDatePickerState
 */
@ExperimentalNepaliDatePickerApi
@Composable
fun rememberNepaliDateRangePickerState(
    initialSelectedStartNepaliDate: SimpleDate? = null,
    initialSelectedEndNepaliDate: SimpleDate? = null,
    initialDisplayedMonth: SimpleDate? = initialSelectedStartNepaliDate,
    yearRange: IntRange = NepaliDatePickerDefaults.NepaliYearRange,
    initialDisplayMode: DisplayMode = DisplayMode.Picker,
    nepaliSelectableDates: NepaliSelectableDates = NepaliDatePickerDefaults.AllDates,
    locale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultRangePickerLocale
): NepaliDateRangePickerState {
    return rememberSaveable(
        saver = NepaliDateRangePickerStateImpl.Saver(
            nepaliSelectableDates, locale
        )
    ) {
        NepaliDateRangePickerStateImpl(
            initialSelectedStartNepaliDate = initialSelectedStartNepaliDate,
            initialSelectedEndNepaliDate = initialSelectedEndNepaliDate,
            initialDisplayedMonth = initialDisplayedMonth,
            yearRange = yearRange,
            nepaliSelectableDates = nepaliSelectableDates,
            initialDisplayMode = initialDisplayMode,
            locale = locale
        )
    }
}

/**
 * Creates a [NepaliDateRangePickerState]. Primarily recommended for outside composition.
 *
 * You are recommended to use [rememberNepaliDateRangePickerState] when inside a composition.
 *
 * @param initialSelectedStartNepaliDate [SimpleDate] that represents an initial selection of a Nepali start date.
 * Provide a `null` to indicate no selection.
 * @param initialSelectedEndNepaliDate [SimpleDate] that represents an initial selection of a Nepali end date.
 * Provide a `null` to indicate no selection.
 * @param initialDisplayedMonth [SimpleDate] that represents an initial selection of a month
 * to be displayed to the user. By default, in case an `initialSelectedDate` is provided, the
 * initial displayed month would be the month of the selected date. Otherwise, in case `null`
 * is provided, the displayed month would be the current one.
 * @param yearRange an [IntRange] that holds the year range that the date picker will be limited to
 * @param nepaliSelectableDates a [NepaliSelectableDates] that is consulted to check if a date is
 * allowed. In case a date is not allowed to be selected, it will appear disabled in the UI. You can
 * checkout helper functions `BeforeDateSelectable`, `AfterDateSelectable`, and `DateRangeSelectable` in `NepaliDateConverter`.
 * @param locale an instance of [NepaliDateLocale] that is used to localize the date picker. It holds
 * the preference for the date picker formatted date and the language of the date picker.
 *
 * @throws [IllegalArgumentException] if the initial selected date or displayed month represent a
 *   year that is out of the year range.
 *
 * @see rememberNepaliDateRangePickerState
 */
fun NepaliDateRangePickerState(
    initialSelectedStartNepaliDate: SimpleDate? = null,
    initialSelectedEndNepaliDate: SimpleDate? = null,
    initialDisplayedMonth: SimpleDate? = initialSelectedStartNepaliDate,
    yearRange: IntRange = NepaliDatePickerDefaults.NepaliYearRange,
    initialDisplayMode: DisplayMode = DisplayMode.Picker,
    nepaliSelectableDates: NepaliSelectableDates = NepaliDatePickerDefaults.AllDates,
    locale: NepaliDateLocale,
): NepaliDateRangePickerState = NepaliDateRangePickerStateImpl(
    initialSelectedStartNepaliDate = initialSelectedStartNepaliDate,
    initialSelectedEndNepaliDate = initialSelectedEndNepaliDate,
    initialDisplayedMonth = initialDisplayedMonth,
    yearRange = yearRange,
    initialDisplayMode = initialDisplayMode,
    nepaliSelectableDates = nepaliSelectableDates,
    locale = locale
)

@Stable
private class NepaliDateRangePickerStateImpl(
    initialSelectedStartNepaliDate: SimpleDate?,
    initialSelectedEndNepaliDate: SimpleDate?,
    initialDisplayedMonth: SimpleDate?,
    yearRange: IntRange,
    initialDisplayMode: DisplayMode,
    nepaliSelectableDates: NepaliSelectableDates,
    locale: NepaliDateLocale
) : BaseNepaliDatePickerStateImpl(
    initialDisplayedMonth = initialDisplayedMonth,
    yearRange = yearRange,
    nepaliSelectableDates = nepaliSelectableDates,
    locale = locale
), NepaliDateRangePickerState {

    private var _selectedStartNepaliDate = mutableStateOf<CustomCalendar?>(null)
    private var _selectedEndNepaliDate = mutableStateOf<CustomCalendar?>(null)

    private var _selectedStartEnglishDate = mutableStateOf<CustomCalendar?>(null)
    private var _selectedEndEnglishDate = mutableStateOf<CustomCalendar?>(null)

    init {
        setSelection(
            startNepaliDate = initialSelectedStartNepaliDate?.let {
                calendarModel.getNepaliCalendar(initialSelectedStartNepaliDate)
            },
            endNepaliDate = initialSelectedEndNepaliDate?.let {
                calendarModel.getNepaliCalendar(initialSelectedEndNepaliDate)
            }
        )
    }

    override val selectedStartNepaliDate: CustomCalendar?
        get() = _selectedStartNepaliDate.value

    override val selectedEndNepaliDate: CustomCalendar?
        get() = _selectedEndNepaliDate.value

    override val selectedStartEnglishDate: CustomCalendar?
        get() = _selectedStartEnglishDate.value

    override val selectedEndEnglishDate: CustomCalendar?
        get() = _selectedEndEnglishDate.value

    private var _displayMode = mutableStateOf(initialDisplayMode)

    override var displayMode: DisplayMode
        get() = _displayMode.value
        set(displayMode) {
            selectedStartNepaliDate?.let {
                displayedMonth = it.toNepaliMonthCalendar()
            }
            _displayMode.value = displayMode
        }

    override fun setSelection(
        startNepaliDate: CustomCalendar?, endNepaliDate: CustomCalendar?
    ) {
        val startNepaliCalendar = if (startNepaliDate != null) {
            require(yearRange.contains(startNepaliDate.year)) {
                "The provided initial date's year (${startNepaliDate.year}) is out of the years range of $yearRange."
            }
            startNepaliDate
        } else {
            null
        }

        val endNepaliCalendar = if (endNepaliDate != null) {
            require(yearRange.contains(endNepaliDate.year)) {
                "The provided initial date's year (${endNepaliDate.year}) is out of the years range of $yearRange."
            }
            endNepaliDate
        } else {
            null
        }

        if (endNepaliCalendar != null) {
            requireNotNull(startNepaliCalendar) { "An end date was provided without a start date." }
            // Validate that the end date appears on or after the start date.
            require(
                calendarModel.compareDates(
                    startNepaliCalendar,
                    endNepaliCalendar.year,
                    endNepaliCalendar.month,
                    endNepaliCalendar.dayOfMonth
                ) < 0
            ) {
                "The provided end date appears before the start date."
            }
        }

        _selectedStartNepaliDate.value = startNepaliCalendar
        _selectedEndNepaliDate.value = endNepaliCalendar

        _selectedStartEnglishDate.value = if (startNepaliCalendar != null) {
            calendarModel.convertToEnglishDate(
                startNepaliCalendar.year, startNepaliCalendar.month, startNepaliCalendar.dayOfMonth
            )
        } else {
            null
        }
        _selectedEndEnglishDate.value = if (endNepaliCalendar != null) {
            calendarModel.convertToEnglishDate(
                endNepaliCalendar.year, endNepaliCalendar.month, endNepaliCalendar.dayOfMonth
            )
        } else {
            null
        }
    }

    companion object {
        fun Saver(
            nepaliSelectableDates: NepaliSelectableDates, locale: NepaliDateLocale
        ): Saver<NepaliDateRangePickerStateImpl, Any> = listSaver(save = {
            listOf(
                it.selectedStartNepaliDate?.encodeToSimpleDateString(),
                it.selectedEndNepaliDate?.encodeToSimpleDateString(),
                it.displayedMonth.encodeToSimpleDateString(),
                it.yearRange.first,
                it.yearRange.last,
                it.displayMode.value
            )
        }, restore = { value ->
            NepaliDateRangePickerStateImpl(
                initialSelectedStartNepaliDate = if (value.isNotEmpty()) decodeSimpleDateFromString(
                    value[0] as? String
                ) else null,
                initialSelectedEndNepaliDate = if (value.isNotEmpty()) decodeSimpleDateFromString(
                    value[1] as? String
                ) else null,
                initialDisplayedMonth = if (value.size > 2) decodeSimpleDateFromString(value[2] as? String) else null,
                yearRange = IntRange(value[3] as Int, value[4] as Int),
                initialDisplayMode = DisplayMode(value[5] as Int),
                nepaliSelectableDates = nepaliSelectableDates,
                locale = locale
            )
        })
    }
}

internal fun updateNepaliDateSelection(
    nepaliCalendar: CustomCalendar,
    currentNepaliStartDate: CustomCalendar?,
    currentNepaliEndDate: CustomCalendar?,
    onDatesSelectionChange: (startNepaliDate: CustomCalendar?, endNepaliDate: CustomCalendar?) -> Unit,
    compareDates: (CustomCalendar, Int, Int, Int) -> Int
) {
    if ((currentNepaliStartDate == null && currentNepaliEndDate == null) || (currentNepaliStartDate != null && currentNepaliEndDate != null)) {
        // Set the selection to "start" only.
        onDatesSelectionChange(nepaliCalendar, null)
    } else if (currentNepaliStartDate != null && compareDates(
            nepaliCalendar,
            currentNepaliStartDate.year,
            currentNepaliStartDate.month,
            currentNepaliStartDate.dayOfMonth
        ) >= 0
    ) {
        // Set the end date.
        onDatesSelectionChange(currentNepaliStartDate, nepaliCalendar)
    } else {
        // The user selected an earlier date than the start date, so reset the start.
        onDatesSelectionChange(nepaliCalendar, null)
    }
}

internal class NepaliSelectedRangeInfo(
    val gridStartCoordinates: IntOffset,
    val gridEndCoordinates: IntOffset,
    val firstIsSelectionStart: Boolean,
    val lastIsSelectionEnd: Boolean
) {
    companion object {
        /**
         * Calculates the selection coordinates within the current month's grid. The returned [Pair]
         * holds the actual item x & y coordinates within the LazyVerticalGrid, and is later used to
         * calculate the exact offset for drawing the selection rectangles when in range-selection
         * mode.
         */
        fun calculateRangeInfo(
            nepaliMonthCalendar: NepaliMonthCalendar,
            startNepaliCalendar: CustomCalendar,
            endNepaliCalendar: CustomCalendar,
            compareDates: (CustomCalendar, Int, Int, Int) -> Int
        ): NepaliSelectedRangeInfo? {
            val comparedNepaliStartCalendarWithMonthStartDate = compareDates(
                startNepaliCalendar, nepaliMonthCalendar.year, nepaliMonthCalendar.month, 1
            )
            val comparedNepaliEndCalendarWithMonthStartDate = compareDates(
                endNepaliCalendar, nepaliMonthCalendar.year, nepaliMonthCalendar.month, 1
            )
            val comparedNepaliStartCalendarWithMonthEndDate = compareDates(
                startNepaliCalendar,
                nepaliMonthCalendar.year,
                nepaliMonthCalendar.month,
                nepaliMonthCalendar.totalDaysInMonth
            )
            val comparedNepaliEndCalendarWithMonthEndDate = compareDates(
                endNepaliCalendar,
                nepaliMonthCalendar.year,
                nepaliMonthCalendar.month,
                nepaliMonthCalendar.totalDaysInMonth
            )

            if (comparedNepaliStartCalendarWithMonthEndDate > 0 || comparedNepaliEndCalendarWithMonthStartDate < 0) {
                return null
            }

            val firstIsSelectionStart = comparedNepaliStartCalendarWithMonthStartDate >= 0
            val lastIsSelectionEnd = comparedNepaliEndCalendarWithMonthEndDate <= 0
            val startGridItemOffset = if (firstIsSelectionStart) {
                nepaliMonthCalendar.daysFromStartOfWeekToFirstOfMonth + startNepaliCalendar.dayOfMonth - 1
            } else {
                nepaliMonthCalendar.daysFromStartOfWeekToFirstOfMonth
            }
            val endGridItemOffset = if (lastIsSelectionEnd) {
                nepaliMonthCalendar.daysFromStartOfWeekToFirstOfMonth + endNepaliCalendar.dayOfMonth - 1
            } else {
                nepaliMonthCalendar.daysFromStartOfWeekToFirstOfMonth + nepaliMonthCalendar.totalDaysInMonth - 1
            }

            // Calculate the selected coordinates within the cells grid.
            val gridStartCoordinates = IntOffset(
                x = startGridItemOffset % NepaliDaysInWeek,
                y = startGridItemOffset / NepaliDaysInWeek
            )
            val gridEndCoordinates =
                IntOffset(
                    x = endGridItemOffset % NepaliDaysInWeek,
                    y = endGridItemOffset / NepaliDaysInWeek
                )
            return NepaliSelectedRangeInfo(
                gridStartCoordinates, gridEndCoordinates, firstIsSelectionStart, lastIsSelectionEnd
            )
        }
    }
}

internal fun ContentDrawScope.drawRangeBackground(
    nepaliSelectedRangeInfo: NepaliSelectedRangeInfo,
    color: Color
) {
    val itemContainerWidth = RecommendedSizeForAccessibility.toPx()
    val itemContainerHeight = RecommendedSizeForAccessibility.toPx()
    val itemStateLayerHeight = DateStateLayerHeight.toPx()
    val stateLayerVerticalPadding = (itemContainerHeight - itemStateLayerHeight) / 2
    val horizontalSpaceBetweenItems =
        (this.size.width - NepaliDaysInWeek * itemContainerWidth) / NepaliDaysInWeek

    val (x1, y1) = nepaliSelectedRangeInfo.gridStartCoordinates
    val (x2, y2) = nepaliSelectedRangeInfo.gridEndCoordinates
    // The endX and startX are offset to include only half the item's width when dealing with first
    // and last items in the selection in order to keep the selection edges rounded.
    var startX =
        x1 * (itemContainerWidth + horizontalSpaceBetweenItems) +
                (if (nepaliSelectedRangeInfo.firstIsSelectionStart) itemContainerWidth / 2 else 0f) +
                horizontalSpaceBetweenItems / 2
    val startY = y1 * itemContainerHeight + stateLayerVerticalPadding
    var endX =
        x2 * (itemContainerWidth + horizontalSpaceBetweenItems) +
                (if (nepaliSelectedRangeInfo.lastIsSelectionEnd) itemContainerWidth / 2
                else itemContainerWidth) +
                horizontalSpaceBetweenItems / 2
    val endY = y2 * itemContainerHeight + stateLayerVerticalPadding

    val isRtl = layoutDirection == LayoutDirection.Rtl
    // Adjust the start and end in case the layout is RTL.
    if (isRtl) {
        startX = this.size.width - startX
        endX = this.size.width - endX
    }

    // Draw the first row background
    drawRect(
        color = color,
        topLeft = Offset(startX, startY),
        size =
            Size(
                width =
                    when {
                        y1 == y2 -> endX - startX
                        isRtl -> -startX
                        else -> this.size.width - startX
                    },
                height = itemStateLayerHeight
            )
    )

    if (y1 != y2) {
        for (y in y2 - y1 - 1 downTo 1) {
            // Draw background behind the rows in between.
            drawRect(
                color = color,
                topLeft = Offset(0f, startY + (y * itemContainerHeight)),
                size = Size(width = this.size.width, height = itemStateLayerHeight)
            )
        }
        // Draw the last row selection background
        val topLeftX = if (layoutDirection == LayoutDirection.Ltr) 0f else this.size.width
        drawRect(
            color = color,
            topLeft = Offset(topLeftX, endY),
            size =
                Size(
                    width = if (isRtl) endX - this.size.width else endX,
                    height = itemStateLayerHeight
                )
        )
    }
}

internal val NepaliDateRangePickerTitlePadding =
    PaddingValues(start = 24.dp, end = 12.dp, top = 16.dp)
private val NepaliDateRangePickerHeadlinePadding =
    PaddingValues(start = 24.dp, end = 12.dp, bottom = 12.dp)
internal val NepaliRangeSelectionHeaderContainerHeight = 136.0.dp
internal val CalendarMonthSubheadPadding = PaddingValues(start = 24.dp, top = 20.dp, bottom = 8.dp)
internal val NepaliRangePickerHeaderHeightOffset = 60.dp
