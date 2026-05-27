/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 */

package dev.shivathapaa.nepalidatepickerkmp

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SelectableDatesTests {

    private val anchor = SimpleDate(2081, 5, 24)
    private val before = NepaliDateConverter.getNepaliCalendar(2081, 5, 23)
    private val sameDay = NepaliDateConverter.getNepaliCalendar(2081, 5, 24)
    private val after = NepaliDateConverter.getNepaliCalendar(2081, 5, 25)
    private val differentMonthEarlier = NepaliDateConverter.getNepaliCalendar(2081, 4, 1)
    private val differentMonthLater = NepaliDateConverter.getNepaliCalendar(2081, 6, 1)
    private val differentYearEarlier = NepaliDateConverter.getNepaliCalendar(2080, 1, 1)
    private val differentYearLater = NepaliDateConverter.getNepaliCalendar(2082, 1, 1)

    // ── BeforeDateSelectable ─────────────────────────────────────────────────

    @Test
    fun beforeDateSelectable_excludeAnchor_dayBeforeIsSelectable() {
        val selectable = NepaliDateConverter.BeforeDateSelectable(anchor, includeDate = false)
        assertTrue(selectable.isSelectableDate(before))
        assertTrue(selectable.isSelectableDate(differentMonthEarlier))
        assertTrue(selectable.isSelectableDate(differentYearEarlier))
    }

    @Test
    fun beforeDateSelectable_excludeAnchor_anchorAndAfterRejected() {
        val selectable = NepaliDateConverter.BeforeDateSelectable(anchor, includeDate = false)
        assertFalse(selectable.isSelectableDate(sameDay))
        assertFalse(selectable.isSelectableDate(after))
        assertFalse(selectable.isSelectableDate(differentMonthLater))
        assertFalse(selectable.isSelectableDate(differentYearLater))
    }

    @Test
    fun beforeDateSelectable_includeAnchor_anchorIsSelectable() {
        val selectable = NepaliDateConverter.BeforeDateSelectable(anchor, includeDate = true)
        assertTrue(selectable.isSelectableDate(sameDay))
        assertTrue(selectable.isSelectableDate(before))
        assertFalse(selectable.isSelectableDate(after))
    }

    @Test
    fun beforeDateSelectable_isSelectableYear_allowsAnchorYearAndEarlier() {
        val selectable = NepaliDateConverter.BeforeDateSelectable(anchor, includeDate = false)
        assertTrue(selectable.isSelectableYear(2081))
        assertTrue(selectable.isSelectableYear(2080))
        assertFalse(selectable.isSelectableYear(2082))
    }

    // ── AfterDateSelectable ──────────────────────────────────────────────────

    @Test
    fun afterDateSelectable_excludeAnchor_dayAfterIsSelectable() {
        val selectable = NepaliDateConverter.AfterDateSelectable(anchor, includeDate = false)
        assertTrue(selectable.isSelectableDate(after))
        assertTrue(selectable.isSelectableDate(differentMonthLater))
        assertTrue(selectable.isSelectableDate(differentYearLater))
    }

    @Test
    fun afterDateSelectable_excludeAnchor_anchorAndBeforeRejected() {
        val selectable = NepaliDateConverter.AfterDateSelectable(anchor, includeDate = false)
        assertFalse(selectable.isSelectableDate(sameDay))
        assertFalse(selectable.isSelectableDate(before))
        assertFalse(selectable.isSelectableDate(differentMonthEarlier))
        assertFalse(selectable.isSelectableDate(differentYearEarlier))
    }

    @Test
    fun afterDateSelectable_includeAnchor_anchorIsSelectable() {
        val selectable = NepaliDateConverter.AfterDateSelectable(anchor, includeDate = true)
        assertTrue(selectable.isSelectableDate(sameDay))
        assertTrue(selectable.isSelectableDate(after))
        assertFalse(selectable.isSelectableDate(before))
    }

    @Test
    fun afterDateSelectable_isSelectableYear_allowsAnchorYearAndLater() {
        val selectable = NepaliDateConverter.AfterDateSelectable(anchor, includeDate = false)
        assertTrue(selectable.isSelectableYear(2081))
        assertTrue(selectable.isSelectableYear(2082))
        assertFalse(selectable.isSelectableYear(2080))
    }

    // ── DateRangeSelectable ──────────────────────────────────────────────────

    @Test
    fun dateRangeSelectable_excludeBothEnds_onlyInteriorSelectable() {
        val min = SimpleDate(2081, 5, 1)
        val max = SimpleDate(2081, 5, 31)
        val selectable = NepaliDateConverter.DateRangeSelectable(min, max,
            includeMinDate = false, includeMaxDate = false)

        assertFalse(selectable.isSelectableDate(NepaliDateConverter.getNepaliCalendar(2081, 5, 1)))
        assertTrue(selectable.isSelectableDate(NepaliDateConverter.getNepaliCalendar(2081, 5, 2)))
        assertTrue(selectable.isSelectableDate(NepaliDateConverter.getNepaliCalendar(2081, 5, 30)))
        assertFalse(selectable.isSelectableDate(NepaliDateConverter.getNepaliCalendar(2081, 5, 31)))
    }

    @Test
    fun dateRangeSelectable_includeBothEnds_endpointsAreSelectable() {
        val min = SimpleDate(2081, 5, 1)
        val max = SimpleDate(2081, 5, 31)
        val selectable = NepaliDateConverter.DateRangeSelectable(min, max,
            includeMinDate = true, includeMaxDate = true)

        assertTrue(selectable.isSelectableDate(NepaliDateConverter.getNepaliCalendar(2081, 5, 1)))
        assertTrue(selectable.isSelectableDate(NepaliDateConverter.getNepaliCalendar(2081, 5, 15)))
        assertTrue(selectable.isSelectableDate(NepaliDateConverter.getNepaliCalendar(2081, 5, 31)))
    }

    @Test
    fun dateRangeSelectable_includeMinExcludeMax() {
        val min = SimpleDate(2081, 5, 1)
        val max = SimpleDate(2081, 5, 31)
        val selectable = NepaliDateConverter.DateRangeSelectable(min, max,
            includeMinDate = true, includeMaxDate = false)

        assertTrue(selectable.isSelectableDate(NepaliDateConverter.getNepaliCalendar(2081, 5, 1)))
        assertFalse(selectable.isSelectableDate(NepaliDateConverter.getNepaliCalendar(2081, 5, 31)))
    }

    @Test
    fun dateRangeSelectable_excludeMinIncludeMax() {
        val min = SimpleDate(2081, 5, 1)
        val max = SimpleDate(2081, 5, 31)
        val selectable = NepaliDateConverter.DateRangeSelectable(min, max,
            includeMinDate = false, includeMaxDate = true)

        assertFalse(selectable.isSelectableDate(NepaliDateConverter.getNepaliCalendar(2081, 5, 1)))
        assertTrue(selectable.isSelectableDate(NepaliDateConverter.getNepaliCalendar(2081, 5, 31)))
    }

    @Test
    fun dateRangeSelectable_outsideRangeRejected() {
        val min = SimpleDate(2081, 5, 1)
        val max = SimpleDate(2081, 5, 31)
        val selectable = NepaliDateConverter.DateRangeSelectable(min, max,
            includeMinDate = true, includeMaxDate = true)

        assertFalse(selectable.isSelectableDate(NepaliDateConverter.getNepaliCalendar(2081, 4, 30)))
        assertFalse(selectable.isSelectableDate(NepaliDateConverter.getNepaliCalendar(2081, 6, 1)))
    }

    @Test
    fun dateRangeSelectable_isSelectableYear_returnsTrueWithinInclusiveRange() {
        val min = SimpleDate(2080, 1, 1)
        val max = SimpleDate(2082, 12, 30)
        val selectable = NepaliDateConverter.DateRangeSelectable(min, max,
            includeMinDate = true, includeMaxDate = true)

        assertTrue(selectable.isSelectableYear(2080))
        assertTrue(selectable.isSelectableYear(2081))
        assertTrue(selectable.isSelectableYear(2082))
        assertFalse(selectable.isSelectableYear(2079))
        assertFalse(selectable.isSelectableYear(2083))
    }

    @Test
    fun dateRangeSelectable_crossYearBoundary_includesEnclosedDates() {
        val min = SimpleDate(2080, 12, 1)
        val max = SimpleDate(2081, 2, 28)
        val selectable = NepaliDateConverter.DateRangeSelectable(min, max,
            includeMinDate = true, includeMaxDate = true)

        assertTrue(selectable.isSelectableDate(NepaliDateConverter.getNepaliCalendar(2080, 12, 15)))
        assertTrue(selectable.isSelectableDate(NepaliDateConverter.getNepaliCalendar(2081, 1, 15)))
        assertTrue(selectable.isSelectableDate(NepaliDateConverter.getNepaliCalendar(2081, 2, 28)))
        assertFalse(selectable.isSelectableDate(NepaliDateConverter.getNepaliCalendar(2080, 11, 30)))
    }
}
