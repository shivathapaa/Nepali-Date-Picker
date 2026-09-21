// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

package dev.shivathapaa.nepali_date_picker_kmp

import dev.shivathapaa.nepalidatepickerkmp.data.DigitScript
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatter
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pins every wire enum to the engine enum it stands for, entry by entry.
 *
 * The bridge maps these by ordinal, so reordering or extending one side alone
 * would keep compiling and quietly change what a caller's value means; it has
 * to fail here instead.
 */
internal class WireEnumMappingTest {

    @Test
    fun languagesMapEntryForEntry() {
        assertEquals(NepaliDatePickerLang.entries.size, LangDto.entries.size)
        assertEquals(NepaliDatePickerLang.ENGLISH, LangDto.ENGLISH.toCore())
        assertEquals(NepaliDatePickerLang.NEPALI, LangDto.NEPALI.toCore())
    }

    @Test
    fun nameFormatsMapEntryForEntry() {
        assertEquals(NameFormat.entries.size, NameFormatDto.entries.size)
        assertEquals(NameFormat.FULL, NameFormatDto.FULL.toCore())
        assertEquals(NameFormat.MEDIUM, NameFormatDto.MEDIUM.toCore())
        assertEquals(NameFormat.SHORT, NameFormatDto.SHORT.toCore())
    }

    @Test
    fun formatStylesMapEntryForEntry() {
        assertEquals(NepaliDateFormatStyle.entries.size, DateFormatStyleDto.entries.size)
        assertEquals(NepaliDateFormatStyle.FULL, DateFormatStyleDto.FULL.toCore())
        assertEquals(NepaliDateFormatStyle.LONG, DateFormatStyleDto.LONG.toCore())
        assertEquals(NepaliDateFormatStyle.MEDIUM, DateFormatStyleDto.MEDIUM.toCore())
        assertEquals(NepaliDateFormatStyle.SHORT_MDY, DateFormatStyleDto.SHORT_MDY.toCore())
        assertEquals(NepaliDateFormatStyle.SHORT_YMD, DateFormatStyleDto.SHORT_YMD.toCore())
        assertEquals(NepaliDateFormatStyle.COMPACT_MDY, DateFormatStyleDto.COMPACT_MDY.toCore())
        assertEquals(NepaliDateFormatStyle.COMPACT_YMD, DateFormatStyleDto.COMPACT_YMD.toCore())
    }

    @Test
    fun digitScriptsMapEntryForEntry() {
        assertEquals(DigitScript.entries.size, DigitScriptDto.entries.size)
        assertEquals(DigitScript.LATIN, DigitScriptDto.LATIN.toCore())
        assertEquals(DigitScript.DEVANAGARI, DigitScriptDto.DEVANAGARI.toCore())
    }

    @Test
    fun eventKindsMapEntryForEntry() {
        assertEquals(NepaliEventKind.entries.size, EventKindDto.entries.size)
        assertEquals(NepaliEventKind.GovernmentPublic, EventKindDto.GOVERNMENT_PUBLIC.toCore())
        assertEquals(NepaliEventKind.Religious, EventKindDto.RELIGIOUS.toCore())
        assertEquals(NepaliEventKind.Regional, EventKindDto.REGIONAL.toCore())
        assertEquals(NepaliEventKind.Observance, EventKindDto.OBSERVANCE.toCore())
    }

    @Test
    fun eventKindsSurviveTheRoundTripBack() {
        for (kind in NepaliEventKind.entries) {
            assertEquals(kind, kind.toDto().toCore())
        }
    }

    @Test
    fun datePatternsMapEntryForEntry() {
        assertEquals(NepaliDateFormatter.Pattern.entries.size, DatePatternDto.entries.size)
        assertEquals(
            NepaliDateFormatter.Pattern.YYYY_SLASH_MM_SLASH_DD,
            DatePatternDto.YYYY_SLASH_MM_SLASH_DD.toCore()
        )
        assertEquals(
            NepaliDateFormatter.Pattern.YYYY_DASH_MM_DASH_DD,
            DatePatternDto.YYYY_DASH_MM_DASH_DD.toCore()
        )
        assertEquals(
            NepaliDateFormatter.Pattern.DD_SLASH_MM_SLASH_YYYY,
            DatePatternDto.DD_SLASH_MM_SLASH_YYYY.toCore()
        )
        assertEquals(
            NepaliDateFormatter.Pattern.DD_DASH_MM_DASH_YYYY,
            DatePatternDto.DD_DASH_MM_DASH_YYYY.toCore()
        )
    }
}
