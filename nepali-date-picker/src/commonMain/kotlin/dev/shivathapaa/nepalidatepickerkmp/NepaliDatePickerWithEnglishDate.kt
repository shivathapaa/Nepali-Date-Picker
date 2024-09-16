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
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
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
import kotlin.math.max

/**
 * Nepali Date Picker lets user select a date and preferably should be embedded into Dialogs.
 * See [NepaliDatePickerDialogForWithEnglish].
 *
 * Nepali date picker lets you pick a date via a calendar UI.
 *
 * @param state state of the date picker. See [rememberNepaliDatePickerState].
 * @param modifier the [Modifier] to be applied to this date picker
 * @param englishDateLocale the locale for the english date
 * @param title the title to be displayed in the date picker
 * @param headline the headline to be displayed in the date picker
 * @param colors [NepaliDatePickerColors] that will be used to resolve the colors used for this date
 * picker in different states.
 * See [NepaliDatePickerDefaults.colors].
 *
 * Example usage:
 * ```
 * val defaultNepaliDatePickerState = rememberNepaliDatePickerState()
 *
 * NepaliDatePicker(state = defaultNepaliDatePickerState)
 * ```
 */
@ExperimentalNepaliDatePickerApi
@Composable
fun NepaliDatePickerWithEnglishDate(
    state: NepaliDatePickerState,
    modifier: Modifier = Modifier,
    englishDateLocale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultLocale,
    title: (@Composable () -> Unit)? = {
        NepaliDatePickerDefaults.NepaliDatePickerTitle(
            modifier = Modifier.padding(NepaliDatePickerTitlePadding),
            language = state.locale.language
        )
    },
    headline: (@Composable () -> Unit)? = {
        NepaliDatePickerDefaults.NepaliDatePickerHeadlineWithEnglishDate(
            modifier = Modifier.padding(NepaliDatePickerHeadlinePadding),
            selectedDate = state.selectedDate,
            selectedEnglishDate = state.selectedEnglishDate,
            locale = state.locale,
            englishLocale = englishDateLocale
        )
    },
    showTodayButton: Boolean = true,
    colors: NepaliDatePickerColors = NepaliDatePickerDefaults.colors()
) {
    val calendarModel = NepaliCalendarModel(state.locale)
    // Because it's expensive
    val today = calendarModel.today.toSimpleDate()

    NepaliDateEntryContainer(
        modifier = modifier, title = title, headline = headline, colors = colors
    ) {
        NepaliDatePicker(
            selectedDate = state.selectedDate?.toSimpleDate(),
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

@Composable
private fun NepaliDateEntryContainer(
    modifier: Modifier,
    title: (@Composable () -> Unit)?,
    headline: (@Composable () -> Unit)?,
    colors: NepaliDatePickerColors,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.sizeIn(minWidth = ContainerWidth)
    ) {
        NepaliDatePickerHeader(
            modifier = Modifier,
            title = title,
            titleContentColor = colors.titleContentColor,
            headlineContentColor = colors.headlineContentColor
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                val horizontalArrangement = when {
                    headline != null -> Arrangement.Start
                    else -> Arrangement.End
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = horizontalArrangement,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (headline != null) {
                        ProvideTextStyle(value = MaterialTheme.typography.headlineLarge) {
                            Box(modifier = Modifier.weight(1f)) {
                                headline()
                            }
                        }
                    }
                }
                // Display a divider only when there is a title, or headline.
                if (title != null || headline != null) {
                    HorizontalDivider(color = colors.dividerColor)
                }
            }
        }
        content()
    }
}

@Composable
private fun NepaliDatePickerHeader(
    modifier: Modifier,
    title: (@Composable () -> Unit)?,
    titleContentColor: Color,
    headlineContentColor: Color,
    content: @Composable () -> Unit
) {
    // Apply a defaultMinSize only when the title is not null.
    val heightModifier = if (title != null) {
        Modifier.defaultMinSize(minHeight = HeaderContainerHeight)
    } else {
        Modifier
    }
    Column(
        modifier = modifier.fillMaxWidth().then(heightModifier),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (title != null) {
            ProvideContentColorTextStyle(
                contentColor = titleContentColor, textStyle = MaterialTheme.typography.labelLarge
            ) {
                Box(contentAlignment = Alignment.BottomStart) {
                    title()
                }
            }
        }
        CompositionLocalProvider(
            LocalContentColor provides headlineContentColor, content = content
        )
    }
}

@Composable
private fun NepaliDatePicker(
    selectedDate: SimpleDate?,
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

    val currentMonth = englishCalendar.month
    val currentMonthName =
        calendarModel.getEnglishMonthName(currentMonth, englishDateLanguage, NameFormat.SHORT)
    val fullEnglishYear = calendarModel.localizeNumber(
        stringToLocalize = englishCalendar.year.toString(), locale = englishDateLanguage
    )

    val nextMonth = currentMonth + 1
    // Adjust to ensure the month is within the 1-12 range
    val adjustedNextMonth = if (nextMonth > 12) nextMonth - 12 else nextMonth
    val nextMonthName =
        calendarModel.getEnglishMonthName(adjustedNextMonth, englishDateLanguage, NameFormat.SHORT)

    val formattedEnglishMonthYear = "$currentMonthName/$nextMonthName $fullEnglishYear"

    Column {
        NepaliMonthsNavigation(
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
                            RecommendedSizeForAccessibility * (NepaliMaxCalendarRows + 1) - 1.dp
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
private fun NepaliMonthsNavigation(
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

/**
 * Composes the weekdays letters.
 */
@Composable
private fun NepaliWeekDays(
    colors: NepaliDatePickerColors, language: NepaliDatePickerLang, weekDayFormat: NameFormat
) {
    val firstDayOfWeek = NepaliDatePickerDefaults.FIRST_DAY_OF_WEEK
    val weekdays = language.weekdays

    val dayNames = (firstDayOfWeek..firstDayOfWeek + 6).map { dayIndex ->
        if (weekDayFormat == NameFormat.SHORT) weekdays[dayIndex - 1].short else weekdays[dayIndex - 1].medium
    }

    val textStyle = MaterialTheme.typography.bodyLarge

    Row(
        modifier = Modifier.defaultMinSize(
            minHeight = RecommendedSizeForAccessibility
        ).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        dayNames.fastForEach {
            Box(
                modifier = Modifier.size(
                    width = RecommendedSizeForAccessibility,
                    height = RecommendedSizeForAccessibility
                ), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = it,
                    modifier = Modifier.wrapContentSize(),
                    color = colors.weekdayContentColor,
                    style = textStyle,
                    textAlign = TextAlign.Center
                )
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
    selectedDate: SimpleDate?,
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
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState)

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
                    chosenLanguage = calendarModel.locale.language,
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

/**
 * Returns the number of months within the given year range.
 */
private fun numberOfMonthsInRange(yearRange: IntRange) = (yearRange.last - yearRange.first + 1) * 12

private suspend fun updateDisplayedMonth(
    lazyListState: LazyListState,
    calendarModel: NepaliCalendarModel,
    onDisplayedMonthChange: (NepaliMonthCalendar) -> Unit,
    yearRange: IntRange
) {
    snapshotFlow { lazyListState.firstVisibleItemIndex }.collect {
        val yearOffset = lazyListState.firstVisibleItemIndex / 12
        val month = lazyListState.firstVisibleItemIndex % 12 + 1
        onDisplayedMonthChange(
            calendarModel.getNepaliMonth(
                nepaliYear = yearRange.first + yearOffset, nepaliMonth = month
            )
        )
    }
}

@Composable
private fun NepaliMonth(
    monthCalendar: NepaliMonthCalendar,
    todayDate: SimpleDate,
    startDate: SimpleDate?,
    calendarModel: NepaliCalendarModel,
    chosenLanguage: NepaliDatePickerLang,
    nepaliSelectableDates: NepaliSelectableDates,
    onDateSelectionChange: (CustomCalendar) -> Unit,
    englishDateLanguage: NepaliDatePickerLang,
    colors: NepaliDatePickerColors
) {
    var cellIndex = 0
    val daysFromStartOfWeekToFirstOfMonth = monthCalendar.daysFromStartOfWeekToFirstOfMonth

    Column(
        modifier = Modifier.requiredHeight(RecommendedSizeForAccessibility * NepaliMaxCalendarRows),
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
                        val startDateSelected = startDate == currentMonthDate.toSimpleDate()

                        NepaliDay(
                            modifier = Modifier,
                            selected = startDateSelected,
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
                            colors = colors
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    modifier = Modifier.align(Alignment.Center)
                                        .padding(bottom = 4.dp, end = 2.dp),
                                    textAlign = TextAlign.Center,
                                    text = calendarModel.localizeNumber(
                                        stringToLocalize = dayNumber.toString(),
                                        locale = chosenLanguage
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
private fun NepaliDay(
    modifier: Modifier,
    selected: Boolean,
    onClick: () -> Unit,
    animateChecked: Boolean,
    enabled: Boolean,
    today: Boolean,
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
            inRange = false, // Todo: Implement range selection later
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

@Composable
private fun NepaliYearPicker(
    modifier: Modifier,
    currentYear: Int,
    displayedYear: Int,
    onYearSelected: (year: Int) -> Unit,
    nepaliSelectableDates: NepaliSelectableDates,
    calendarModel: NepaliCalendarModel,
    yearRange: IntRange,
    colors: NepaliDatePickerColors
) {
    val lazyGridState = rememberLazyGridState(
        // Set the initial index to a few years before the current year to allow quicker
        // selection of previous years.
        initialFirstVisibleItemIndex = max(
            0, displayedYear - yearRange.first - NepaliYearsInRow
        )
    )
    // Match the years container color to any elevated surface color that is composed under it.
    val surfaceColor = MaterialTheme.colorScheme.copy(surface = colors.containerColor)
    val containerColor = surfaceColor.surfaceColorAtElevation(LocalAbsoluteTonalElevation.current)

    LazyVerticalGrid(
        contentPadding = PaddingValues(bottom = YearPickerContentBottomPadding),
        columns = GridCells.Fixed(NepaliYearsInRow),
        modifier = modifier.background(containerColor),
        state = lazyGridState,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalArrangement = Arrangement.spacedBy(YearsVerticalPadding)
    ) {
        items(count = yearRange.count(), key = { index: Int -> index }) { index ->
            val selectedYear = index + yearRange.first
            val localizedYear = calendarModel.localizeNumber(
                stringToLocalize = selectedYear.toString(), locale = calendarModel.locale.language
            )
            NepaliYear(
                modifier = Modifier.requiredSize(
                    width = SelectionYearContainerWidth, height = SelectionYearContainerHeight
                ),
                selected = selectedYear == displayedYear,
                currentYear = selectedYear == currentYear,
                onClick = { onYearSelected(selectedYear) },
                enabled = nepaliSelectableDates.isSelectableYear(selectedYear),
                colors = colors
            ) {
                Text(
                    text = localizedYear,
                    modifier = Modifier,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun NepaliYear(
    modifier: Modifier,
    selected: Boolean,
    currentYear: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    colors: NepaliDatePickerColors,
    content: @Composable () -> Unit
) {
    val border = remember(currentYear, selected) {
        if (currentYear && !selected) {
            // Use the day's spec to draw a border around the current year.
            BorderStroke(
                DateTodayContainerOutlineWidth, colors.todayDateBorderColor
            )
        } else {
            null
        }
    }
    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = CircleShape,
        color = colors.yearContainerColor(selected = selected, enabled = enabled).value,
        contentColor = colors.yearContentColor(
            currentYear = currentYear, selected = selected, enabled = enabled
        ).value,
        border = border,
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            content()
        }
    }
}

private const val NepaliDaysInWeek: Int = 7

private const val NepaliMaxCalendarRows = 6
private const val NepaliYearsInRow: Int = 3

private val DateStateLayerWidth = 40.0.dp
private val DateStateLayerHeight = 40.0.dp
private val SelectionYearContainerWidth = 72.0.dp
private val SelectionYearContainerHeight = 36.0.dp
private val RecommendedSizeForAccessibility = 48.dp
private val MonthYearHeight = 56.dp
private val YearsVerticalPadding = 16.dp
private val HeaderContainerHeight = 120.0.dp
private val DateTodayContainerOutlineWidth = 1.0.dp

private val DatePickerHorizontalPadding = 12.dp
private val YearPickerContentBottomPadding = 8.dp
private val NepaliDatePickerTitlePadding = PaddingValues(start = 24.dp, end = 12.dp, top = 16.dp)
private val NepaliDatePickerHeadlinePadding =
    PaddingValues(start = 24.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)