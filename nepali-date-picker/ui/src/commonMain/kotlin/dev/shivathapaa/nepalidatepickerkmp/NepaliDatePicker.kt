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
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.snapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import dev.shivathapaa.nepalidatepickerkmp.icons.NepaliIcons
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerColors
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toNepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import kotlinx.coroutines.launch
import kotlin.jvm.JvmInline
import kotlin.math.max

/**
 * Nepali Date Picker lets user select a date and preferably should be embedded into Dialogs.
 * See [NepaliDatePickerDialog].
 *
 * Nepali date picker lets you pick a Nepali date via a calendar UI which displays Nepali date..
 *
 * @param state state of the date picker. See [rememberNepaliDatePickerState].
 * @param modifier the [Modifier] to be applied to this date picker
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
 * NepaliDatePicker(state = defaultNepaliDatePickerState)
 * ```
 *
 * @see NepaliDatePickerDialog
 * @see NepaliDatePickerWithEnglishDate
 * @see NepaliDateRangePicker
 * @see NepaliDateRangePickerWithEnglishDate
 */
@Composable
fun NepaliDatePicker(
    state: NepaliDatePickerState,
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = {
        NepaliDatePickerDefaults.NepaliDatePickerTitle(
            modifier = Modifier.padding(NepaliDatePickerTitlePadding),
            language = state.locale.language,
            displayMode = state.displayMode
        )
    },
    headline: (@Composable () -> Unit)? = {
        NepaliDatePickerDefaults.NepaliDatePickerHeadline(
            selectedDate = state.selectedDate,
            modifier = Modifier.padding(NepaliDatePickerHeadlinePadding),
            locale = state.locale,
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
        colors = colors,
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
            nepaliDisplayMode = state.displayMode
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
                colors = colors,
                today = today
            )
        }
    }
}

@Composable
internal fun NepaliDateEntryContainer(
    modifier: Modifier,
    title: (@Composable () -> Unit)?,
    headline: (@Composable () -> Unit)?,
    modeToggleButton: (@Composable () -> Unit)?,
    colors: NepaliDatePickerColors,
    headerMinHeight: Dp,
    headlineTextStyle: TextStyle = MaterialTheme.typography.headlineLarge,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.sizeIn(minWidth = ContainerWidth)
            .background(colors.containerColor)
    ) {
        NepaliDatePickerHeader(
            modifier = Modifier,
            title = title,
            titleContentColor = colors.titleContentColor,
            headlineContentColor = colors.headlineContentColor,
            headerMinHeight = headerMinHeight
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                val horizontalArrangement = when {
                    headline != null && modeToggleButton != null -> Arrangement.SpaceBetween
                    headline != null -> Arrangement.Start
                    else -> Arrangement.End
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = horizontalArrangement,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (headline != null) {
                        ProvideTextStyle(value = headlineTextStyle) {
                            Box(modifier = Modifier.weight(1f)) {
                                headline()
                            }
                        }
                    }
                    modeToggleButton?.invoke()
                }
                // Display a divider only when there is a title, or headline.
                if (title != null || headline != null || modeToggleButton != null) {
                    HorizontalDivider(color = colors.dividerColor)
                }
            }
        }
        content()
    }
}

/** Represents the different modes that a date picker can be at. */
@Immutable
@JvmInline
value class DisplayMode internal constructor(internal val value: Int) {

    companion object {
        /** Date picker mode */
        val Picker = DisplayMode(0)

        /** Date text input mode */
        val Input = DisplayMode(1)
    }

    override fun toString() =
        when (this) {
            Picker -> "Picker"
            Input -> "Input"
            else -> "Unknown"
        }
}


@Composable
internal fun NepaliDisplayModeToggleButton(
    modifier: Modifier,
    displayMode: DisplayMode,
    onDisplayModeChange: (DisplayMode) -> Unit,
    language: NepaliDatePickerLang = NepaliDatePickerLang.ENGLISH
) {
    if (displayMode == DisplayMode.Picker) {
        IconButton(onClick = { onDisplayModeChange(DisplayMode.Input) }, modifier = modifier) {
            Icon(
                imageVector = NepaliIcons.Edit,
                contentDescription = language.switchToInputModeContentDescription
            )
        }
    } else {
        IconButton(onClick = { onDisplayModeChange(DisplayMode.Picker) }, modifier = modifier) {
            Icon(
                imageVector = NepaliIcons.DateRange,
                contentDescription = language.switchToCalendarModeContentDescription
            )
        }
    }
}

@Composable
internal fun SwitchableNepaliDateEntryContent(
    selectedDate: CustomCalendar?,
    onDateSelectionChange: (CustomCalendar?) -> Unit,
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
                NepaliDateInputContent(
                    selectedDate = selectedDate,
                    onDateSelectionChange = { onDateSelectionChange(it) },
                    calendarModel = calendarModel,
                    yearRange = yearRange,
                    nepaliSelectableDates = nepaliSelectableDates,
                    language = language,
                    colors = colors
                )
        }
    }
}

@Composable
private fun NepaliDatePickerHeader(
    modifier: Modifier,
    title: (@Composable () -> Unit)?,
    titleContentColor: Color,
    headlineContentColor: Color,
    headerMinHeight: Dp,
    content: @Composable () -> Unit
) {
    // Apply a defaultMinSize only when the title is not null.
    val heightModifier = if (title != null) {
        Modifier.defaultMinSize(minHeight = headerMinHeight)
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
    selectedDate: CustomCalendar?,
    displayedMonth: NepaliMonthCalendar,
    onDateSelectionChange: (CustomCalendar) -> Unit,
    onDisplayedMonthChange: (NepaliMonthCalendar) -> Unit,
    calendarModel: NepaliCalendarModel,
    yearRange: IntRange,
    nepaliSelectableDates: NepaliSelectableDates,
    showTodayButton: Boolean,
    colors: NepaliDatePickerColors,
    today: SimpleDate
) {
    // Plain remember: these are pure functions of their keys, not derived reads of other
    // snapshot state, so derivedStateOf would only add a redundant observer allocation.
    val displayedMonthIndex = remember(displayedMonth, yearRange) {
        displayedMonth.indexIn(yearRange)
    }
    val initialIndex = today.indexIn(yearRange)

    val isToday = displayedMonthIndex == initialIndex

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
            colors = colors,
            previousMonthContentDescription = chosenLanguage.previousMonthContentDescription,
            nextMonthContentDescription = chosenLanguage.nextMonthContentDescription
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

@Stable
interface NepaliDatePickerState {
    var selectedDate: CustomCalendar?
    var displayedMonth: NepaliMonthCalendar
    val selectedEnglishDate: CustomCalendar?
    var displayMode: DisplayMode
    val yearRange: IntRange
    val nepaliSelectableDates: NepaliSelectableDates
    val locale: NepaliDateLocale
}

/**
 * Creates a [NepaliDatePickerState] for a [NepaliDatePicker] that is remembered across compositions.
 *
 * To create a date picker state outside composition, see the `NepaliDatePickerState` function.
 *
 * @param initialSelectedDate [SimpleDate] that represents an initial selection of a date.
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
 * Example usage:
 * ```
 * val defaultNepaliDatePickerState = rememberNepaliDatePickerState()
 * val customizedDatePickerState =
 *     rememberNepaliDatePickerState(
 *         locale = NepaliDateLocale(language = NepaliDatePickerLang.NEPALI),
 *         nepaliSelectableDates = object : NepaliSelectableDates {
 *             override fun isSelectableDate(customCalendar: CustomCalendar)
 *                     : Boolean {
 *                 return customCalendar.dayOfWeek != 7
 *                         || customCalendar.dayOfMonth != 12
 *             }
 *
 *             override fun isSelectableYear(year: Int): Boolean {
 *                 return (year % 5 != 0)
 *             }
 *         }
 *     )
 *
 * // Or you can utilize helper function (BeforeDateSelectable or AfterDateSelectable or DateRangeSelectable) to disable and enable dates
 *
 * val datePickerStateWithDateLimiter =
 *     rememberNepaliDatePickerState(
 *         nepaliSelectableDates = NepaliDatePickerDefaults.BeforeDate(
 *              SimpleDate(2081, 3, 21)
 *         )
 *     )
 *
 * // For Range, minDate and maxDate should make sense i.e., minDate should be less than or equal to maxDate
 * val nepaliDatePickerStateWithRangeSelectable = rememberNepaliDatePickerState(
 *     nepaliSelectableDates = DateRangeSelectableDates(
 *         SimpleDate(2081, 2, 11),
 *         SimpleDate(2082, 1, 29)
 *     )
 * )
 *
 * NepaliDatePicker(state = defaultNepaliDatePickerState)
 *
 * NepaliDatePicker(
 *     state = customizedDatePickerState,
 *     colors = NepaliDatePickerDefaults.colors().copy(
 *         containerColor = MaterialTheme.colorScheme.surface
 *     )
 * )
 *
 * NepaliDatePicker(state = datePickerStateWithDateLimiter)
 *
 * NepaliDatePicker(state = nepaliDatePickerStateWithRangeSelectable)
 *
 * ```
 */
@Composable
fun rememberNepaliDatePickerState(
    initialSelectedDate: SimpleDate? = null,
    initialDisplayedMonth: SimpleDate? = initialSelectedDate,
    yearRange: IntRange = NepaliCalendarDefaults.NepaliYearRange,
    initialDisplayMode: DisplayMode = DisplayMode.Picker,
    nepaliSelectableDates: NepaliSelectableDates = NepaliDatePickerDefaults.AllDates,
    locale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultLocale,
): NepaliDatePickerState {
    return rememberSaveable(
        saver = NepaliDatePickerStateImpl.Saver(nepaliSelectableDates, locale)
    ) {
        NepaliDatePickerStateImpl(
            initialSelectedDate = initialSelectedDate,
            initialDisplayedMonth = initialDisplayedMonth,
            yearRange = yearRange,
            initialDisplayMode = initialDisplayMode,
            nepaliSelectableDates = nepaliSelectableDates,
            locale = locale
        )
    }
}

/**
 * Creates [NepaliDatePickerState]. Primarily recommended for outside composition.
 *
 * You are recommended to use [rememberNepaliDatePickerState] when inside a composition.
 *
 * @param initialSelectedDate [SimpleDate] that represents an initial selection of a date.
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
fun NepaliDatePickerState(
    initialSelectedDate: SimpleDate? = null,
    initialDisplayedMonth: SimpleDate? = initialSelectedDate,
    yearRange: IntRange = NepaliCalendarDefaults.NepaliYearRange,
    initialDisplayMode: DisplayMode = DisplayMode.Picker,
    nepaliSelectableDates: NepaliSelectableDates = NepaliDatePickerDefaults.AllDates,
    locale: NepaliDateLocale
): NepaliDatePickerState = NepaliDatePickerStateImpl(
    initialSelectedDate = initialSelectedDate,
    initialDisplayedMonth = initialDisplayedMonth,
    yearRange = yearRange,
    initialDisplayMode = initialDisplayMode,
    nepaliSelectableDates = nepaliSelectableDates,
    locale = locale
)

/**
 * An abstract for the date pickers states.
 *
 * This base class common state properties and provides a base implementation that is extended by
 * the different state classes.
 *
 * @param initialDisplayedMonth SimpleDate represents an initial selection of a month to be
 * displayed to the user. In case `null` is provided, the displayed month would be the current one.
 * @param yearRange an [IntRange] that holds the year range that the date picker will be limited to
 * @param nepaliSelectableDates a [NepaliSelectableDates] that is consulted to check if a date is allowed.
 * In case a date is not allowed to be selected, it will appear disabled in the UI.
 * @see rememberNepaliDatePickerState
 * @throws [IllegalArgumentException] if the initial selected date or displayed month represent
 * a year that is out of the year range.
 */
@Stable
internal abstract class BaseNepaliDatePickerStateImpl(
    initialDisplayedMonth: SimpleDate?,
    val yearRange: IntRange,
    val nepaliSelectableDates: NepaliSelectableDates,
    val locale: NepaliDateLocale,
) {

    protected val calendarModel = NepaliCalendarModel(locale)

    private var _displayedMonth = mutableStateOf(
        if (initialDisplayedMonth != null) {
            val month = calendarModel.getNepaliMonth(
                nepaliYear = initialDisplayedMonth.year, nepaliMonth = initialDisplayedMonth.month
            )
            require(yearRange.contains(month.year)) {
                "The initial display month's year (${month.year}) is out of the years range of $yearRange."
            }
            month
        } else {
            // Set the displayed month to the current one.
            calendarModel.todayNepaliCalendar.toNepaliMonthCalendar()
        })

    var displayedMonth: NepaliMonthCalendar
        get() = _displayedMonth.value
        set(month) {
            require(yearRange.contains(month.year)) {
                "The display month's year (${month.year}) is out of the years range of $yearRange."
            }
            _displayedMonth.value = month
        }
}

/**
 * A default implementation of the [NepaliDatePickerState]. See [rememberNepaliDatePickerState].
 *
 * @see rememberNepaliDatePickerState
 * @throws [IllegalArgumentException] if the initial selected date or displayed month represent
 * a year that is out of the year range.
 */
@Stable
private class NepaliDatePickerStateImpl(
    initialSelectedDate: SimpleDate?,
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
), NepaliDatePickerState {

    /**
     * A mutable state of [CustomCalendar] that represents a selected date.
     */
    private var _selectedDate = mutableStateOf(
        if (initialSelectedDate != null) {
            val date = calendarModel.getNepaliCalendar(simpleNepaliDate = initialSelectedDate)
            require(yearRange.contains(date.year)) {
                "The provided initial date's year (${date.year}) is out of the years range of $yearRange."
            }
            date
        } else {
            null
        })

    override var selectedDate: CustomCalendar?
        get() = _selectedDate.value
        set(customCalendar) {
            if (customCalendar != null) {
                // Validate that the give date is within the valid years range.
                require(yearRange.contains(customCalendar.year)) {
                    "The provided date's year (${customCalendar.year}) is out of the years range of $yearRange."
                }
                _selectedDate.value = customCalendar
            } else {
                _selectedDate.value = null
            }
        }

    override val selectedEnglishDate: CustomCalendar?
        get() = if (selectedDate != null) {
            calendarModel.convertToEnglishDate(
                nepaliYYYY = selectedDate!!.year,
                nepaliMM = selectedDate!!.month,
                nepaliDD = selectedDate!!.dayOfMonth
            )
        } else null

    private var _displayMode = mutableStateOf(initialDisplayMode)

    override var displayMode: DisplayMode
        get() = _displayMode.value
        set(displayMode) {
            selectedDate?.let {
                displayedMonth = it.toNepaliMonthCalendar()
            }
            _displayMode.value = displayMode
        }

    companion object {
        fun Saver(
            nepaliSelectableDates: NepaliSelectableDates, locale: NepaliDateLocale
        ): Saver<NepaliDatePickerStateImpl, Any> = listSaver(save = { state ->
            listOf(
                state.selectedDate?.encodeToSimpleDateString(),
                state.displayedMonth.encodeToSimpleDateString(),
                state.yearRange.first,
                state.yearRange.last,
                state.displayMode.value
            )
        }, restore = { value ->
            NepaliDatePickerStateImpl(
                initialSelectedDate = if (value.isNotEmpty()) decodeSimpleDateFromString(value[0] as? String) else null,
                initialDisplayedMonth = if (value.size > 1) decodeSimpleDateFromString(value[1] as? String) else null,
                yearRange = IntRange(value[2] as Int, value[3] as Int),
                initialDisplayMode = DisplayMode(value[4] as Int),
                nepaliSelectableDates = nepaliSelectableDates,
                locale = locale
            )
        })
    }
}

/**
 * A composable that shows a year menu button and a couple of buttons that enable navigation between
 * displayed months.
 */
@Composable
internal fun NepaliMonthsNavigation(
    modifier: Modifier,
    nextAvailable: Boolean,
    isToday: Boolean,
    todayText: String,
    previousAvailable: Boolean,
    yearPickerVisible: Boolean,
    yearPickerText: String,
    onNextClicked: () -> Unit,
    onPreviousClicked: () -> Unit,
    showTodayButton: Boolean,
    onTodayClicked: () -> Unit,
    onYearPickerButtonClicked: () -> Unit,
    colors: NepaliDatePickerColors,
    previousMonthContentDescription: String? = null,
    nextMonthContentDescription: String? = null
) {
    Row(
        modifier = modifier.fillMaxWidth().requiredHeight(MonthYearHeight),
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
                Text(
                    text = yearPickerText,
                    modifier = Modifier,
                    style = MaterialTheme.typography.bodyLarge
                )
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
                            NepaliIcons.KeyboardArrowLeft,
                            contentDescription = previousMonthContentDescription
                        )
                    }

                    IconButton(onClick = onNextClicked, enabled = nextAvailable) {
                        Icon(
                            NepaliIcons.KeyboardArrowRight,
                            contentDescription = nextMonthContentDescription
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
            imageVector = NepaliIcons.ArrowDropDown,
            contentDescription = null,
            modifier = Modifier.rotate(if (expanded) 180f else 0f)
        )
    }
}

/**
 * Composes the weekdays letters.
 */
@Composable
internal fun NepaliWeekDays(
    colors: NepaliDatePickerColors, language: NepaliDatePickerLang, weekDayFormat: NameFormat
) {
    val firstDayOfWeek = NepaliDatePickerDefaults.FIRST_DAY_OF_WEEK
    val weekdays = language.weekdays

    // Pair each abbreviated label with its full weekday name so screen readers announce
    // "Monday" instead of the single letter "M".
    val dayNames = (firstDayOfWeek..firstDayOfWeek + 6).map { dayIndex ->
        val weekday = weekdays[dayIndex - 1]
        val display = if (weekDayFormat == NameFormat.SHORT) weekday.short else weekday.medium
        display to weekday.full
    }

    val textStyle = MaterialTheme.typography.bodyLarge

    Row(
        modifier = Modifier.defaultMinSize(
            minHeight = RecommendedSizeForAccessibility
        ).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        dayNames.fastForEach { (display, fullName) ->
            Box(
                modifier = Modifier.size(
                    width = RecommendedSizeForAccessibility,
                    height = RecommendedSizeForAccessibility
                ).clearAndSetSemantics { contentDescription = fullName },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = display,
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
    selectedDate: CustomCalendar?,
    calendarModel: NepaliCalendarModel,
    nepaliSelectableDates: NepaliSelectableDates,
    colors: NepaliDatePickerColors,
) {
    val firstMonth = remember(yearRange, calendarModel) {
        calendarModel.getNepaliMonth(nepaliYear = yearRange.first, nepaliMonth = 1)
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
                    calendarModel = calendarModel,
                    onDateSelectionChange = onDateSelectionChange,
                    nepaliSelectableDates = nepaliSelectableDates,
                    colors = colors,
                    endDate = null
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
internal fun numberOfMonthsInRange(yearRange: IntRange) =
    (yearRange.last - yearRange.first + 1) * 12

internal suspend fun updateDisplayedMonth(
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
internal fun NepaliMonth(
    monthCalendar: NepaliMonthCalendar,
    todayDate: SimpleDate,
    startDate: CustomCalendar?,
    endDate: CustomCalendar?,
    calendarModel: NepaliCalendarModel,
    nepaliSelectableDates: NepaliSelectableDates,
    onDateSelectionChange: (CustomCalendar) -> Unit,
    colors: NepaliDatePickerColors,
    nepaliSelectedRangeInfo: NepaliSelectedRangeInfo? = null,
    dayShape: Shape = CircleShape,
    // When non-null, each cell also renders the corresponding English day-of-month (dual-date cell).
    englishDateLanguage: NepaliDatePickerLang? = null
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

    // Hoisted out of the 42-cell loop (were re-read on every cell).
    val startingNepaliYear = NepaliCalendarDefaults.startingNepaliCalendar
    val endingNepaliYear = NepaliCalendarDefaults.endNepaliCalendar
    // FULL-format locale used only to build each cell's screen-reader description.
    val a11yLocale = remember(calendarModel) {
        calendarModel.locale.copy(dateFormat = NepaliDateFormatStyle.FULL)
    }

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

                        // Keyed on the actual month + day so a LazyRow slot reused for a
                        // different month can't serve a stale date (the old key was cellIndex +
                        // dayNumber only). Plain remember — the value is a pure function of the key.
                        val currentMonthDate = remember(
                            monthCalendar.year, monthCalendar.month, dayNumber
                        ) {
                            calendarModel.getNepaliCalendar(
                                SimpleDate(
                                    year = monthCalendar.year,
                                    month = monthCalendar.month,
                                    dayOfMonth = dayNumber
                                )
                            )
                        }

                        val isToday = todayDate == currentMonthDate.toSimpleDate()
                        val startDateSelected = startDate == currentMonthDate
                        val endDateSelected = endDate == currentMonthDate

                        // Plain computed remember (was a per-cell MutableState allocation).
                        val inRange =
                            if (nepaliSelectedRangeInfo != null) {
                                remember(nepaliSelectedRangeInfo, currentMonthDate) {
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
                                }
                            } else {
                                false
                            }

                        // Full localized date (+ "today") read by screen readers for this cell.
                        val dayContentDescription = remember(currentMonthDate, isToday) {
                            buildString {
                                append(calendarModel.formatNepaliDate(currentMonthDate, a11yLocale))
                                if (isToday) {
                                    append(", ")
                                    append(calendarModel.locale.language.today)
                                }
                            }
                        }

                        // Only computed for the dual English+Nepali cell variant.
                        val currentEnglishDay: Int? = if (englishDateLanguage != null) {
                            remember(currentMonthDate) {
                                calendarModel.convertToEnglishDate(
                                    currentMonthDate.year,
                                    currentMonthDate.month,
                                    currentMonthDate.dayOfMonth
                                ).dayOfMonth
                            }
                        } else {
                            null
                        }

                        NepaliDay(
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
                            colors = colors,
                            inRange = inRange,
                            dateContentDescription = dayContentDescription,
                            shape = dayShape
                        ) {
                            if (englishDateLanguage != null && currentEnglishDay != null) {
                                // Dual-date cell: large Nepali number, small English day-of-month.
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
                                            stringToLocalize = currentEnglishDay.toString(),
                                            locale = englishDateLanguage
                                        ),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp)
                                    )
                                }
                            } else {
                                Text(
                                    modifier = Modifier,
                                    textAlign = TextAlign.Center,
                                    text = calendarModel.localizeNumber(
                                        stringToLocalize = dayNumber.toString(),
                                        locale = calendarModel.locale.language
                                    ),
                                    style = MaterialTheme.typography.bodyLarge
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
    inRange: Boolean = false,
    dateContentDescription: String? = null,
    shape: Shape = CircleShape,
    content: @Composable () -> Unit
) {
    Surface(
        selected = selected,
        onClick = onClick,
        // Surface already contributes selected/disabled state to semantics; the description adds
        // the full localized date so a screen reader announces the whole cell, not just the number.
        modifier = if (dateContentDescription != null) {
            modifier.semantics { contentDescription = dateContentDescription }
        } else {
            modifier
        },
        enabled = enabled,
        shape = shape,
        color = colors.dayContainerColor(
            selected = selected, enabled = enabled, animate = animateChecked
        ).value,
        contentColor = colors.dayContentColor(
            isToday = today,
            selected = selected,
            inRange = inRange,
            enabled = enabled
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
internal fun NepaliYearPicker(
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

    LazyVerticalGrid(
        contentPadding = PaddingValues(bottom = YearPickerContentBottomPadding),
        columns = GridCells.Fixed(NepaliYearsInRow),
        modifier = modifier.background(colors.containerColor),
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

@Composable
internal fun ProvideContentColorTextStyle(
    contentColor: Color, textStyle: TextStyle, content: @Composable () -> Unit
) {
    val mergedStyle = LocalTextStyle.current.merge(textStyle)
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        LocalTextStyle provides mergedStyle,
        content = content
    )
}

/**
 * Creates and remembers a [FlingBehavior] that will represent natural fling curve with snap to
 * the most visible month in the months list.
 *
 * @param lazyListState a [LazyListState]
 * @param decayAnimationSpec the decay to use
 */
@Composable
internal fun rememberCustomSnapFlingBehavior(
    lazyListState: LazyListState,
    decayAnimationSpec: DecayAnimationSpec<Float> = exponentialDecay()
): FlingBehavior {
    return remember(decayAnimationSpec, lazyListState) {
        val original = SnapLayoutInfoProvider(lazyListState)
        val snapLayoutInfoProvider =
            object : SnapLayoutInfoProvider by original {
                override fun calculateApproachOffset(
                    velocity: Float,
                    decayOffset: Float
                ): Float = 0.0f
            }

        snapFlingBehavior(
            snapLayoutInfoProvider = snapLayoutInfoProvider,
            decayAnimationSpec = decayAnimationSpec,
            snapAnimationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        )
    }
}


/**
 * Encode the [CustomCalendar] and [NepaliMonthCalendar] in [SimpleDate] format.
 */
internal fun CustomCalendar.encodeToSimpleDateString(): String {
    return "$year,$month,$dayOfMonth"
}

internal fun NepaliMonthCalendar.encodeToSimpleDateString(): String {
    return "$year,$month,1" // Defaulting to the first day of the month
}

/**
 * Decodes a comma-separated string into a [SimpleDate] object.
 *
 * The input string should contain exactly three values: year, month, and day of the month.
 * If the string is invalid or cannot be parsed into integers, this function returns null.
 *
 * @param dateString The comma-separated string to decode, e.g., "2081,3,21".
 * @return A [SimpleDate] object if the string is valid and contains three integer values;
 *         otherwise, returns null.
 */
internal fun decodeSimpleDateFromString(dateString: String?): SimpleDate? {
    return dateString?.split(",")?.takeIf { it.size == 3 }?.let {
        try {
            SimpleDate(it[0].toInt(), it[1].toInt(), it[2].toInt())
        } catch (e: NumberFormatException) {
            null
        }
    }
}

internal const val NepaliDaysInWeek: Int = 7

internal const val NepaliMaxCalendarRows = 6
internal const val NepaliYearsInRow: Int = 3

internal val DateStateLayerWidth = 40.0.dp
internal val DateStateLayerHeight = 40.0.dp
internal val SelectionYearContainerWidth = 72.0.dp
internal val SelectionYearContainerHeight = 36.0.dp
internal val ContainerWidth = 360.0.dp
internal val ContainerHeight = 568.0.dp
internal val RecommendedSizeForAccessibility = 48.dp
internal val MonthYearHeight = 56.dp
internal val YearsVerticalPadding = 16.dp
internal val HeaderContainerHeight = 120.0.dp
internal val DateTodayContainerOutlineWidth = 1.0.dp

internal val DatePickerHorizontalPadding = 12.dp
internal val YearPickerContentBottomPadding = 8.dp
internal val NepaliDatePickerTitlePadding = PaddingValues(start = 24.dp, end = 12.dp, top = 16.dp)
private val NepaliDatePickerHeadlinePadding =
    PaddingValues(start = 24.dp, end = 12.dp, bottom = 12.dp)
internal val NepaliDatePickerModeTogglePadding = PaddingValues(end = 12.dp, bottom = 12.dp)