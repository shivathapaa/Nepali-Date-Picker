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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val calendarModel = NepaliCalendarModel(state.locale)
    // Because it's expensive
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

/**
 * A composable that shows a year menu button and a couple of buttons that enable navigation between
 * displayed months.
 */
@Composable
internal fun NepaliEnglishMonthsNavigation(
    modifier: Modifier,
    nextAvailable: Boolean,
    isToday: Boolean,
    todayText: String,
    previousAvailable: Boolean,
    yearPickerVisible: Boolean,
    yearPickerText: String,
    englishMonthYearText: String,
    onNextClicked: () -> Unit,
    onPreviousClicked: () -> Unit,
    showTodayButton: Boolean,
    onTodayClicked: () -> Unit,
    onYearPickerButtonClicked: () -> Unit,
    colors: NepaliDatePickerColors
) {
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = MonthYearHeight),
        horizontalArrangement = if (yearPickerVisible) {
            Arrangement.Start
        } else {
            Arrangement.SpaceBetween
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalContentColor provides colors.navigationContentColor) {
            // A menu button for selecting a year.
            NepaliYearPickerMenuButton(
                onClick = onYearPickerButtonClicked, expanded = yearPickerVisible
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = yearPickerText,
                        modifier = Modifier,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = englishMonthYearText,
                        modifier = Modifier.alpha(0.75f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                    )
                }
            }
            // Show arrows for traversing months (only visible when the year selection is off)
            if (!yearPickerVisible) {
                Row {
                    if (showTodayButton) {
                        TextButton(onTodayClicked, enabled = !isToday) {
                            Text(text = todayText, style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    IconButton(onClick = onPreviousClicked, enabled = previousAvailable) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null
                        )
                    }

                    IconButton(onClick = onNextClicked, enabled = nextAvailable) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NepaliYearPickerMenuButton(
    onClick: () -> Unit,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        colors = ButtonDefaults.textButtonColors(contentColor = LocalContentColor.current),
        elevation = null,
        border = null,
    ) {
        content()
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = null,
            modifier = Modifier.rotate(if (expanded) 180f else 0f)
        )
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
                NepaliEnglishMonth(
                    monthCalendar = monthCalendar,
                    todayDate = today,
                    startDate = selectedDate,
                    endDate = null,
                    calendarModel = calendarModel,
                    onDateSelectionChange = onDateSelectionChange,
                    nepaliSelectableDates = nepaliSelectableDates,
                    englishDateLanguage = englishDateLanguage,
                    colors = colors
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

@Composable
internal fun NepaliEnglishMonth(
    monthCalendar: NepaliMonthCalendar,
    todayDate: SimpleDate,
    startDate: CustomCalendar?,
    endDate: CustomCalendar?,
    calendarModel: NepaliCalendarModel,
    nepaliSelectableDates: NepaliSelectableDates,
    onDateSelectionChange: (CustomCalendar) -> Unit,
    englishDateLanguage: NepaliDatePickerLang,
    colors: NepaliDatePickerColors,
    nepaliSelectedRangeInfo: NepaliSelectedRangeInfo? = null
) {
    val rangeSelectionDrawModifier =
        if (nepaliSelectedRangeInfo != null) {
            Modifier.drawWithContent {
                drawRangeBackground(
                    nepaliSelectedRangeInfo,
                    colors.dayInSelectionRangeContainerColor
                )
                drawContent()
            }
        } else {
            Modifier
        }

    var cellIndex = 0
    val daysFromStartOfWeekToFirstOfMonth = monthCalendar.daysFromStartOfWeekToFirstOfMonth

    Column(
        modifier = Modifier.requiredHeight(RecommendedSizeForAccessibility * NepaliMaxCalendarRows)
            .then(rangeSelectionDrawModifier),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        for (weekIndex in 0 until NepaliMaxCalendarRows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (dayIndex in 0 until NepaliDaysInWeek) {
                    if (cellIndex < daysFromStartOfWeekToFirstOfMonth || cellIndex >= (daysFromStartOfWeekToFirstOfMonth + monthCalendar.totalDaysInMonth)) {
                        // Empty cell
                        Spacer(
                            modifier = Modifier.requiredSize(
                                width = RecommendedSizeForAccessibility,
                                height = RecommendedSizeForAccessibility
                            )
                        )
                    } else {
                        val dayNumber = cellIndex - daysFromStartOfWeekToFirstOfMonth + 1

                        // To tackle recompositions
                        val currentMonthDate by remember(cellIndex, dayNumber) {
                            derivedStateOf {
                                calendarModel.getNepaliCalendar(
                                    SimpleDate(
                                        year = monthCalendar.year,
                                        month = monthCalendar.month,
                                        dayOfMonth = dayNumber
                                    )
                                )
                            }
                        }

                        val currentEnglishMonth by remember(currentMonthDate) {
                            derivedStateOf {
                                calendarModel.convertToEnglishDate(
                                    currentMonthDate.year,
                                    currentMonthDate.month,
                                    currentMonthDate.dayOfMonth
                                )
                            }
                        }

                        val isToday = todayDate == currentMonthDate.toSimpleDate()
                        val startDateSelected = startDate == currentMonthDate
                        val endDateSelected = endDate == currentMonthDate
                        val startingNepaliYear = NepaliDatePickerDefaults.startingNepaliCalendar
                        val endingNepaliYear = NepaliDatePickerDefaults.endNepaliCalendar

                        val inRange =
                            if (nepaliSelectedRangeInfo != null) {
                                remember(nepaliSelectedRangeInfo, currentMonthDate) {
                                    mutableStateOf(
                                        calendarModel.compareDates(
                                            currentMonthDate.toSimpleDate(),
                                            startingNepaliYear.year,
                                            startingNepaliYear.month,
                                            startingNepaliYear.dayOfMonth
                                        ) >= 0 &&
                                                calendarModel.compareDates(
                                                    currentMonthDate.toSimpleDate(),
                                                    endingNepaliYear.year,
                                                    endingNepaliYear.month,
                                                    endingNepaliYear.dayOfMonth
                                                ) <= 0
                                    )
                                }
                                    .value
                            } else {
                                false
                            }


                        NepaliEnglishDay(
                            modifier = Modifier,
                            selected = startDateSelected || endDateSelected,
                            onClick = { onDateSelectionChange(currentMonthDate) },
                            animateChecked = startDateSelected,
                            enabled = remember(currentMonthDate) {
                                // Disabled a day in case its year is not selectable, or the
                                // date itself is specifically not allowed by the state's
                                // SelectableDates.
                                with(nepaliSelectableDates) {
                                    isSelectableYear(monthCalendar.year)
                                            && isSelectableDate(currentMonthDate)
                                }
                            },
                            today = isToday,
                            inRange = inRange,
                            colors = colors
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    modifier = Modifier.align(Alignment.Center)
                                        .padding(bottom = 4.dp, end = 2.dp),
                                    textAlign = TextAlign.Center,
                                    text = calendarModel.localizeNumber(
                                        stringToLocalize = dayNumber.toString(),
                                        locale = calendarModel.locale.language
                                    ),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.5.sp)
                                )

                                Text(
                                    modifier = Modifier.align(Alignment.BottomEnd)
                                        .padding(end = 2.dp).alpha(0.75f),
                                    text = calendarModel.localizeNumber(
                                        stringToLocalize = currentEnglishMonth.dayOfMonth.toString(),
                                        locale = englishDateLanguage
                                    ),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                )
                            }
                        }
                    }
                    cellIndex++
                }
            }
        }
    }
}

@Composable
private fun NepaliEnglishDay(
    modifier: Modifier,
    selected: Boolean,
    onClick: () -> Unit,
    animateChecked: Boolean,
    enabled: Boolean,
    today: Boolean,
    inRange: Boolean,
    colors: NepaliDatePickerColors,
    content: @Composable () -> Unit
) {
    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(4.dp),
        color = colors.dayContainerColor(
            selected = selected, enabled = enabled, animate = animateChecked
        ).value,
        contentColor = colors.dayContentColor(
            isToday = today,
            selected = selected,
            inRange = inRange,
            enabled = enabled,
        ).value,
        border = if (today && !selected) {
            BorderStroke(
                DateTodayContainerOutlineWidth, colors.todayDateBorderColor
            )
        } else {
            null
        }
    ) {
        Box(
            modifier = Modifier.requiredSize(
                DateStateLayerWidth, DateStateLayerHeight
            ), contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

private val NepaliDatePickerHeadlinePadding =
    PaddingValues(start = 24.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
private val HalfDaySizeWithDivider = 25.dp