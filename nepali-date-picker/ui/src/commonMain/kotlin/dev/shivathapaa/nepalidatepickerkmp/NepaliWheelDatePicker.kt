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
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerColors
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate

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
 * @param modifier the [Modifier] applied to the picker surface.
 * @param initialDate the [SimpleDate] shown centered on first composition. Defaults to today.
 * @param yearRange the selectable Bikram Sambat year range.
 * @param locale the [NepaliDateLocale] controlling language, month names, and digit script.
 * @param selectableDates consulted via [NepaliSelectableDates.isSelectableYear] to drop non-selectable
 *   years from the year wheel. Fine-grained per-day disabling is not expressed by a wheel by design.
 * @param colors the [NepaliDatePickerColors] used to theme the picker.
 * @param itemHeight the height of each wheel row.
 * @param visibleItemCount how many rows are visible at once; coerced to an odd number of at least 3.
 * @param shape the [Shape] of the wheel surface.
 * @param selectedTextStyle the [TextStyle] of the centered (selected) row.
 * @param unselectedTextStyle the [TextStyle] of the non-centered rows.
 * @param onDateChange invoked with the resolved [CustomCalendar] when the selected date changes.
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
    onDateChange: (CustomCalendar) -> Unit
) {
    val calendarModel = remember(locale) { NepaliCalendarModel(locale) }

    // Keep an odd count of at least 3 rows so a single center row is always well-defined.
    val visibleCount = visibleItemCount.coerceAtLeast(3).let { if (it % 2 == 0) it + 1 else it }

    // Year wheel entries - drop non-selectable years, but never present an empty wheel.
    val yearList = remember(yearRange, selectableDates) {
        yearRange.filter { selectableDates.isSelectableYear(it) }.ifEmpty { yearRange.toList() }
    }

    var selectedYear by rememberSaveable {
        mutableIntStateOf(initialDate.year.coerceIn(yearList.first(), yearList.last()))
    }
    var selectedMonth by rememberSaveable { mutableIntStateOf(initialDate.month.coerceIn(1, 12)) }
    var selectedDay by rememberSaveable { mutableIntStateOf(initialDate.dayOfMonth.coerceAtLeast(1)) }

    val daysInMonth = remember(selectedYear, selectedMonth) {
        calendarModel.getTotalDaysInNepaliMonth(selectedYear, selectedMonth)
    }

    // A shorter month must not keep a stale higher day (e.g. leaving day 32 after switching months).
    LaunchedEffect(daysInMonth) {
        if (selectedDay > daysInMonth) selectedDay = daysInMonth
    }

    val clampedDay = selectedDay.coerceIn(1, daysInMonth)

    // Emit the resolved calendar whenever the settled selection changes.
    LaunchedEffect(selectedYear, selectedMonth, clampedDay) {
        runCatching {
            calendarModel.getNepaliCalendar(SimpleDate(selectedYear, selectedMonth, clampedDay))
        }.onSuccess(onDateChange)
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = colors.containerColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Center selection band drawn behind the wheels.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WheelBandHorizontalPadding)
                    .height(itemHeight)
                    .background(
                        color = colors.selectedDayContainerColor.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(WheelBandCornerRadius)
                    )
            )
            HorizontalDivider(
                modifier = Modifier.padding(bottom = itemHeight),
                color = colors.dividerColor
            )
            HorizontalDivider(
                modifier = Modifier.padding(top = itemHeight),
                color = colors.dividerColor
            )

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
                    itemCount = 12,
                    selectedIndex = selectedMonth - 1,
                    onSelectedIndexChange = { selectedMonth = it + 1 },
                    itemLabel = { index ->
                        calendarModel.getNepaliMonthName(index + 1, locale.monthName, locale.language)
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

private val WheelItemHeight: Dp = 44.dp
private const val WheelVisibleCount: Int = 5
private const val WheelUnselectedAlpha: Float = 0.38f
private val WheelCornerRadius: Dp = 20.dp
private val WheelBandCornerRadius: Dp = 12.dp
private val WheelBandHorizontalPadding: Dp = 16.dp
