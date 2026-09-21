# Module core

Compose-free Bikram Sambat engine. Converts between BS and AD dates, resolves month layouts,
formats dates in Nepali or English locales, and compares calendars, all on plain `kotlinx-datetime`
with zero Compose dependency, so it ships on server, CLI, and every native target.

`NepaliDateConverter` is the entry point for most consumers. `NepaliCalendarModel` is the underlying
locale-aware engine, and `NepaliCalendarDefaults` holds the supported year ranges and boundary
calendars. The supported range (BS 1970..2100 / AD 1913..2043) is bounded by the conversion table in
`data/NepaliYearMonthMap.kt`.

Indexing is 1-based: month/weekday 1 = Baisakh / Sunday, 12 = Chaitra; `era` 1 = AD, 2 = BS.

Both calendars are first class. Alongside the Bikram Sambat entry points there are Gregorian ones
(`getEnglishCalendar`, `getEnglishMonthCalendar`, `parseEnglish`, `formatEnglishDate`), whole-month
batch converters that resolve a month in a single pass, and `isEnglishDateConvertible` plus
`minConvertibleEnglishDate` / `maxConvertibleEnglishDate` for the bounds. The two calendars start
mid-year relative to each other, so `EnglishYearRange` alone is not a sufficient bound: AD 1913-01-01
through 1913-04-12 sit inside the year range yet have no Bikram Sambat equivalent.

# Package dev.shivathapaa.nepalidatepickerkmp.calendar_model

The conversion and formatting engine. `NepaliCalendarModel` performs BS↔AD conversion, month-detail
lookup, formatting, and comparison; `NepaliDateConverter` is the public facade over it;
`NepaliCalendarDefaults` exposes year ranges and boundary calendars.

# Package dev.shivathapaa.nepalidatepickerkmp.data

Immutable calendar models (`CustomCalendar`, `SimpleDate`, `SimpleTime`, `NepaliMonthCalendar`),
locale (`NepaliDateLocale`), digit scripts, and the `NepaliDateFormatter` contract that drives
locale-aware rendering.

`CalendarSystem` is the named form of the `era` integer (1 = AD, 2 = BS) and `MonthCalendar` is the
calendar-tagged counterpart of `NepaliMonthCalendar`, so month geometry can be handled without
knowing which calendar produced it. Use `toMonthCalendar` and `toNepaliMonthCalendar` to move between
the two.

# Package dev.shivathapaa.nepalidatepickerkmp.event

Calendar-event SPI. `NepaliCalendarEvent` is one thing on one day, a public holiday, a festival, a
programme or a meeting, and `closesOffices` says whether the institution is actually shut for it.
`NepaliEventProvider` supplies them, `NepaliCalendarPolicy` pairs that list with the weekdays an
institution never opens and answers `statusOf` / `eventsOn` / `eventsIn` / `monthStatus`, and the
working-day helpers count by the same rule. `spanningDays` and `spanningThrough` expand something
that runs longer than a day into the per-day entries the rest of the package reads. No event dataset
ships with the library by design.

# Package dev.shivathapaa.nepalidatepickerkmp.holiday

The 3.1.0 holiday names, deprecated. Typealiases onto the `event` package so existing imports keep
resolving; an implementation of the old provider renames `holidays(year)` to `events(year)`.

# Package dev.shivathapaa.nepalidatepickerkmp.annotations

`ExperimentalNepaliDatePickerApi`, the opt-in marker guarding APIs whose shape may still change.

# Package dev.shivathapaa.nepalidatepickerkmp.annotation

Compose stability markers (`Immutable`, `Stable`). They resolve to the Compose runtime annotations on
Compose-capable targets and compile away everywhere else, so `:core` advertises stability without
depending on Compose.
