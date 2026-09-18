# Module ui

Compose Multiplatform date-picker UI aligned with the Material3 `DatePicker`, built on `:core`.
Covers the calendar dialog, docked and full-screen variants, wheel picker, range pickers, and text
input fields — each with optional paired English dates. This module also produces the iOS
XCFramework.

The newer variants (wheel, docked, range, full-screen dialog, text fields) require
`@OptIn(ExperimentalNepaliDatePickerApi::class)`.

Every picker can display either calendar. `NepaliDatePickerState.displayedCalendarSystem` decides
which one is on screen, `showCalendarSystemToggle` draws a `B.S.` / `A.D.` switch for the user, and
`showAdjacentMonthDays` fills the grid's empty cells with the neighbouring months' days. Only the
display changes: a selected date is always reported in Bikram Sambat, so switching keeps the same day
selected.

# Package dev.shivathapaa.nepalidatepickerkmp

The picker composables and their state holders: `NepaliDatePicker`, `NepaliDatePickerDialog`,
`NepaliDatePickerDocked`, `NepaliDatePickerFullScreenDialog`, `NepaliWheelDatePicker`,
`NepaliDateRangePicker`, the `*WithEnglishDate` variants, the `NepaliDateField` /
`NepaliDateInput` / `NepaliDateRangeField` text-entry surfaces, and `NepaliCalendarSystemToggle` for
driving the displayed calendar from your own chrome.

# Package dev.shivathapaa.nepalidatepickerkmp.calendar_model

`NepaliDatePickerDefaults` — colors, shapes, and configuration defaults shared across the picker
composables, following the Material3 defaults pattern.

# Package dev.shivathapaa.nepalidatepickerkmp.icons

`NepaliIcons` — the vector assets the picker draws (navigation chevrons, toggle affordances) so the
UI carries no dependency on an external icon pack.
