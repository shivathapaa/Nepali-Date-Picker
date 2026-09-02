/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package sample.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliSelectableDates
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import dev.shivathapaa.nepalidatepickerkmp.rememberNepaliDatePickerState

/** Constraining which dates can be picked, from the built-in factories to fully custom rules. */
@Composable
fun SelectableDatesShowcase(modifier: Modifier = Modifier) {
    ShowcaseColumn(modifier) {
        val today = remember { NepaliDateConverter.todayNepaliSimpleDate }
        val inTwentyDays = remember(today) {
            NepaliDateConverter
                .getNepaliCalendarAfterAdditionOrSubtraction(today.year, today.month, today.dayOfMonth, 20)
                .toSimpleDate()
        }

        DemoSection(
            "On or before today",
            "BeforeDateSelectable disables everything after the anchor. Useful for birth dates."
        ) {
            val selectable = remember(today) {
                NepaliDateConverter.BeforeDateSelectable(today, includeDate = true)
            }
            NepaliDatePicker(state = rememberNepaliDatePickerState(nepaliSelectableDates = selectable))
        }

        DemoSection(
            "On or after today",
            "AfterDateSelectable disables the past. Useful for booking and deadlines."
        ) {
            val selectable = remember(today) {
                NepaliDateConverter.AfterDateSelectable(today, includeDate = true)
            }
            NepaliDatePicker(state = rememberNepaliDatePickerState(nepaliSelectableDates = selectable))
        }

        DemoSection(
            "Within a range",
            "DateRangeSelectable keeps selection inside a window, here today through 20 days out."
        ) {
            val selectable = remember(today, inTwentyDays) {
                NepaliDateConverter.DateRangeSelectable(
                    minDate = today,
                    maxDate = inTwentyDays,
                    includeMinDate = true,
                    includeMaxDate = true
                )
            }
            NepaliDatePicker(state = rememberNepaliDatePickerState(nepaliSelectableDates = selectable))
        }

        DemoSection(
            "Custom rule: no Saturdays",
            "Implement NepaliSelectableDates for arbitrary rules. Here Saturday (day 7) is disabled."
        ) {
            val noSaturdays = remember {
                object : NepaliSelectableDates {
                    override fun isSelectableDate(customCalendar: CustomCalendar): Boolean =
                        customCalendar.dayOfWeek != 7
                }
            }
            NepaliDatePicker(state = rememberNepaliDatePickerState(nepaliSelectableDates = noSaturdays))
        }

        DemoSection(
            "Custom rule: block specific holidays",
            "A rule can consult any data source. Here a fixed set of dates is disabled."
        ) {
            val holidays = remember(today) {
                setOf(
                    offsetDate(today, 3),
                    offsetDate(today, 4),
                    offsetDate(today, 9)
                )
            }
            val avoidHolidays = remember(holidays) {
                object : NepaliSelectableDates {
                    override fun isSelectableDate(customCalendar: CustomCalendar): Boolean =
                        SimpleDate(customCalendar.year, customCalendar.month, customCalendar.dayOfMonth) !in holidays
                }
            }
            NepaliDatePicker(state = rememberNepaliDatePickerState(nepaliSelectableDates = avoidHolidays))
        }

        DemoSection(
            "Custom rule: restrict the year picker",
            "isSelectableYear filters the year list. Here only a five-year window is offered."
        ) {
            val window = remember(today) {
                object : NepaliSelectableDates {
                    override fun isSelectableYear(year: Int): Boolean = year in (today.year - 2)..(today.year + 2)
                }
            }
            NepaliDatePicker(state = rememberNepaliDatePickerState(nepaliSelectableDates = window))
        }
    }
}

private fun offsetDate(from: SimpleDate, days: Int): SimpleDate =
    NepaliDateConverter
        .getNepaliCalendarAfterAdditionOrSubtraction(from.year, from.month, from.dayOfMonth, days)
        .toSimpleDate()
