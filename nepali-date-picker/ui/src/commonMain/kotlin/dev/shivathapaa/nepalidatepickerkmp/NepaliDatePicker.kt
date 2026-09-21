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
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.snap
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
import androidx.compose.runtime.key
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
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.CalendarViewAdapter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.DayIndicatorPadding
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.MaxDayIndicators
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.MaxDualDateDayIndicators
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerColors
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayDecoration
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayDecorator
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayIndicators
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayInfo
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.calendarViewAdapter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.resolveDayVisuals
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.formatSecondary
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.isInSelectedRange
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.monthGrid
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.rememberCalendarViewAdapter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.secondaryMonthLabel
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.MonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.toNepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.jvm.JvmInline
import kotlin.math.max

/**
 * Nepali Date Picker lets user select a date and preferably should be embedded into Dialogs.
 * See [NepaliDatePickerDialog].
 *
 * Nepali date picker lets you pick a Nepali date via a calendar UI which displays Nepali date..
 *
 * The grid can show either calendar. Pass a [secondaryDateLocale] to pair every day with its
 * counterpart in the other calendar, and set [showCalendarSystemToggle] to let the user switch which
 * calendar leads. Switching never changes the selection: a selected date is always held in Bikram
 * Sambat, so the same day stays picked in both views. Both are off by default, so a picker that does
 * not ask for them looks and behaves exactly as before.
 *
 * @param state state of the date picker. See [rememberNepaliDatePickerState].
 * @param modifier the [Modifier] to be applied to this date picker
 * @param title the title to be displayed in the date picker
 * @param headline the headline to be displayed in the date picker
 * @param secondaryDateLocale when non-null, every day cell also shows the same day in the other
 * calendar, using this [NepaliDateLocale] for its language and digits. `null` shows one calendar only.
 * @param showModeToggle the boolean to let user toggle between Date Picker and Date Input
 * @param showTodayButton the boolean to control either to show `TODAY` button or not
 * @param showCalendarSystemToggle the boolean to show the `B.S.` / `A.D.` switch, which drives
 * [NepaliDatePickerState.displayedCalendarSystem]
 * @param showAdjacentMonthDays the boolean to fill the grid's empty cells with the neighbouring
 * months' days, drawn faded. Tapping one selects that day and moves the grid to its month. A day the
 * picker cannot select, because of [NepaliSelectableDates] or the year range, stays faded and inert.
 * Off by default, matching the Material3 `DatePicker`.
 * @param colors [NepaliDatePickerColors] that will be used to resolve the colors used for this date
 * picker in different states. See [NepaliDatePickerDefaults.colors].
 * @param dayDecorator marks days that carry a holiday, a festival or an app's own event, with dots
 * under the day number and a color for the number itself. `null`, the default, leaves every day as
 * the theme draws it. Selection and the disabled state always win over a decoration. See
 * [NepaliDatePickerDefaults.eventDecorator] and [NepaliDatePickerDefaults.dayDecorator].
 *
 * Example usage:
 * ```
 * val defaultNepaliDatePickerState = rememberNepaliDatePickerState()
 *
 * NepaliDatePicker(state = defaultNepaliDatePickerState)
 *
 * // Events coloured and dotted, from the provider the app already has
 * NepaliDatePicker(
 *     state = rememberNepaliDatePickerState(),
 *     dayDecorator = NepaliDatePickerDefaults.eventDecorator(provider = myEvents)
 * )
 *
 * // Both calendars in every cell, and a switch for which one leads
 * NepaliDatePicker(
 *     state = rememberNepaliDatePickerState(),
 *     secondaryDateLocale = NepaliDatePickerDefaults.DefaultLocale,
 *     showCalendarSystemToggle = true
 * )
 *
 * // A full six-week grid, with the neighbouring months filling the edges
 * NepaliDatePicker(
 *     state = rememberNepaliDatePickerState(),
 *     showAdjacentMonthDays = true
 * )
 * ```
 *
 * @see NepaliDatePickerDialog
 * @see NepaliDatePickerWithEnglishDate
 * @see NepaliDateRangePicker
 * @see NepaliDateRangePickerWithEnglishDate
 */
@OptIn(ExperimentalNepaliDatePickerApi::class)
@Composable
fun NepaliDatePicker(
    state: NepaliDatePickerState,
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = {
        NepaliDatePickerDefaults.NepaliDatePickerTitle(
            modifier = Modifier.padding(NepaliDatePickerTitlePadding),
            language = state.locale.language,
            displayMode = state.displayMode,
            calendarSystem = state.displayedCalendarSystem
        )
    },
    headline: (@Composable () -> Unit)? = {
        NepaliDatePickerDefaults.NepaliDatePickerHeadline(
            selectedDate = state.selectedDate,
            modifier = Modifier.padding(NepaliDatePickerHeadlinePadding),
            locale = state.locale,
            displayMode = state.displayMode,
            selectedEnglishDate = state.selectedEnglishDate,
            calendarSystem = state.displayedCalendarSystem
        )
    },
    secondaryDateLocale: NepaliDateLocale? = null,
    showModeToggle: Boolean = true,
    showTodayButton: Boolean = true,
    showCalendarSystemToggle: Boolean = false,
    showAdjacentMonthDays: Boolean = false,
    colors: NepaliDatePickerColors = NepaliDatePickerDefaults.colors(),
    dayDecorator: NepaliDayDecorator? = null
) {
    val calendarModel = remember(state.locale) { NepaliCalendarModel(state.locale) }
    // `today` reads the wall clock; remember it so it isn't recomputed on every recomposition.
    val today = remember(calendarModel) { calendarModel.todayNepaliSimpleDate }
    val adapter = rememberCalendarViewAdapter(
        calendarSystem = state.displayedCalendarSystem,
        calendarModel = calendarModel,
        nepaliYearRange = state.yearRange
    )

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
        headerMinHeight = HeaderContainerHeight,
        calendarSystemToggle =
            if (showCalendarSystemToggle) {
                {
                    NepaliCalendarSystemToggle(
                        calendarSystem = state.displayedCalendarSystem,
                        onCalendarSystemChange = { system ->
                            state.displayedCalendarSystem = system
                        },
                        language = state.locale.language,
                        colors = colors
                    )
                }
            } else {
                null
            },
        displayedCalendarSystem = state.displayedCalendarSystem
    ) {
        SwitchableNepaliDateEntryContent(
            selectedDate = state.selectedDate,
            nepaliSelectableDates = state.nepaliSelectableDates,
            onDateSelectionChange = { customCalendar -> state.selectedDate = customCalendar },
            calendarModel = calendarModel,
            adapter = adapter,
            colors = colors,
            language = state.locale.language,
            nepaliDisplayMode = state.displayMode
        ) {
            NepaliDatePicker(
                selectedDate = state.selectedDate,
                nepaliSelectableDates = state.nepaliSelectableDates,
                displayedMonth = state.displayedMonthCalendar,
                calendarSystem = state.displayedCalendarSystem,
                onDateSelectionChange = { customCalendar -> state.selectedDate = customCalendar },
                onDisplayedMonthChange = { month ->
                    state.displayedMonthCalendar = month
                },
                calendarModel = calendarModel,
                yearRange = state.yearRange,
                showTodayButton = showTodayButton,
                showCalendarSystemToggle = showCalendarSystemToggle,
                showAdjacentMonthDays = showAdjacentMonthDays,
                secondaryDateLocale = secondaryDateLocale,
                colors = colors,
                today = today,
                dayDecorator = dayDecorator
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
    // Rides at the end of the title, or of the headline when there is no title. Either way it shares
    // a row that already exists, so the header never grows to hold it.
    calendarSystemToggle: (@Composable () -> Unit)? = null,
    // The headline names a date in this calendar, so it cross-dissolves when the calendar changes
    // instead of snapping while the grid below it dissolves.
    displayedCalendarSystem: CalendarSystem,
    content: @Composable () -> Unit
) {
    val toggleInHeadline = if (title == null) calendarSystemToggle else null

    Column(
        modifier = modifier.sizeIn(minWidth = ContainerWidth)
            .background(colors.containerColor)
    ) {
        NepaliDatePickerHeader(
            modifier = Modifier,
            title = title,
            titleContentColor = colors.titleContentColor,
            headlineContentColor = colors.headlineContentColor,
            headerMinHeight = headerMinHeight,
            titleTrailingContent = if (title != null) calendarSystemToggle else null
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                val hasTrailingContent = modeToggleButton != null || toggleInHeadline != null
                val horizontalArrangement = when {
                    headline != null && hasTrailingContent -> Arrangement.SpaceBetween
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
                            Box(
                                modifier = Modifier.weight(1f)
                                    .nepaliCalendarSwitchAppearance(displayedCalendarSystem)
                            ) {
                                headline()
                            }
                        }
                    }
                    if (toggleInHeadline != null) {
                        Box(modifier = Modifier.padding(end = CalendarSystemTogglePadding)) {
                            toggleInHeadline()
                        }
                    }
                    modeToggleButton?.invoke()
                }
                // Display a divider only when there is a title, or headline.
                if (title != null || headline != null || hasTrailingContent) {
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
    adapter: CalendarViewAdapter,
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
                    // The size changes in one step instead of animating. The calendar and the
                    // input field differ in height by hundreds of dp, and a dialog cannot resize
                    // smoothly over that. The fade and the slide carry the motion.
                    SizeTransform(clip = true) { _, _ -> snap() }
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
                    adapter = adapter,
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
    titleTrailingContent: (@Composable () -> Unit)?,
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    // The title carries its own top padding, so its text hugs the bottom of this
                    // row. Bottom-aligning the trailing content lines the two up; centering would
                    // float it above the text.
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        title()
                    }
                    if (titleTrailingContent != null) {
                        Box(modifier = Modifier.padding(end = CalendarSystemTogglePadding)) {
                            titleTrailingContent()
                        }
                    }
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
    displayedMonth: MonthCalendar,
    calendarSystem: CalendarSystem,
    onDateSelectionChange: (CustomCalendar) -> Unit,
    onDisplayedMonthChange: (MonthCalendar) -> Unit,
    calendarModel: NepaliCalendarModel,
    yearRange: IntRange,
    nepaliSelectableDates: NepaliSelectableDates,
    showTodayButton: Boolean,
    showCalendarSystemToggle: Boolean,
    showAdjacentMonthDays: Boolean,
    secondaryDateLocale: NepaliDateLocale?,
    colors: NepaliDatePickerColors,
    today: SimpleDate,
    dayDecorator: NepaliDayDecorator?
) {
    val adapter = rememberCalendarViewAdapter(calendarSystem, calendarModel, yearRange)

    // Plain remember: these are pure functions of their keys, not derived reads of other
    // snapshot state, so derivedStateOf would only add a redundant observer allocation.
    val displayedMonthIndex = remember(displayedMonth, adapter) {
        displayedMonth.indexIn(adapter.yearRange)
    }
    val todayMonth = remember(today, adapter) { adapter.monthContaining(today) }
    val initialIndex = remember(todayMonth, adapter) { todayMonth.indexIn(adapter.yearRange) }

    val isToday = displayedMonthIndex == initialIndex

    // A calendar switch changes both how many months the pager holds and what an index means, so
    // the list state cannot be carried over; `key` rebuilds it at the new calendar's index.
    val monthsListState = key(calendarSystem) {
        rememberLazyListState(initialFirstVisibleItemIndex = displayedMonthIndex)
    }
    val coroutineScope = rememberCoroutineScope()
    var yearPickerVisible by rememberSaveable { mutableStateOf(false) }

    val chosenLanguage = calendarModel.locale.language
    val weekDayFormat = calendarModel.locale.weekDayName
    val fullMonthName = adapter.monthName(displayedMonth.month, chosenLanguage, NameFormat.FULL)
    val fullYear = calendarModel.localizeNumber(
        stringToLocalize = displayedMonth.year.toString(), locale = chosenLanguage
    )
    val formattedMonthYear = "$fullMonthName $fullYear"

    // The second line under the year button names the months of the other calendar this one
    // straddles. It earns its space whenever a second calendar is in play, either as the small
    // number in each cell or as the thing the switch switches to.
    val secondaryLabelLanguage = secondaryDateLocale?.language ?: chosenLanguage
    val showSecondaryLabel = secondaryDateLocale != null || showCalendarSystemToggle
    val formattedSecondaryMonthYear = remember(
        displayedMonth, adapter, secondaryLabelLanguage, showSecondaryLabel
    ) {
        if (showSecondaryLabel) {
            adapter.secondaryMonthLabel(
                days = adapter.daysIn(displayedMonth, withSecondary = true),
                calendarModel = calendarModel,
                language = secondaryLabelLanguage
            )
        } else {
            null
        }
    }

    // The month label, the grid and the year overlay all change together on a switch, so the whole
    // block arrives as one.
    Column(modifier = Modifier.nepaliCalendarSwitchAppearance(calendarSystem)) {
        NepaliMonthsNavigation(
            modifier = Modifier.padding(horizontal = DatePickerHorizontalPadding),
            isToday = isToday,
            todayText = chosenLanguage.today,
            nextAvailable = monthsListState.canScrollForward,
            previousAvailable = monthsListState.canScrollBackward,
            yearPickerVisible = yearPickerVisible,
            yearPickerText = formattedMonthYear,
            yearPickerSubtitle = formattedSecondaryMonthYear,
            showTodayButton = showTodayButton,
            onNextClicked = {
                coroutineScope.scrollMonthsTo(
                    monthsListState, monthsListState.firstVisibleItemIndex + 1
                )
            },
            onPreviousClicked = {
                coroutineScope.scrollMonthsTo(
                    monthsListState, monthsListState.firstVisibleItemIndex - 1
                )
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
                    adapter = adapter,
                    onDateSelectionChange = onDateSelectionChange,
                    onDisplayedMonthChange = onDisplayedMonthChange,
                    selectedDate = selectedDate,
                    nepaliSelectableDates = nepaliSelectableDates,
                    calendarModel = calendarModel,
                    colors = colors,
                    dayShape = if (secondaryDateLocale != null) {
                        RoundedCornerShape(DualDateDayCornerRadius)
                    } else {
                        CircleShape
                    },
                    secondaryDateLanguage = secondaryDateLocale?.language,
                    showAdjacentMonthDays = showAdjacentMonthDays,
                    dayDecorator = dayDecorator
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
                        currentYear = todayMonth.year,
                        displayedYear = displayedMonth.year,
                        onYearSelected = { year ->
                            // Switch back to the monthly calendar and scroll to the selected year.
                            yearPickerVisible = !yearPickerVisible
                            coroutineScope.launch {
                                // Scroll to the selected year (maintaining the month of year).
                                // A LaunchEffect at the MonthsList will take care of rest and will
                                // update the state's displayedMonth to the month we scrolled to.
                                monthsListState.scrollToItem(
                                    (year - adapter.yearRange.first) * NepaliMonthsInYear +
                                            displayedMonth.month - 1
                                )
                            }
                        },
                        nepaliSelectableDates = nepaliSelectableDates,
                        calendarModel = calendarModel,
                        adapter = adapter,
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

    /**
     * The Bikram Sambat month the grid is showing.
     *
     * While [displayedCalendarSystem] is [CalendarSystem.GREGORIAN] the grid pages over Gregorian
     * months, and this reports the Bikram Sambat month holding the first day of the visible one.
     * Assigning still works in either calendar: the grid moves to whichever of its own months
     * contains the first day of the month you assign. Use [displayedMonthCalendar] when you want
     * the month exactly as displayed.
     */
    var displayedMonth: NepaliMonthCalendar
    val selectedEnglishDate: CustomCalendar?
    var displayMode: DisplayMode
    val yearRange: IntRange
    val nepaliSelectableDates: NepaliSelectableDates
    val locale: NepaliDateLocale

    /**
     * Which calendar the grid shows.
     *
     * Only the display changes: [selectedDate] stays a Bikram Sambat date whichever calendar is on
     * screen, so switching keeps the same day selected and leaves [nepaliSelectableDates] rules
     * working unchanged. The grid re-anchors on the selected date, or on the visible month when
     * there is no selection.
     *
     * Defaults to [CalendarSystem.BIKRAM_SAMBAT]. The default implementation here is inert, so a
     * custom implementation of this interface keeps compiling but will not respond to the picker's
     * calendar switch until it overrides this.
     */
    var displayedCalendarSystem: CalendarSystem
        get() = CalendarSystem.BIKRAM_SAMBAT
        set(value) = Unit

    /**
     * The month the grid is showing, in [displayedCalendarSystem].
     *
     * The calendar-agnostic counterpart of [displayedMonth]. Defaults to [displayedMonth] read as a
     * Bikram Sambat month.
     */
    var displayedMonthCalendar: MonthCalendar
        get() = displayedMonth.toMonthCalendar()
        set(value) {
            displayedMonth = value.toNepaliMonthCalendar()
        }

    /**
     * English years the grid covers when [displayedCalendarSystem] is [CalendarSystem.GREGORIAN].
     *
     * Derived from [yearRange] so both calendars page over the same span of real days, then clamped
     * into [NepaliCalendarDefaults.EnglishYearRange].
     */
    val englishYearRange: IntRange
        get() = NepaliCalendarDefaults.gregorianYearRangeFor(yearRange)
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
 * Out-of-range or invalid initial values are coerced rather than rejected: the displayed month is
 * clamped into [yearRange], and an out-of-range or non-existent initial selected date resolves to
 * no selection.
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
    initialCalendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT
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
            locale = locale,
            initialCalendarSystem = initialCalendarSystem
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
 * Out-of-range or invalid initial values are coerced rather than rejected: the displayed month is
 * clamped into [yearRange], and an out-of-range or non-existent initial selected date resolves to
 * no selection.
 *
 * @see rememberNepaliDatePickerState
 */
fun NepaliDatePickerState(
    initialSelectedDate: SimpleDate? = null,
    initialDisplayedMonth: SimpleDate? = initialSelectedDate,
    yearRange: IntRange = NepaliCalendarDefaults.NepaliYearRange,
    initialDisplayMode: DisplayMode = DisplayMode.Picker,
    nepaliSelectableDates: NepaliSelectableDates = NepaliDatePickerDefaults.AllDates,
    locale: NepaliDateLocale,
    initialCalendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT
): NepaliDatePickerState = NepaliDatePickerStateImpl(
    initialSelectedDate = initialSelectedDate,
    initialDisplayedMonth = initialDisplayedMonth,
    yearRange = yearRange,
    initialDisplayMode = initialDisplayMode,
    nepaliSelectableDates = nepaliSelectableDates,
    locale = locale,
    initialCalendarSystem = initialCalendarSystem
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
 * Out-of-range or invalid initial values are coerced rather than rejected: the displayed month is
 * clamped into [yearRange], and an out-of-range or non-existent initial selected date resolves to
 * no selection.
 */
@Stable
internal abstract class BaseNepaliDatePickerStateImpl(
    initialDisplayedMonth: SimpleDate?,
    initialCalendarSystem: CalendarSystem,
    val yearRange: IntRange,
    val nepaliSelectableDates: NepaliSelectableDates,
    val locale: NepaliDateLocale,
) {

    protected val calendarModel = NepaliCalendarModel(locale)

    open val englishYearRange: IntRange =
        NepaliCalendarDefaults.gregorianYearRangeFor(yearRange)

    // The caller's year range can reach past the conversion table; every month lookup is clamped
    // into the intersection so a wide range degrades to the supported span instead of throwing.
    private val supportedYearRange: IntRange = IntRange(
        yearRange.first.coerceIn(NepaliCalendarDefaults.NepaliYearRange),
        yearRange.last.coerceIn(NepaliCalendarDefaults.NepaliYearRange)
    )

    private var _displayedCalendarSystem = mutableStateOf(initialCalendarSystem)

    // Derived from the calendar system, which is snapshot state, so this plain field is only ever
    // read and written together with it and never observed from composition.
    private var adapter: CalendarViewAdapter =
        calendarViewAdapter(initialCalendarSystem, calendarModel, yearRange)

    private val initialAnchorDate: SimpleDate =
        coerceCanonicalDate(initialDisplayedMonth ?: calendarModel.todayNepaliSimpleDate)

    private var _displayedMonth = mutableStateOf(adapter.monthContaining(initialAnchorDate))

    // Reading `displayedMonth` in Gregorian mode means converting a whole month, so the Bikram
    // Sambat projection is computed once per move rather than on every read.
    private var _displayedNepaliMonth = mutableStateOf(nepaliMonthOf(_displayedMonth.value))

    // The day a calendar switch re-centres on when nothing is selected. It has to be a date the
    // user is actually looking at, held across switches: deriving it from the visible month each
    // time loses a month per switch, because one Gregorian month spans two Bikram Sambat ones and
    // the projection can only name the first of them.
    private var _anchorDate = mutableStateOf(initialAnchorDate)

    open var displayedCalendarSystem: CalendarSystem
        get() = _displayedCalendarSystem.value
        set(calendarSystem) {
            if (calendarSystem == _displayedCalendarSystem.value) return

            // Re-anchor before swapping so the user keeps looking at the same point in time.
            val anchor = canonicalAnchor()
            _displayedCalendarSystem.value = calendarSystem
            adapter = calendarViewAdapter(calendarSystem, calendarModel, yearRange)
            moveTo(adapter.monthContaining(anchor))
        }

    open var displayedMonthCalendar: MonthCalendar
        get() = _displayedMonth.value
        set(month) = moveTo(month)

    var displayedMonth: NepaliMonthCalendar
        get() = _displayedNepaliMonth.value
        set(month) {
            moveTo(adapter.monthContaining(coerceCanonicalDate(SimpleDate(month.year, month.month))))
        }

    /**
     * The Bikram Sambat date the grid re-anchors on when the calendar changes. Subclasses prefer a
     * selected date when they have one.
     */
    protected open fun canonicalAnchor(): SimpleDate = _anchorDate.value

    /** Puts the grid on the month holding [canonicalDate], in whichever calendar is displayed. */
    protected fun showCanonicalDate(canonicalDate: SimpleDate) {
        val coerced = coerceCanonicalDate(canonicalDate)
        _anchorDate.value = coerced
        moveTo(adapter.monthContaining(coerced))
    }

    private fun moveTo(month: MonthCalendar) {
        _displayedMonth.value = month
        _displayedNepaliMonth.value = nepaliMonthOf(month)
        // Keep the anchor while it is still inside the month on screen, and only re-seat it once
        // the user has moved somewhere it no longer is. Re-seating it on every move would make a
        // switch walk backwards a month at a time.
        if (adapter.monthContaining(_anchorDate.value) != month) {
            _anchorDate.value = firstCanonicalDayOf(month)
        }
    }

    /** The earliest day of [month] that has a Bikram Sambat date. */
    private fun firstCanonicalDayOf(month: MonthCalendar): SimpleDate =
        when (month.calendarSystem) {
            CalendarSystem.BIKRAM_SAMBAT -> SimpleDate(month.year, month.month, 1)
            CalendarSystem.GREGORIAN -> firstNepaliDayIn(month)?.toSimpleDate()
                ?: SimpleDate(supportedYearRange.first, 1, 1)
        }

    /** The Bikram Sambat month holding the first day of [month] that has a Bikram Sambat date. */
    private fun nepaliMonthOf(month: MonthCalendar): NepaliMonthCalendar {
        if (month.calendarSystem == CalendarSystem.BIKRAM_SAMBAT) {
            return month.toNepaliMonthCalendar()
        }
        val firstNepaliDay = firstNepaliDayIn(month)
        return calendarModel.getNepaliMonth(
            nepaliYear = firstNepaliDay?.year ?: supportedYearRange.first,
            nepaliMonth = firstNepaliDay?.month ?: 1
        )
    }

    /**
     * The Bikram Sambat date of the first convertible day of a Gregorian [month], or `null` when
     * the whole month predates the conversion anchor.
     *
     * Day 1 answers this for every month but the one holding the anchor, so the cheap single
     * conversion is tried first and the whole-month scan is the fallback.
     */
    private fun firstNepaliDayIn(month: MonthCalendar): CustomCalendar? =
        if (calendarModel.isEnglishDateConvertible(month.year, month.month, 1)) {
            calendarModel.convertToNepaliCalendar(month.year, month.month, 1)
        } else {
            calendarModel.getNepaliCalendarsInEnglishMonth(month.year, month.month)
                .firstNotNullOfOrNull { it }
        }

    // The displayed month drives the month pager, so an out-of-range date would crash on scroll.
    // Clamp year, month and day into something the conversion table can answer for.
    private fun coerceCanonicalDate(date: SimpleDate): SimpleDate {
        val year = date.year.coerceIn(supportedYearRange)
        val month = date.month.coerceIn(1, NepaliMonthsInYear)
        val dayOfMonth = date.dayOfMonth
            .coerceIn(1, calendarModel.getTotalDaysInNepaliMonth(year, month))
        return SimpleDate(year, month, dayOfMonth)
    }
}

/**
 * A default implementation of the [NepaliDatePickerState]. See [rememberNepaliDatePickerState].
 *
 * @see rememberNepaliDatePickerState
 * Out-of-range or invalid initial values are coerced rather than rejected: the displayed month is
 * clamped into [yearRange], and an out-of-range or non-existent initial selected date resolves to
 * no selection.
 */
@Stable
private class NepaliDatePickerStateImpl(
    initialSelectedDate: SimpleDate?,
    initialDisplayedMonth: SimpleDate?,
    yearRange: IntRange,
    initialDisplayMode: DisplayMode,
    nepaliSelectableDates: NepaliSelectableDates,
    locale: NepaliDateLocale,
    initialCalendarSystem: CalendarSystem
) : BaseNepaliDatePickerStateImpl(
    initialDisplayedMonth = initialDisplayedMonth,
    initialCalendarSystem = initialCalendarSystem,
    yearRange = yearRange,
    nepaliSelectableDates = nepaliSelectableDates,
    locale = locale
), NepaliDatePickerState {

    /**
     * A mutable state of [CustomCalendar] that represents a selected date.
     */
    // Drop an out-of-range or unparseable initial date to "no selection" instead of crashing.
    private var _selectedDate = mutableStateOf(
        initialSelectedDate?.let {
            runCatching { calendarModel.getNepaliCalendar(simpleNepaliDate = it) }
                .getOrNull()
                ?.takeIf { date -> yearRange.contains(date.year) }
        })

    override var selectedDate: CustomCalendar?
        get() = _selectedDate.value
        // Keep only an in-range date; anything else clears the selection so the headline and pager
        // never receive a value they cannot render.
        set(customCalendar) {
            _selectedDate.value = customCalendar?.takeIf { yearRange.contains(it.year) }
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
            // Show the month holding the selected day itself. Going through `displayedMonth` would
            // route via the first of its Bikram Sambat month, which in a Gregorian grid can be the
            // month before the one the selection actually falls in.
            selectedDate?.let { showCanonicalDate(it.toSimpleDate()) }
            _displayMode.value = displayMode
        }

    // Keep the selected date in view across a calendar switch; only fall back to the visible month
    // when nothing is selected.
    override fun canonicalAnchor(): SimpleDate =
        selectedDate?.toSimpleDate() ?: super.canonicalAnchor()

    // The base class already tracks all three. These overrides only pick it over the interface's
    // inert default, which exists so that a custom NepaliDatePickerState keeps compiling.
    override var displayedCalendarSystem: CalendarSystem
        get() = super<BaseNepaliDatePickerStateImpl>.displayedCalendarSystem
        set(value) {
            super<BaseNepaliDatePickerStateImpl>.displayedCalendarSystem = value
        }

    override var displayedMonthCalendar: MonthCalendar
        get() = super<BaseNepaliDatePickerStateImpl>.displayedMonthCalendar
        set(value) {
            super<BaseNepaliDatePickerStateImpl>.displayedMonthCalendar = value
        }

    override val englishYearRange: IntRange
        get() = super<BaseNepaliDatePickerStateImpl>.englishYearRange

    companion object {
        fun Saver(
            nepaliSelectableDates: NepaliSelectableDates, locale: NepaliDateLocale
        ): Saver<NepaliDatePickerStateImpl, Any> = listSaver(save = { state ->
            listOf(
                state.selectedDate?.encodeToSimpleDateString(),
                state.displayedMonth.encodeToSimpleDateString(),
                state.yearRange.first,
                state.yearRange.last,
                state.displayMode.value,
                // Stored as the era rather than the ordinal so reordering the enum cannot silently
                // restore the wrong calendar.
                state.displayedCalendarSystem.era
            )
        }, restore = { value ->
            NepaliDatePickerStateImpl(
                initialSelectedDate = if (value.isNotEmpty()) decodeSimpleDateFromString(value[0] as? String) else null,
                initialDisplayedMonth = if (value.size > 1) decodeSimpleDateFromString(value[1] as? String) else null,
                yearRange = IntRange(value[2] as Int, value[3] as Int),
                initialDisplayMode = DisplayMode(value[4] as Int),
                nepaliSelectableDates = nepaliSelectableDates,
                locale = locale,
                initialCalendarSystem = decodeCalendarSystem(value.getOrNull(5))
            )
        })
    }
}

/**
 * A composable that shows a year menu button and a couple of buttons that enable navigation between
 * displayed months.
 *
 * The calendar switch is deliberately not here. At the picker's 360dp width the year button, `TODAY`
 * and the two arrows already fill this row, so an inline switch would push the year label into
 * truncation; it rides in the header's title instead.
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
    nextMonthContentDescription: String? = null,
    // When non-null, a smaller second line under the year label (the other calendar's month/year in
    // the dual-date pickers). The row grows to fit it via heightIn instead of a fixed requiredHeight.
    yearPickerSubtitle: String? = null
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
                if (yearPickerSubtitle != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = yearPickerText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = yearPickerSubtitle,
                            modifier = Modifier.alpha(0.75f),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                        )
                    }
                } else {
                    Text(
                        text = yearPickerText,
                        modifier = Modifier,
                        style = MaterialTheme.typography.bodyLarge
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
    // Pair each abbreviated label with its full weekday name so screen readers announce
    // "Monday" instead of the single letter "M".
    val dayNames = remember(language, weekDayFormat) {
        val firstDayOfWeek = NepaliDatePickerDefaults.FIRST_DAY_OF_WEEK
        val weekdays = language.weekdays
        (firstDayOfWeek..firstDayOfWeek + 6).map { dayIndex ->
            val weekday = weekdays[dayIndex - 1]
            val display = if (weekDayFormat == NameFormat.SHORT) weekday.short else weekday.medium
            display to weekday.full
        }
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
    adapter: CalendarViewAdapter,
    onDateSelectionChange: (CustomCalendar) -> Unit,
    onDisplayedMonthChange: (MonthCalendar) -> Unit,
    selectedDate: CustomCalendar?,
    calendarModel: NepaliCalendarModel,
    nepaliSelectableDates: NepaliSelectableDates,
    colors: NepaliDatePickerColors,
    dayShape: Shape = CircleShape,
    secondaryDateLanguage: NepaliDatePickerLang? = null,
    showAdjacentMonthDays: Boolean = false,
    dayDecorator: NepaliDayDecorator? = null
) {
    val snapFlingBehavior = rememberCustomSnapFlingBehavior(lazyListState = lazyListState)
    val coroutineScope = rememberCoroutineScope()
    val onNavigateToMonth: (Int) -> Unit = remember(lazyListState, coroutineScope) {
        { monthIndex -> coroutineScope.scrollMonthsTo(lazyListState, monthIndex) }
    }

    LazyRow(
        modifier = Modifier, state = lazyListState, flingBehavior = snapFlingBehavior
    ) {
        items(
            count = numberOfMonthsInRange(yearRange = adapter.yearRange),
            key = { index: Int -> index }) { index ->
            val monthCalendar = remember(index, adapter) { adapter.monthAt(index) }
            Box(
                modifier = Modifier.fillParentMaxWidth()
            ) {
                NepaliMonth(
                    monthCalendar = monthCalendar,
                    adapter = adapter,
                    todayDate = today,
                    startDate = selectedDate,
                    calendarModel = calendarModel,
                    onDateSelectionChange = onDateSelectionChange,
                    nepaliSelectableDates = nepaliSelectableDates,
                    colors = colors,
                    endDate = null,
                    dayShape = dayShape,
                    secondaryDateLanguage = secondaryDateLanguage,
                    showAdjacentMonthDays = showAdjacentMonthDays,
                    dayDecorator = dayDecorator,
                    onNavigateToMonth = onNavigateToMonth
                )
            }
        }
    }

    LaunchedEffect(lazyListState, adapter) {
        updateDisplayedMonth(
            lazyListState = lazyListState,
            adapter = adapter,
            onDisplayedMonthChange = onDisplayedMonthChange
        )
    }
}

/**
 * Returns the number of months within the given year range.
 */
internal fun numberOfMonthsInRange(yearRange: IntRange) =
    (yearRange.last - yearRange.first + 1) * NepaliMonthsInYear

/**
 * Animates the month pager to [monthIndex].
 *
 * An index the list cannot reach is ignored rather than thrown, which happens when the user taps
 * again while a previous scroll is still animating.
 */
internal fun CoroutineScope.scrollMonthsTo(lazyListState: LazyListState, monthIndex: Int) {
    launch {
        try {
            lazyListState.animateScrollToItem(monthIndex)
        } catch (_: IllegalArgumentException) {
            // Nothing to move to, so stay where we are.
        }
    }
}

internal suspend fun updateDisplayedMonth(
    lazyListState: LazyListState,
    adapter: CalendarViewAdapter,
    onDisplayedMonthChange: (MonthCalendar) -> Unit
) {
    snapshotFlow { lazyListState.firstVisibleItemIndex }.collect { index ->
        onDisplayedMonthChange(adapter.monthAt(index))
    }
}

@Composable
internal fun NepaliMonth(
    monthCalendar: MonthCalendar,
    adapter: CalendarViewAdapter,
    todayDate: SimpleDate,
    startDate: CustomCalendar?,
    endDate: CustomCalendar?,
    calendarModel: NepaliCalendarModel,
    nepaliSelectableDates: NepaliSelectableDates,
    onDateSelectionChange: (CustomCalendar) -> Unit,
    colors: NepaliDatePickerColors,
    nepaliSelectedRangeInfo: NepaliSelectedRangeInfo? = null,
    dayShape: Shape = CircleShape,
    // When non-null, each cell also renders the same day in the other calendar (dual-date cell).
    secondaryDateLanguage: NepaliDatePickerLang? = null,
    showAdjacentMonthDays: Boolean = false,
    // Consulted once per drawn day for the colors and dots an event adds to it.
    dayDecorator: NepaliDayDecorator? = null,
    // Invoked with a pager index when a day of a neighbouring month is tapped.
    onNavigateToMonth: (monthIndex: Int) -> Unit = {}
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

    val cells = remember(monthCalendar, adapter, secondaryDateLanguage, showAdjacentMonthDays) {
        adapter.monthGrid(
            month = monthCalendar,
            withSecondary = secondaryDateLanguage != null,
            withAdjacentDays = showAdjacentMonthDays
        )
    }
    val monthIndex = remember(monthCalendar, adapter) { monthCalendar.indexIn(adapter.yearRange) }

    // FULL-format locale used only to build each cell's screen-reader description.
    val a11yLocale = remember(calendarModel) {
        calendarModel.locale.copy(dateFormat = NepaliDateFormatStyle.FULL)
    }
    // The other half of a dual-date cell, spoken after the displayed date. LONG rather than FULL,
    // since the weekday it would add has already been said once.
    val secondaryA11yLocale = remember(calendarModel, secondaryDateLanguage) {
        secondaryDateLanguage?.let {
            calendarModel.locale.copy(language = it, dateFormat = NepaliDateFormatStyle.LONG)
        }
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
                    val cell = cells[cellIndex]
                    if (cell == null) {
                        // Empty cell
                        Spacer(
                            modifier = Modifier.requiredSize(
                                width = RecommendedSizeForAccessibility,
                                height = RecommendedSizeForAccessibility
                            )
                        )
                    } else {
                        val day = cell.day
                        val dayNumber = day.displayed.dayOfMonth
                        // Selection, "today" and the selectable-date rules all run on the Bikram
                        // Sambat date, so they mean the same thing in either calendar. A day with
                        // no Bikram Sambat equivalent (before the conversion anchor) is inert.
                        val canonicalDate = day.canonical

                        val isToday = canonicalDate != null &&
                                todayDate == canonicalDate.toSimpleDate()
                        val startDateSelected =
                            canonicalDate != null && startDate == canonicalDate
                        val endDateSelected = canonicalDate != null && endDate == canonicalDate

                        val inRange = if (nepaliSelectedRangeInfo != null) {
                            remember(canonicalDate, startDate, endDate) {
                                isInSelectedRange(
                                    canonicalDate = canonicalDate,
                                    rangeStart = startDate,
                                    rangeEnd = endDate,
                                    compareDates = { date, year, month, dayOfMonth ->
                                        calendarModel.compareDates(date, year, month, dayOfMonth)
                                    }
                                )
                            }
                        } else {
                            false
                        }

                        // Full localized date (+ "today") read by screen readers for this cell.
                        val dayContentDescription = remember(
                            day, isToday, cell, adapter, secondaryA11yLocale
                        ) {
                            buildString {
                                append(adapter.format(day.displayed, a11yLocale))
                                // A dual-date cell draws two numbers, and the description is what a
                                // screen reader reads instead of them, so it has to carry both.
                                val secondaryDate = day.secondary
                                if (secondaryA11yLocale != null && secondaryDate != null) {
                                    append(", ")
                                    append(
                                        adapter.formatSecondary(
                                            secondaryDate = secondaryDate,
                                            calendarModel = calendarModel,
                                            locale = secondaryA11yLocale
                                        )
                                    )
                                }
                                if (isToday) {
                                    append(", ")
                                    append(calendarModel.locale.language.today)
                                }
                                // The fading is what tells a sighted user this cell moves the grid.
                                if (cell.monthOffset != 0) {
                                    append(", ")
                                    append(
                                        calendarModel.locale.language
                                            .adjacentMonthDayContentDescription
                                    )
                                }
                            }
                        }

                        val secondaryDay = if (secondaryDateLanguage != null) {
                            day.secondary?.dayOfMonth
                        } else {
                            null
                        }

                        // Disabled in case the day's year is not selectable, or the date itself is
                        // specifically not allowed by the state's SelectableDates.
                        val dayEnabled = remember(canonicalDate, nepaliSelectableDates) {
                            canonicalDate != null && with(nepaliSelectableDates) {
                                isSelectableYear(canonicalDate.year)
                                        && isSelectableDate(canonicalDate)
                            }
                        }
                        val daySelected = startDateSelected || endDateSelected

                        val decoration = if (dayDecorator == null || canonicalDate == null) {
                            null
                        } else {
                            remember(
                                dayDecorator, canonicalDate, day.displayed, isToday, daySelected,
                                inRange, dayEnabled, cell.monthOffset
                            ) {
                                dayDecorator.decorate(
                                    NepaliDayInfo(
                                        date = canonicalDate,
                                        displayedDate = day.displayed,
                                        isToday = isToday,
                                        isSelected = daySelected,
                                        isInRange = inRange,
                                        isEnabled = dayEnabled,
                                        isAdjacentMonth = cell.monthOffset != 0
                                    )
                                )
                            }
                        }

                        // What the decoration calls the day is spoken after the date itself, so an
                        // event is announced rather than left to the color of the number.
                        val cellContentDescription = remember(dayContentDescription, decoration) {
                            val marking = decoration?.contentDescription
                            if (marking.isNullOrEmpty()) {
                                dayContentDescription
                            } else {
                                "$dayContentDescription, $marking"
                            }
                        }

                        NepaliDay(
                            // A neighbouring month's day is faded so it reads as context around the
                            // displayed month rather than part of it.
                            modifier = if (cell.monthOffset != 0) {
                                Modifier.alpha(AdjacentMonthDayAlpha)
                            } else {
                                Modifier
                            },
                            selected = startDateSelected || endDateSelected,
                            onClick = {
                                canonicalDate?.let(onDateSelectionChange)
                                if (cell.monthOffset != 0) {
                                    onNavigateToMonth(monthIndex + cell.monthOffset)
                                }
                            },
                            animateChecked = startDateSelected,
                            enabled = dayEnabled,
                            today = isToday,
                            colors = colors,
                            inRange = inRange,
                            dateContentDescription = cellContentDescription,
                            shape = dayShape,
                            decoration = decoration,
                            isDualDateCell = secondaryDay != null
                        ) {
                            NepaliDayNumbers(
                                dayNumber = dayNumber,
                                secondaryDayNumber = secondaryDay,
                                calendarModel = calendarModel,
                                secondaryDateLanguage = secondaryDateLanguage
                            )
                        }
                    }
                    cellIndex++
                }
            }
        }
    }
}

/**
 * The number, or the pair of numbers, a day cell draws.
 *
 * A dual-date cell carries the displayed calendar's day at its centre and the other calendar's in
 * the bottom-end corner, which is the corner its dots move away from.
 */
@Composable
internal fun NepaliDayNumbers(
    dayNumber: Int,
    secondaryDayNumber: Int?,
    calendarModel: NepaliCalendarModel,
    secondaryDateLanguage: NepaliDatePickerLang?
) {
    val displayedNumber = calendarModel.localizeNumber(
        stringToLocalize = dayNumber.toString(),
        locale = calendarModel.locale.language
    )
    if (secondaryDateLanguage == null || secondaryDayNumber == null) {
        Text(
            text = displayedNumber,
            modifier = Modifier,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        return
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = displayedNumber,
            modifier = Modifier.align(Alignment.Center)
                .padding(bottom = DualDateDayNumberBottomPadding, end = DualDateDayNumberEndPadding),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = DualDateDayNumberSize)
        )
        Text(
            text = calendarModel.localizeNumber(
                stringToLocalize = secondaryDayNumber.toString(),
                locale = secondaryDateLanguage
            ),
            modifier = Modifier.align(Alignment.BottomEnd)
                .padding(end = DualDateDayNumberEndPadding)
                .alpha(SecondaryDayNumberAlpha),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = SecondaryDayNumberSize)
        )
    }
}

/**
 * One day of a month grid: the disc or rounded box behind the number, the border that marks today,
 * and the dots an event adds. Shared by the pickers and by [NepaliCalendar], so a day is drawn the
 * same way whichever surface it appears on.
 */
@Composable
internal fun NepaliDay(
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
    decoration: NepaliDayDecoration? = null,
    isDualDateCell: Boolean = false,
    content: @Composable () -> Unit
) {
    val visuals = resolveDayVisuals(
        decoration = decoration,
        themeContainerColor = colors.dayContainerColor(
            selected = selected, enabled = enabled, animate = animateChecked
        ).value,
        themeContentColor = colors.dayContentColor(
            isToday = today,
            selected = selected,
            inRange = inRange,
            enabled = enabled
        ).value,
        selectedIndicatorColor = colors.selectedDayContentColor,
        inRangeIndicatorColor = colors.dayInSelectionRangeContentColor,
        isSelected = selected,
        isInRange = inRange,
        isEnabled = enabled,
        maxIndicators = if (isDualDateCell) MaxDualDateDayIndicators else MaxDayIndicators
    )

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
        color = visuals.containerColor,
        contentColor = visuals.contentColor,
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
            if (visuals.indicators.isNotEmpty()) {
                // A dual-date cell already draws the other calendar's number in its bottom-end
                // corner, so the dots take the opposite one rather than sitting on top of it.
                NepaliDayIndicators(
                    indicators = visuals.indicators,
                    modifier = if (isDualDateCell) {
                        Modifier.align(Alignment.BottomStart)
                            .padding(start = DayIndicatorPadding, bottom = DayIndicatorPadding)
                    } else {
                        Modifier.align(Alignment.BottomCenter).padding(bottom = DayIndicatorPadding)
                    }
                )
            }
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
    adapter: CalendarViewAdapter,
    colors: NepaliDatePickerColors
) {
    val yearRange = adapter.yearRange
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
        items(count = yearRange.last - yearRange.first + 1, key = { index: Int -> index }) { index ->
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
                // isSelectableYear speaks Bikram Sambat. A Gregorian year straddles two of them, so
                // it stays reachable while either one is selectable.
                enabled = remember(selectedYear, adapter, nepaliSelectableDates) {
                    adapter.canonicalYearsIn(selectedYear)
                        .any { nepaliSelectableDates.isSelectableYear(it) }
                },
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

/**
 * Reads a saved [CalendarSystem] back from its era.
 *
 * Falls back to [CalendarSystem.BIKRAM_SAMBAT] for anything unrecognised, which includes the `null`
 * a state saved before the calendar switch existed produces.
 */
internal fun decodeCalendarSystem(savedEra: Any?): CalendarSystem =
    (savedEra as? Int)?.let { CalendarSystem.fromEra(it) } ?: CalendarSystem.BIKRAM_SAMBAT

internal const val NepaliDaysInWeek: Int = 7

internal const val NepaliMaxCalendarRows = 6

/** How far a neighbouring month's day is faded against the days of the displayed month. */
internal const val AdjacentMonthDayAlpha = 0.38f

internal const val NepaliYearsInRow: Int = 3
internal const val NepaliMonthsInYear: Int = 12

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
internal val CalendarSystemTogglePadding = 12.dp
internal val DualDateDayCornerRadius = 4.dp

/** Lift of a dual-date cell's own number, which makes room for the other calendar's under it. */
private val DualDateDayNumberBottomPadding = 4.dp

/** Distance both numbers of a dual-date cell keep from its end edge. */
private val DualDateDayNumberEndPadding = 2.dp

/** Size of the displayed calendar's number in a dual-date cell, which holds two. */
private val DualDateDayNumberSize = 16.5.sp

/** Size of the other calendar's number in a dual-date cell. */
private val SecondaryDayNumberSize = 8.sp

/** How far the other calendar's number is faded against the day's own. */
private const val SecondaryDayNumberAlpha = 0.75f
internal val NepaliDatePickerTitlePadding = PaddingValues(start = 24.dp, end = 12.dp, top = 16.dp)
private val NepaliDatePickerHeadlinePadding =
    PaddingValues(start = 24.dp, end = 12.dp, bottom = 12.dp)
internal val NepaliDatePickerModeTogglePadding = PaddingValues(end = 12.dp, bottom = 12.dp)