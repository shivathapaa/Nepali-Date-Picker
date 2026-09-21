# Module ui

Compose Multiplatform date-picker UI aligned with the Material3 `DatePicker`, built on `:core`.
Covers the calendar dialog, docked and full-screen variants, wheel picker, range pickers, and text
input fields, each with optional paired English dates. It also carries the browsable month
calendar, where the events an app keeps are the point rather than an accent. This module produces
the iOS XCFramework and the Android View factories the Flutter plugin embeds.

The newer variants (wheel, docked, range, full-screen dialog, text fields) require
`@OptIn(ExperimentalNepaliDatePickerApi::class)`.

Any picker that draws a month grid takes a `dayDecorator`, which marks the days that carry a
holiday, a festival or an app's own event: one colour for what the day *is*, and up to three dots
for what is *scheduled* on it. `NepaliDatePickerDefaults.eventDecorator` turns a `:core`
`NepaliCalendarPolicy` into one, `dayDecorator` takes plain maps keyed by date, and `then` layers
the two. Marking never blocks a date; `policy.asSelectableDates()` does that separately.

An event's `id` and `payload` are handed back untouched when a day or a list line is tapped, so an
app finds its own record behind what was drawn. Image URLs live there: the library never loads or
draws a picture, and the app renders it with whatever it already uses.

`NepaliCalendar` is the calendar as a screen rather than a dialog: it fills the width it is given,
pages month by month, and takes a `NepaliCalendarPolicy` directly, so the grid is marked from one
`monthStatus` pass per visible month. `NepaliDaySummary` writes the picked day out, whether the
institution is shut and why, and `NepaliMonthEventList` lists the month with a festival that runs
several days collapsed into one line. All three share a `rememberNepaliCalendarState`, and none of
them scrolls on its own, so they sit inside a screen's own scrolling content.

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

The calendar family lives here as well: `NepaliCalendar`, `NepaliDaySummary`,
`NepaliMonthEventList`, and the `NepaliCalendarState` they share.

# Package dev.shivathapaa.nepalidatepickerkmp.calendar_model

`NepaliDatePickerDefaults`: colors, shapes, and configuration defaults shared across the picker
composables, following the Material3 defaults pattern.

Day marking lives here too: `NepaliDayDecorator` and the `NepaliDayInfo` it is handed per cell,
`NepaliDayDecoration` for what to draw, `NepaliDayMarkerColors` for a palette resolved from
`MaterialTheme.colorScheme`, and `NepaliEventDisplayStyle` for which channels are drawn at all.

# Package dev.shivathapaa.nepalidatepickerkmp.icons

`NepaliIcons`: the vector assets the picker draws (navigation chevrons, toggle affordances), so the
UI carries no dependency on an external icon pack.
