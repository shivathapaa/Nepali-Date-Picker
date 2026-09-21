/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepickerkmp

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The day-paired reading of a Gregorian month, which is the form every platform gets and the only
 * one Swift gets. These hold it to the list it restates: the same days, in the same order, with the
 * same values present and absent.
 */
class NepaliEnglishMonthDayTests {

    private val firstConvertible = NepaliCalendarDefaults.minConvertibleEnglishDate

    @Test
    fun pairsEveryDayOfTheMonthInOrder() {
        val paired = NepaliDateConverter.getNepaliCalendarsInEnglishMonthByDay(2024, 9)

        assertEquals(30, paired.size)
        assertEquals((1..30).toList(), paired.map { it.englishDayOfMonth })
    }

    @Test
    fun carriesTheSameCalendarsAsTheListItRestates() {
        val listed = NepaliDateConverter.getNepaliCalendarsInEnglishMonth(2024, 9)
        val paired = NepaliDateConverter.getNepaliCalendarsInEnglishMonthByDay(2024, 9)

        assertEquals(listed, paired.map { it.nepaliCalendar })
    }

    @Test
    fun aMonthEveryDayOfWhichConvertsCarriesNoGap() {
        val paired = NepaliDateConverter.getNepaliCalendarsInEnglishMonthByDay(2024, 9)

        assertTrue(paired.all { it.nepaliCalendar != null })
    }

    @Test
    fun aDayBeforeTheFirstConvertibleDateCarriesNothing() {
        val paired = NepaliDateConverter.getNepaliCalendarsInEnglishMonthByDay(
            firstConvertible.year, firstConvertible.month
        )

        val before = paired.first { it.englishDayOfMonth < firstConvertible.dayOfMonth }
        val onward = paired.first { it.englishDayOfMonth == firstConvertible.dayOfMonth }

        assertNull(before.nepaliCalendar, "a day the table does not reach has no equivalent")
        assertNotNull(onward.nepaliCalendar, "the first convertible day has one")
    }

    @Test
    fun theDayItCarriesIsTheDayItConverted() {
        val paired = NepaliDateConverter.getNepaliCalendarsInEnglishMonthByDay(2024, 9)

        for (day in paired) {
            val nepali = assertNotNull(day.nepaliCalendar)
            val converted = NepaliDateConverter.convertEnglishToNepali(2024, 9, day.englishDayOfMonth)
            assertEquals(converted, nepali, "day ${day.englishDayOfMonth}")
        }
    }

    @Test
    fun aMonthOfThirtyOneAndAFebruaryAreBothCoveredWhole() {
        assertEquals(31, NepaliDateConverter.getNepaliCalendarsInEnglishMonthByDay(2024, 1).size)
        assertEquals(29, NepaliDateConverter.getNepaliCalendarsInEnglishMonthByDay(2024, 2).size)
        assertEquals(28, NepaliDateConverter.getNepaliCalendarsInEnglishMonthByDay(2023, 2).size)
    }
}
