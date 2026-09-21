/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepickerkmp

import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The labels a calendar writes a day or a month out with. Both languages have to answer every one
 * of them, and the two answers have to differ, since a missing translation would otherwise show up
 * as English text inside a Nepali calendar.
 */
class CalendarSurfaceLabelTests {

    private val labelsOf: (NepaliDatePickerLang) -> List<String> = { language ->
        listOf(
            language.closedText,
            language.workingDayText,
            language.weeklyOffText,
            language.noEventsOnDayText,
            language.noEventsInMonthText
        )
    }

    @Test
    fun everyLanguage_answersEveryCalendarLabel() {
        NepaliDatePickerLang.entries.forEach { language ->
            labelsOf(language).forEach { label ->
                assertTrue(label.isNotBlank(), "$language left a calendar label blank")
            }
        }
    }

    @Test
    fun nepaliLabels_areTranslatedRatherThanCopied() {
        val english = labelsOf(NepaliDatePickerLang.ENGLISH)
        val nepali = labelsOf(NepaliDatePickerLang.NEPALI)
        english.zip(nepali).forEach { (englishLabel, nepaliLabel) ->
            assertTrue(
                englishLabel != nepaliLabel,
                "\"$englishLabel\" was left untranslated in Nepali"
            )
        }
    }

    @Test
    fun englishLabels_readAsTheCalendarWritesThem() {
        assertEquals("Closed", NepaliDatePickerLang.ENGLISH.closedText)
        assertEquals("Working day", NepaliDatePickerLang.ENGLISH.workingDayText)
        assertEquals("Weekly day off", NepaliDatePickerLang.ENGLISH.weeklyOffText)
    }
}
