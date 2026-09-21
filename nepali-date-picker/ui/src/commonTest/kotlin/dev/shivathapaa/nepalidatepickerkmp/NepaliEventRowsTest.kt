/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 */

package dev.shivathapaa.nepalidatepickerkmp

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.collapseEventRows
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.spanningDays
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * How a month's entries become its list rows: a span is one row, and everything else stays the day
 * it falls on.
 */
class NepaliEventRowsTest {

    private fun event(
        day: Int,
        name: String,
        id: String? = null,
        month: Int = 6,
        year: Int = 2082
    ) = NepaliCalendarEvent(
        date = SimpleDate(year, month, day),
        name = name,
        kind = NepaliEventKind.Religious,
        id = id
    )

    @Test
    fun aSpanSharingAnId_becomesOneRow() {
        val dashain = event(17, "Dashain", id = "dashain-2082").spanningDays(10)

        val rows = collapseEventRows(dashain)

        assertEquals(1, rows.size)
        assertEquals(SimpleDate(2082, 6, 17), rows.single().firstDate)
        assertEquals(SimpleDate(2082, 6, 26), rows.single().lastDate)
        assertTrue(rows.single().isSpan)
    }

    @Test
    fun aSingleDayEvent_isARowOfOneDay() {
        val rows = collapseEventRows(listOf(event(3, "Constitution Day")))

        assertEquals(1, rows.size)
        assertEquals(rows.single().firstDate, rows.single().lastDate)
        assertTrue(!rows.single().isSpan)
    }

    @Test
    fun entriesWithoutAnId_stayOneRowPerDay() {
        val rows = collapseEventRows(
            listOf(event(3, "Meeting"), event(4, "Meeting"), event(5, "Meeting"))
        )

        assertEquals(3, rows.size)
    }

    @Test
    fun aRepeatedIdThatSkipsADay_startsANewRow() {
        val rows = collapseEventRows(
            listOf(
                event(3, "Exam", id = "exam"),
                event(4, "Exam", id = "exam"),
                event(8, "Exam", id = "exam")
            )
        )

        assertEquals(2, rows.size)
        assertEquals(SimpleDate(2082, 6, 4), rows.first().lastDate)
        assertEquals(SimpleDate(2082, 6, 8), rows.last().firstDate)
    }

    @Test
    fun twoSpansInterleavedByDay_keepTheirOwnRows() {
        val rows = collapseEventRows(
            listOf(
                event(3, "Festival", id = "festival"),
                event(3, "Leave", id = "leave"),
                event(4, "Festival", id = "festival"),
                event(4, "Leave", id = "leave")
            )
        )

        assertEquals(2, rows.size)
        assertEquals(listOf("Festival", "Leave"), rows.map { it.event.name })
        assertTrue(rows.all { it.isSpan })
    }

    @Test
    fun aSpanCrossingAMonthBoundary_staysOneRow() {
        val entries = event(31, "Leave", id = "leave", month = 5).spanningDays(3)

        val rows = collapseEventRows(entries)

        assertEquals(1, rows.size)
        assertEquals(SimpleDate(2082, 5, 31), rows.single().firstDate)
        assertEquals(SimpleDate(2082, 6, 2), rows.single().lastDate)
    }

    @Test
    fun theOrderItIsGiven_isTheOrderItKeeps() {
        val rows = collapseEventRows(
            listOf(event(2, "Second"), event(1, "First"), event(9, "Ninth"))
        )

        assertEquals(listOf("Second", "First", "Ninth"), rows.map { it.event.name })
    }

    @Test
    fun aRowCoversEveryDayOfItsRun() {
        val row = collapseEventRows(event(17, "Dashain", id = "dashain").spanningDays(3)).single()

        assertTrue(row.covers(SimpleDate(2082, 6, 17)))
        assertTrue(row.covers(SimpleDate(2082, 6, 18)))
        assertTrue(row.covers(SimpleDate(2082, 6, 19)))
        assertTrue(!row.covers(SimpleDate(2082, 6, 20)))
    }

    @Test
    fun nothingAtAll_isNoRows() {
        assertEquals(emptyList(), collapseEventRows(emptyList()))
    }
}
