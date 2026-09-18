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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.CalendarViewAdapter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerColors
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.calendarViewAdapter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.rememberCalendarViewAdapter
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate

/**
 * A wheel / scroll date picker for Bikram Sambat dates - three snapping columns (Year, Month, Day).
 *
 * This is a lighter-weight alternative to the calendar-grid [NepaliDatePicker]: it is the pattern
 * users reach for when entering a birth date or a date far from today, and it is the native feel on
 * iOS. It also sidesteps the grid's heaviest paths - there is no ~1,500-item month pager and no
 * per-cell BS to AD conversion; each column is a bounded [LazyColumn] reading the day-count table
 * directly, so the day column always shows the correct 29 to 32 days for the chosen month.
 *
 * The composable is always in a selected state (a wheel cannot be "empty"); [onDateChange] fires
 * with the resolved [CustomCalendar] whenever the selection settles on a new date.
 *
 * The wheels can spin in either calendar. [onDateChange] always reports a Bikram Sambat
 * [CustomCalendar], whichever calendar is on the wheels, so switching keeps the same day selected.
 *
 * @param modifier the [Modifier] applied to the picker surface.
 * @param initialDate the Bikram Sambat [SimpleDate] shown centered on first composition. Defaults to today.
 * @param yearRange the selectable Bikram Sambat year range. A Gregorian wheel derives its own range
 *   from this so both calendars cover the same span of real days.
 * @param locale the [NepaliDateLocale] controlling language, month names, and digit script.
 * @param selectableDates consulted via [NepaliSelectableDates.isSelectableYear] to drop non-selectable
 *   years from the year wheel. Fine-grained per-day disabling is not expressed by a wheel by design.
 * @param colors the [NepaliDatePickerColors] used to theme the picker.
 * @param itemHeight the height of each wheel row.
 * @param visibleItemCount how many rows are visible at once; coerced to an odd number of at least 3.
 * @param shape the [Shape] of the wheel surface.
 * @param selectedTextStyle the [TextStyle] of the centered (selected) row.
 * @param unselectedTextStyle the [TextStyle] of the non-centered rows.
 * @param initialCalendarSystem the calendar the wheels start in.
 * @param showCalendarSystemToggle shows a `B.S.` / `A.D.` switch above the wheels. Unlike the grid
 *   pickers the wheel has no external state holder, so it owns the choice itself.
 * @param onDateChange invoked with the resolved Bikram Sambat [CustomCalendar] when the selected
 *   date changes.
 *
 * Example usage:
 * ```
 * NepaliWheelDatePicker(
 *     initialDate = NepaliDateConverter.todayNepaliSimpleDate,
 *     onDateChange = { selected -> /* ... */ }
 * )
 * ```
 *
 * @see NepaliDatePicker
 */
@ExperimentalNepaliDatePickerApi
@Composable
fun NepaliWheelDatePicker(
    modifier: Modifier = Modifier,
    initialDate: SimpleDate = NepaliDateConverter.todayNepaliSimpleDate,
    yearRange: IntRange = NepaliCalendarDefaults.NepaliYearRange,
    locale: NepaliDateLocale = NepaliDatePickerDefaults.DefaultLocale,
    selectableDates: NepaliSelectableDates = NepaliDatePickerDefaults.AllDates,
    colors: NepaliDatePickerColors = NepaliDatePickerDefaults.colors(),
    itemHeight: Dp = WheelItemHeight,
    visibleItemCount: Int = WheelVisibleCount,
    shape: Shape = RoundedCornerShape(WheelCornerRadius),
    selectedTextStyle: TextStyle = MaterialTheme.typography.titleMedium,
    unselectedTextStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    initialCalendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT,
    showCalendarSystemToggle: Boolean = false,
    onDateChange: (CustomCalendar) -> Unit
) {
    val calendarModel = remember(locale) { NepaliCalendarModel(locale) }

    // Saved as the era rather than the ordinal so reordering the enum cannot silently restore the
    // wrong calendar.
    var calendarSystemEra by rememberSaveable { mutableIntStateOf(initialCalendarSystem.era) }
    val calendarSystem = CalendarSystem.fromEra(calendarSystemEra)
        ?: CalendarSystem.BIKRAM_SAMBAT
    val adapter = rememberCalendarViewAdapter(calendarSystem, calendarModel, yearRange)

    // Keep an odd count of at least 3 rows so a single center row is always well-defined.
    val visibleCount = visibleItemCount.coerceAtLeast(3).let { if (it % 2 == 0) it + 1 else it }

    // Year wheel entries - drop non-selectable years, but never present an empty wheel. In a
    // Gregorian wheel a year survives while either Bikram Sambat year it straddles is selectable.
    val yearList = remember(adapter, selectableDates) {
        adapter.yearRange
            .filter { year ->
                adapter.canonicalYearsIn(year).any { selectableDates.isSelectableYear(it) }
            }
            .ifEmpty { adapter.yearRange.toList() }
    }

    // The wheels spin in the displayed calendar; the date the caller gets is always Bikram Sambat.
    val initialDisplayedDate = remember(adapter, initialDate) {
        adapter.wheelPositionFor(initialDate, calendarModel)
    }

    var selectedYear by rememberSaveable {
        mutableIntStateOf(initialDisplayedDate.year.coerceIn(yearList.first(), yearList.last()))
    }
    var selectedMonth by rememberSaveable {
        mutableIntStateOf(initialDisplayedDate.month.coerceIn(1, MonthsInYear))
    }
    var selectedDay by rememberSaveable {
        mutableIntStateOf(initialDisplayedDate.dayOfMonth.coerceAtLeast(1))
    }

    val daysInMonth = remember(adapter, selectedYear, selectedMonth) {
        adapter.monthOf(selectedYear, selectedMonth).totalDaysInMonth
    }

    // A shorter month must not keep a stale higher day (e.g. leaving day 32 after switching months).
    LaunchedEffect(daysInMonth) {
        if (selectedDay > daysInMonth) selectedDay = daysInMonth
    }

    val clampedDay = selectedDay.coerceIn(1, daysInMonth)

    // Emit the resolved Bikram Sambat calendar whenever the settled selection changes. A Gregorian
    // day before the conversion anchor has no Bikram Sambat equivalent, so nothing is emitted.
    LaunchedEffect(adapter, selectedYear, selectedMonth, clampedDay) {
        adapter.canonicalDateAt(selectedYear, selectedMonth, clampedDay)?.let(onDateChange)
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = colors.containerColor
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (showCalendarSystemToggle) {
                NepaliCalendarSystemToggle(
                    calendarSystem = calendarSystem,
                    onCalendarSystemChange = { newSystem ->
                        // Re-anchor on the current selection so the switch keeps the same day. A
                        // Gregorian position before the conversion anchor has no Bikram Sambat date
                        // to carry over, so fall back to the start of the range rather than leaving
                        // the old calendar's numbers under the new calendar's labels.
                        val anchor = adapter
                            .canonicalDateAt(selectedYear, selectedMonth, clampedDay)
                            ?.toSimpleDate()
                            ?: SimpleDate(yearRange.first, 1, 1)
                        calendarSystemEra = newSystem.era

                        val moved = calendarViewAdapter(newSystem, calendarModel, yearRange)
                            .wheelPositionFor(anchor, calendarModel)
                        selectedYear = moved.year
                        selectedMonth = moved.month
                        selectedDay = moved.dayOfMonth
                    },
                    modifier = Modifier.padding(top = WheelTogglePadding),
                    language = locale.language,
                    colors = colors
                )
            }

            Box(contentAlignment = Alignment.Center) {
                // Center selection band drawn behind the wheels. A soft inset border defines the selected
                // row without full-width rules, which would overshoot the rounded band and read as clutter.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = WheelBandHorizontalPadding)
                        .height(itemHeight)
                        .background(
                            color = colors.selectedDayContainerColor.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(WheelBandCornerRadius)
                        )
                        .border(
                            width = 1.dp,
                            color = colors.dividerColor,
                            shape = RoundedCornerShape(WheelBandCornerRadius)
                        )
                )

                // A switch changes what every column holds and how many rows it has, so their
                // scroll state cannot be carried over; `key` rebuilds each one at the new index.
                // Reusing it lets a LazyColumn measure against content it no longer has.
                key(calendarSystem) {
                Row(
                    modifier = Modifier.padding(horizontal = WheelBandHorizontalPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WheelColumn(
                        itemCount = yearList.size,
                        selectedIndex = yearList.indexOf(selectedYear).coerceAtLeast(0),
                        onSelectedIndexChange = { selectedYear = yearList[it] },
                        itemLabel = { index ->
                            calendarModel.localizeNumber(yearList[index].toString(), locale.language)
                        },
                        colors = colors,
                        itemHeight = itemHeight,
                        visibleCount = visibleCount,
                        selectedTextStyle = selectedTextStyle,
                        unselectedTextStyle = unselectedTextStyle,
                        modifier = Modifier.weight(1.1f)
                    )
                    WheelColumn(
                        itemCount = MonthsInYear,
                        selectedIndex = selectedMonth - 1,
                        onSelectedIndexChange = { selectedMonth = it + 1 },
                        itemLabel = { index ->
                            adapter.monthName(index + 1, locale.language, locale.monthName)
                        },
                        colors = colors,
                        itemHeight = itemHeight,
                        visibleCount = visibleCount,
                        selectedTextStyle = selectedTextStyle,
                        unselectedTextStyle = unselectedTextStyle,
                        modifier = Modifier.weight(1.5f)
                    )
                    WheelColumn(
                        itemCount = daysInMonth,
                        selectedIndex = clampedDay - 1,
                        onSelectedIndexChange = { selectedDay = it + 1 },
                        itemLabel = { index ->
                            calendarModel.localizeNumber((index + 1).toString(), locale.language)
                        },
                        colors = colors,
                        itemHeight = itemHeight,
                        visibleCount = visibleCount,
                        selectedTextStyle = selectedTextStyle,
                        unselectedTextStyle = unselectedTextStyle,
                        modifier = Modifier.weight(1f)
                    )
                }
                }
            }
        }
    }
}

/**
 * A single snapping wheel column. Reports the settled centered index and reflects external changes
 * (e.g. a day clamp) without fighting an in-progress drag.
 *
 * The centered item is derived from the scroll position rounded to the nearest item, which is
 * padding-independent and correct once [rememberSnapFlingBehavior] settles the fling.
 */
@Composable
private fun WheelColumn(
    itemCount: Int,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    itemLabel: (Int) -> String,
    colors: NepaliDatePickerColors,
    selectedTextStyle: TextStyle,
    unselectedTextStyle: TextStyle,
    modifier: Modifier = Modifier,
    visibleCount: Int = WheelVisibleCount,
    itemHeight: Dp = WheelItemHeight
) {
    val lastIndex = (itemCount - 1).coerceAtLeast(0)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedIndex.coerceIn(0, lastIndex)
    )
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val halfItemPx = with(LocalDensity.current) { itemHeight.roundToPx() / 2 }

    val centeredIndex by remember(itemCount) {
        derivedStateOf {
            val first = listState.firstVisibleItemIndex
            val rounded = if (listState.firstVisibleItemScrollOffset > halfItemPx) first + 1 else first
            rounded.coerceIn(0, lastIndex)
        }
    }

    // Report the selection once the wheel settles (not on every intermediate frame of a fling).
    LaunchedEffect(listState, itemCount) {
        snapshotFlow { listState.isScrollInProgress }.collect { scrolling ->
            if (!scrolling && centeredIndex != selectedIndex) onSelectedIndexChange(centeredIndex)
        }
    }

    // Reflect an externally driven selection change (e.g. day clamped after a month change).
    LaunchedEffect(selectedIndex) {
        if (!listState.isScrollInProgress && centeredIndex != selectedIndex) {
            listState.scrollToItem(selectedIndex.coerceIn(0, lastIndex))
        }
    }

    LazyColumn(
        modifier = modifier.height(itemHeight * visibleCount),
        state = listState,
        flingBehavior = flingBehavior,
        contentPadding = PaddingValues(vertical = itemHeight * (visibleCount / 2)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(count = itemCount, key = { it }) { index ->
            val selected = index == centeredIndex
            Box(
                modifier = Modifier.height(itemHeight).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = itemLabel(index),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    color = if (selected) {
                        colors.dayContentColor
                    } else {
                        colors.dayContentColor.copy(alpha = WheelUnselectedAlpha)
                    },
                    style = if (selected) selectedTextStyle else unselectedTextStyle
                )
            }
        }
    }
}

/**
 * The Bikram Sambat date the wheels are on, or `null` when that position has none: a Gregorian day
 * before the conversion anchor, or a day the table does not cover.
 *
 * `parse` reports the second case as `totalDaysInMonth = -1` rather than as null, so that sentinel
 * is filtered here instead of reaching the caller dressed as a real date.
 */
private fun CalendarViewAdapter.canonicalDateAt(
    year: Int,
    month: Int,
    dayOfMonth: Int
): CustomCalendar? = parse(
    buildString {
        append(year.toString().padStart(4, '0'))
        append(month.toString().padStart(2, '0'))
        append(dayOfMonth.toString().padStart(2, '0'))
    }
)?.takeIf { it.totalDaysInMonth > 0 }?.let { toCanonical(it) }

/**
 * Where the wheels should sit to show [canonicalDate] in this adapter's calendar.
 *
 * Falls back to the first of whatever month the date lands in when the date itself cannot be shown
 * there, so the wheels never rest on a position this calendar has no number for.
 */
private fun CalendarViewAdapter.wheelPositionFor(
    canonicalDate: SimpleDate,
    calendarModel: NepaliCalendarModel
): SimpleDate {
    val month = monthContaining(canonicalDate)
    val displayed = runCatching { calendarModel.getNepaliCalendar(canonicalDate) }
        .getOrNull()
        ?.let { fromCanonical(it) }
        ?.takeIf { it.year == month.year && it.month == month.month }
    return SimpleDate(
        year = month.year,
        month = month.month,
        dayOfMonth = displayed?.dayOfMonth ?: 1
    )
}

private val WheelItemHeight: Dp = 44.dp
private val WheelTogglePadding: Dp = 12.dp
private const val MonthsInYear: Int = 12
private const val WheelVisibleCount: Int = 5
private const val WheelUnselectedAlpha: Float = 0.38f
private val WheelCornerRadius: Dp = 20.dp
private val WheelBandCornerRadius: Dp = 12.dp
private val WheelBandHorizontalPadding: Dp = 16.dp
