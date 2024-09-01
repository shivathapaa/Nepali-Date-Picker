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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerColors
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toNepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.max

/**
 * Nepali Date Picker lets user select a date and preferably should be embedded into Dialogs.
 * See [NepaliDatePickerDialog].
 *
 * Nepali date picker lets you pick a date via a calendar UI.
 *
 * @param state state of the date picker. See [rememberNepaliDatePickerState].
 * @param modifier the [Modifier] to be applied to this date picker
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
@Composable
fun NepaliDatePicker(
    state: NepaliDatePickerState,
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = {
        NepaliDatePickerDefaults.NepaliDatePickerTitle(
            modifier = Modifier.padding(NepaliDatePickerTitlePadding),
            language = state.locale.language
        )
    },
    headline: (@Composable () -> Unit)? = {
        NepaliDatePickerDefaults.NepaliDatePickerHeadline(
            selectedDate = state.selectedDate?.toSimpleDate(),
            modifier = Modifier.padding(NepaliDatePickerHeadlinePadding),
            locale = state.locale
        )
    },
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
    colors: NepaliDatePickerColors,
    today: SimpleDate
) {
    val displayedMonthIndex by remember(displayedMonth) {
        derivedStateOf { displayedMonth.indexIn(yearRange) }
    }
    val monthsListState = rememberLazyListState(initialFirstVisibleItemIndex = displayedMonthIndex)
    val coroutineScope = rememberCoroutineScope()
    var yearPickerVisible by rememberSaveable { mutableStateOf(false) }

    val chosenLanguage = calendarModel.locale.language
    val fullMonthName = chosenLanguage.months[displayedMonth.month - 1].full
    val fullYear = calendarModel.localizeNumber(
        stringToLocalize = displayedMonth.year.toString(), locale = chosenLanguage
    )
    val formattedMonthYear = "$fullMonthName $fullYear"

    Column {
        NepaliMonthsNavigation(
            modifier = Modifier.padding(horizontal = DatePickerHorizontalPadding),
            nextAvailable = monthsListState.canScrollForward,
            previousAvailable = monthsListState.canScrollBackward,
            yearPickerVisible = yearPickerVisible,
            yearPickerText = formattedMonthYear,
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
            onYearPickerButtonClicked = { yearPickerVisible = !yearPickerVisible },
            colors = colors
        )
        Box {
            Column(modifier = Modifier.padding(horizontal = DatePickerHorizontalPadding)) {
                NepaliWeekDays(colors = colors, language = chosenLanguage)
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
    val yearRange: IntRange
    val nepaliSelectableDates: NepaliSelectableDates
    val locale: NepaliDateLocale
}

@Stable
interface NepaliSelectableDates {

    /**
     * Returns true if the date item representing the [customCalendar] should be enabled for
     * selection in the UI.
     */
    fun isSelectableDate(customCalendar: CustomCalendar) = true

    /**
     * Returns true if a given [year] should be enabled for selection in the UI. When a year is
     * defined as non selectable, all the dates in that year will also be non selectable.
     */
    fun isSelectableYear(year: Int) = true
}

/**
 * Creates a [NepaliDatePickerState] for a [NepaliDatePicker] that is remembered across compositions.
 *
 * To create a date picker state outside composition, see the `DatePickerState` function.
 *
 * @param initialSelectedDate [SimpleDate] that represents an initial selection of a date.
 * Provide a `null` to indicate no selection.
 * @param initialDisplayedMonth [SimpleDate] that represents an initial selection of a month
 * to be displayed to the user. By default, in case an `initialSelectedDate` is provided, the
 * initial displayed month would be the month of the selected date. Otherwise, in case `null`
 * is provided, the displayed month would be the current one.
 * @param yearRange an [IntRange] that holds the year range that the date picker will be limited to
 * @param nepaliSelectableDates a [NepaliSelectableDates] that is consulted to check if a date is
 * allowed. In case a date is not allowed to be selected, it will appear disabled in the UI.
 * @param locale an instance of [NepaliDateLocale] that is used to localize the date picker. It holds
 * the preference for the date picker formatted date and the language of the date picker.
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
 * NepaliDatePicker(state = defaultNepaliDatePickerState)
 *
 * NepaliDatePicker(
 *     state = customizedDatePickerState,
 *     colors = NepaliDatePickerDefaults.colors().copy(
 *         containerColor = MaterialTheme.colorScheme.surface
 *     )
 * )
 * ```
 */
@Composable
fun rememberNepaliDatePickerState(
    initialSelectedDate: SimpleDate? = null,
    initialDisplayedMonth: SimpleDate? = initialSelectedDate,
    yearRange: IntRange = NepaliDatePickerDefaults.NepaliYearRange,
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
            nepaliSelectableDates = nepaliSelectableDates,
            locale = locale
        )
    }
}

internal fun NepaliDatePickerState(
    initialSelectedDate: SimpleDate? = null,
    initialDisplayedMonth: SimpleDate? = initialSelectedDate,
    yearRange: IntRange = NepaliDatePickerDefaults.NepaliYearRange,
    nepaliSelectableDates: NepaliSelectableDates = NepaliDatePickerDefaults.AllDates,
    locale: NepaliDateLocale
): NepaliDatePickerState = NepaliDatePickerStateImpl(
    initialSelectedDate = initialSelectedDate,
    initialDisplayedMonth = initialDisplayedMonth,
    yearRange = yearRange,
    nepaliSelectableDates = nepaliSelectableDates,
    locale = locale
)

/**
 * An abstract for the date pickers states.
 *
 * This base class common state properties and provides a base implementation that is extended by
 * the different state classes.
 *
 * @param initialDisplayedMonth timestamp in _UTC_ milliseconds from the epoch that
 * represents an initial selection of a month to be displayed to the user. In case `null` is
 * provided, the displayed month would be the current one.
 * @param yearRange an [IntRange] that holds the year range that the date picker will be limited
 * to
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

    private var _displayedMonth = mutableStateOf(if (initialDisplayedMonth != null) {
        val month = calendarModel.getNepaliMonth(
            nepaliYear = initialDisplayedMonth.year, nepaliMonth = initialDisplayedMonth.month
        )
        require(yearRange.contains(month.year)) {
            "The initial display month's year (${month.year}) is out of the years range of $yearRange."
        }
        month
    } else {
        // Set the displayed month to the current one.
        calendarModel.today.toNepaliMonthCalendar()
    })

    var displayedMonth: NepaliMonthCalendar
        get() = _displayedMonth.value
        set(simpleDate) {
            val month = calendarModel.getNepaliMonth(
                nepaliYear = simpleDate.year, nepaliMonth = simpleDate.month
            )
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
    private var _selectedDate = mutableStateOf(if (initialSelectedDate != null) {
        val date = calendarModel.getNepaliMonth(simpleNepaliDate = initialSelectedDate)
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

    companion object {
        // Initialize Json with ignoreUnknownKeys = true to handle changes in SimpleDate's structure
        // and prevent crashes due to missing properties during deserialization.
        val json = Json { ignoreUnknownKeys = true }

        fun Saver(
            nepaliSelectableDates: NepaliSelectableDates, locale: NepaliDateLocale
        ): Saver<NepaliDatePickerStateImpl, Any> = listSaver(save = { state ->
            listOf(
                state.selectedDate?.let { json.encodeToString(it) }, // Save the encoded date or null
                Json.encodeToString(state.displayedMonth),
                state.yearRange.first,
                state.yearRange.last
            )
        }, restore = { value ->
            NepaliDatePickerStateImpl(
                initialSelectedDate = if (value.isNotEmpty()) decodeDateOrNull(value[0] as? String) else null,
                initialDisplayedMonth = if (value.size > 1) decodeDateOrNull(value[1] as? String) else null,
                yearRange = IntRange(value[2] as Int, value[3] as Int),
                nepaliSelectableDates = nepaliSelectableDates,
                locale = locale
            )
        })

        @Stable
        private fun decodeDateOrNull(dateString: String?): SimpleDate? {
            return when {
                dateString == null -> null
                else -> json.decodeFromString<SimpleDate>(dateString)
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
    previousAvailable: Boolean,
    yearPickerVisible: Boolean,
    yearPickerText: String,
    onNextClicked: () -> Unit,
    onPreviousClicked: () -> Unit,
    onYearPickerButtonClicked: () -> Unit,
    colors: NepaliDatePickerColors
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
    colors: NepaliDatePickerColors, language: NepaliDatePickerLang
) {
    val firstDayOfWeek = NepaliDatePickerDefaults.FIRST_DAY_OF_WEEK
    val weekdays = language.weekdays
    val dayNames = arrayListOf<String>()

    for (i in firstDayOfWeek..weekdays.size) {
        dayNames.add(weekdays[i - 1].short)
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

@OptIn(ExperimentalFoundationApi::class)
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
                                calendarModel.getNepaliMonth(
                                    SimpleDate(
                                        year = monthCalendar.year,
                                        month = monthCalendar.month,
                                        dayOfMonth = dayNumber
                                    )
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
                            Text(
                                modifier = Modifier,
                                textAlign = TextAlign.Center,
                                text = calendarModel.localizeNumber(
                                    stringToLocalize = dayNumber.toString(), locale = chosenLanguage
                                ),
                                style = MaterialTheme.typography.bodyLarge
                            )
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
        shape = CircleShape,
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

private const val NepaliDaysInWeek: Int = 7

private const val NepaliMaxCalendarRows = 6
private const val NepaliYearsInRow: Int = 3

private val DateStateLayerWidth = 40.0.dp
private val DateStateLayerHeight = 40.0.dp
private val SelectionYearContainerWidth = 72.0.dp
private val SelectionYearContainerHeight = 36.0.dp
internal val ContainerWidth = 360.0.dp
internal val ContainerHeight = 568.0.dp
private val RecommendedSizeForAccessibility = 48.dp
private val MonthYearHeight = 56.dp
private val YearsVerticalPadding = 16.dp
private val HeaderContainerHeight = 120.0.dp
private val DateTodayContainerOutlineWidth = 1.0.dp

private val DatePickerHorizontalPadding = 12.dp
private val YearPickerContentBottomPadding = 8.dp
private val NepaliDatePickerTitlePadding = PaddingValues(start = 24.dp, end = 12.dp, top = 16.dp)
private val NepaliDatePickerHeadlinePadding =
    PaddingValues(start = 24.dp, end = 12.dp, bottom = 12.dp)