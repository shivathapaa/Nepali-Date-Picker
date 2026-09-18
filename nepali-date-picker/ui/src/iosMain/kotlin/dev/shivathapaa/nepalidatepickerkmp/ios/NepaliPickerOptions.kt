/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepickerkmp.ios

import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale

/**
 * Optional customization for the full calendar pickers.
 *
 * Kotlin default arguments do not survive the Objective-C bridge, so every knob a Swift caller
 * might want is gathered here as a mutable property with the library's own default. Swift can build
 * one with `NepaliCalendarOptions()` and set only what it needs to change.
 *
 * @property showModeToggle whether the calendar/typed-input switch is shown.
 * @property showTodayButton whether the shortcut back to today is shown.
 * @property showEnglishDate pairs every Bikram Sambat day with its Gregorian equivalent.
 * @property englishDateLocale locale used for the Gregorian half. Falls back to the picker's locale.
 * @property initialCalendarSystem the calendar the grid opens in. The date reported back is always
 * Bikram Sambat, whichever calendar is displayed.
 * @property showCalendarSystemToggle whether the `B.S.` / `A.D.` switch is shown.
 * @property showAdjacentMonthDays fills the grid's empty cells with the neighbouring months'
 * days, drawn faded. Tapping one selects that day and moves the grid to its month.
 */
class NepaliCalendarOptions {
    var showModeToggle: Boolean = true
    var showTodayButton: Boolean = true
    var showEnglishDate: Boolean = false
    var englishDateLocale: NepaliDateLocale? = null
    var initialCalendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT
    var showCalendarSystemToggle: Boolean = false
    var showAdjacentMonthDays: Boolean = false
}

/**
 * Optional customization for the range calendars.
 *
 * @property showModeToggle whether the calendar/typed-input switch is shown.
 * @property showTodayButton whether the shortcut back to today is shown.
 * @property showMonthsVertically stacks the months instead of paging horizontally.
 * @property showYearPickerAndMonthNavigation whether the year dropdown and month arrows are shown.
 * @property showEnglishDate pairs every Bikram Sambat day with its Gregorian equivalent.
 * @property englishDateLocale locale used for the Gregorian half. Falls back to the picker's locale.
 * @property initialCalendarSystem the calendar the grid opens in. The range reported back is always
 * Bikram Sambat, whichever calendar is displayed.
 * @property showCalendarSystemToggle whether the `B.S.` / `A.D.` switch is shown. Needs
 * [showYearPickerAndMonthNavigation].
 * @property showAdjacentMonthDays fills the grid's empty cells with the neighbouring months'
 * days, drawn faded. Tapping one selects that day and moves the grid to its month.
 */
class NepaliRangeCalendarOptions {
    var showModeToggle: Boolean = true
    var showTodayButton: Boolean = true
    var showMonthsVertically: Boolean = true
    var showYearPickerAndMonthNavigation: Boolean = true
    var showEnglishDate: Boolean = false
    var englishDateLocale: NepaliDateLocale? = null
    var initialCalendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT
    var showCalendarSystemToggle: Boolean = false
    var showAdjacentMonthDays: Boolean = false
}

/**
 * Optional customization for the wheel picker.
 *
 * @property itemHeight height of one row, in points.
 * @property visibleItemCount how many rows are visible at once. Odd numbers centre the selection.
 * @property cornerRadius corner radius of the wheel, in points.
 * @property initialCalendarSystem the calendar the wheels start in.
 * @property showCalendarSystemToggle whether the `B.S.` / `A.D.` switch is shown above the wheels.
 */
class NepaliWheelOptions {
    var itemHeight: Float = 44f
    var visibleItemCount: Int = 5
    var cornerRadius: Float = 20f
    var initialCalendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT
    var showCalendarSystemToggle: Boolean = false
}

/**
 * Optional customization for the docked picker.
 *
 * @property dateFormatStyle how the selected date is written inside the field.
 * @property showTodayButton whether the shortcut back to today is shown in the popup.
 * @property label text shown as the field's label, or `null` for none.
 * @property placeholder text shown while the field is empty, or `null` for none.
 * @property cornerRadius corner radius of the anchoring field, in points.
 * @property popupShadowElevation shadow elevation of the popup, in points.
 * @property initialCalendarSystem the calendar the popup grid opens in.
 * @property showCalendarSystemToggle whether the `B.S.` / `A.D.` switch is shown in the popup.
 * @property showAdjacentMonthDays fills the grid's empty cells with the neighbouring months'
 * days, drawn faded. Tapping one selects that day and moves the grid to its month.
 */
class NepaliDockedOptions {
    var dateFormatStyle: NepaliDateFormatStyle = NepaliDateFormatStyle.MEDIUM
    var showTodayButton: Boolean = true
    var label: String? = null
    var placeholder: String? = null
    var cornerRadius: Float = 4f
    var popupShadowElevation: Float = 6f
    var initialCalendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT
    var showCalendarSystemToggle: Boolean = false
    var showAdjacentMonthDays: Boolean = false
}

/**
 * Optional customization for a single typed date field.
 *
 * @property outlined picks the outlined style when true and the filled style when false. The filled
 * style opens a confirmation dialog, which is what [confirmButtonText] and [dismissButtonText] label.
 * @property label text shown as the field's label, or `null` for none.
 * @property placeholder text shown while the field is empty. `null` keeps the library's hint, which
 * spells out the expected pattern.
 * @property supportingText helper text under the field, or `null` for none.
 * @property isError renders the field in its error state.
 * @property enabled whether the field accepts input.
 * @property readOnly whether the value can be edited.
 * @property confirmButtonText label of the filled style's confirming button.
 * @property dismissButtonText label of the filled style's dismissing button.
 * @property cornerRadius corner radius of the field, in points.
 * @property initialCalendarSystem the calendar the user types in, and the one the filled style's
 * dialog opens in. The date reported back is always Bikram Sambat.
 * @property showCalendarSystemToggle whether the filled style's dialog shows the `B.S.` / `A.D.`
 * switch. Ignored by the outlined style, which has no dialog.
 * @property showAdjacentMonthDays fills the filled style's dialog grid with the neighbouring months'
 * days, drawn faded. Ignored by the outlined style.
 */
class NepaliFieldOptions {
    var outlined: Boolean = true
    var label: String? = null
    var placeholder: String? = null
    var supportingText: String? = null
    var isError: Boolean = false
    var enabled: Boolean = true
    var readOnly: Boolean = false
    var confirmButtonText: String? = null
    var dismissButtonText: String? = null
    var cornerRadius: Float = 4f
    var initialCalendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT
    var showCalendarSystemToggle: Boolean = false
    var showAdjacentMonthDays: Boolean = false
}

/**
 * Optional customization for the paired start and end fields.
 *
 * @property outlined picks the outlined style when true and the filled style when false.
 * @property startLabel label of the start field. `null` keeps the localized default.
 * @property endLabel label of the end field. `null` keeps the localized default.
 * @property supportingText helper text under the pair, or `null` for none.
 * @property isStartError renders the start field in its error state.
 * @property isEndError renders the end field in its error state.
 * @property enabled whether the fields accept input.
 * @property readOnly whether the values can be edited.
 * @property confirmButtonText label of the filled style's confirming button.
 * @property dismissButtonText label of the filled style's dismissing button.
 * @property cornerRadius corner radius of both fields, in points.
 * @property initialCalendarSystem the calendar both sides are typed in, and the one the filled
 * style's dialog opens in. The range reported back is always Bikram Sambat.
 * @property showCalendarSystemToggle whether the filled style's dialog shows the `B.S.` / `A.D.`
 * switch. Ignored by the outlined style, which has no dialog.
 * @property showAdjacentMonthDays fills the filled style's dialog grid with the neighbouring months'
 * days, drawn faded. Ignored by the outlined style.
 */
class NepaliRangeFieldOptions {
    var outlined: Boolean = true
    var startLabel: String? = null
    var endLabel: String? = null
    var supportingText: String? = null
    var isStartError: Boolean = false
    var isEndError: Boolean = false
    var enabled: Boolean = true
    var readOnly: Boolean = false
    var confirmButtonText: String? = null
    var dismissButtonText: String? = null
    var cornerRadius: Float = 4f
    var initialCalendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT
    var showCalendarSystemToggle: Boolean = false
    var showAdjacentMonthDays: Boolean = false
}

/**
 * Optional customization for the dialogs.
 *
 * @property title heading shown above the calendar. Only the full-screen dialog draws one.
 * @property confirmText label of the confirming button.
 * @property dismissText label of the dismissing button.
 * @property tonalElevation tonal elevation of the dialog surface, in points. Ignored by the
 * full-screen variant, which has no floating surface.
 * @property cornerRadius corner radius of the dialog surface, in points.
 */
class NepaliDialogOptions {
    var title: String? = null
    var confirmText: String = "OK"
    var dismissText: String = "Cancel"
    var tonalElevation: Float = 6f
    var cornerRadius: Float = 28f
}
