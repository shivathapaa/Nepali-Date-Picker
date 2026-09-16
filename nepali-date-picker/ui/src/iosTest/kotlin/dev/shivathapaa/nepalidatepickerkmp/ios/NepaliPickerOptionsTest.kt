/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepickerkmp.ios

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The options classes restate the composables' default arguments, because Kotlin defaults do not
 * survive the Objective-C bridge. That duplication drifts silently: changing a default in the
 * composable leaves iOS on the old value with nothing to notice. These tests pin the values.
 */
class NepaliPickerOptionsTest {

    @Test
    fun calendarOptionsMatchComposableDefaults() {
        val options = NepaliCalendarOptions()
        assertTrue(options.showModeToggle)
        assertTrue(options.showTodayButton)
        assertFalse(options.showEnglishDate)
        assertNull(options.englishDateLocale, "A null locale falls back to the picker's own.")
    }

    @Test
    fun rangeCalendarOptionsMatchComposableDefaults() {
        val options = NepaliRangeCalendarOptions()
        assertTrue(options.showModeToggle)
        assertTrue(options.showTodayButton)
        assertTrue(options.showMonthsVertically)
        assertTrue(options.showYearPickerAndMonthNavigation)
        assertFalse(options.showEnglishDate)
        assertNull(options.englishDateLocale)
    }

    @Test
    fun wheelOptionsMatchComposableDefaults() {
        val options = NepaliWheelOptions()
        // Mirrors WheelItemHeight, WheelVisibleCount and WheelCornerRadius, which are private to
        // NepaliWheelDatePicker.kt and so cannot be referenced directly.
        assertEquals(44f, options.itemHeight)
        assertEquals(5, options.visibleItemCount)
        assertEquals(20f, options.cornerRadius)
    }

    @Test
    fun dockedOptionsMatchComposableDefaults() {
        val options = NepaliDockedOptions()
        assertEquals(NepaliDateFormatStyle.MEDIUM, options.dateFormatStyle)
        assertTrue(options.showTodayButton)
        assertNull(options.label)
        assertNull(options.placeholder)
        // Mirrors the private DockedPopupElevation.
        assertEquals(6f, options.popupShadowElevation)
    }

    @Test
    fun fieldOptionsDefaultToAnEditableOutlinedField() {
        val options = NepaliFieldOptions()
        assertTrue(options.outlined)
        assertTrue(options.enabled)
        assertFalse(options.readOnly)
        assertFalse(options.isError)
        // Null text keeps the composable's own default rather than blanking it.
        assertNull(options.label)
        assertNull(options.placeholder)
        assertNull(options.supportingText)
        assertNull(options.confirmButtonText)
        assertNull(options.dismissButtonText)
    }

    @Test
    fun rangeFieldOptionsDefaultToAnEditableOutlinedPair() {
        val options = NepaliRangeFieldOptions()
        assertTrue(options.outlined)
        assertTrue(options.enabled)
        assertFalse(options.readOnly)
        assertFalse(options.isStartError)
        assertFalse(options.isEndError)
        assertNull(options.startLabel, "A null label keeps the localized default.")
        assertNull(options.endLabel)
    }

    @Test
    fun dialogOptionsMatchComposableDefaults() {
        val options = NepaliDialogOptions()
        assertNull(options.title)
        assertEquals(
            NepaliDatePickerDefaults.TonalElevation.value,
            options.tonalElevation,
            "Tonal elevation must track the library default."
        )
    }
}
