# Module core

Compose-free Bikram Sambat engine. Converts between BS and AD dates, resolves month layouts,
formats dates in Nepali or English locales, and compares calendars — all on plain `kotlinx-datetime`
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

# Package dev.shivathapaa.nepalidatepickerkmp.holiday

Holiday SPI. `NepaliHolidayProvider` and the working-day helpers let a consumer plug in holiday data;
no holiday dataset ships with the library by design.

# Package dev.shivathapaa.nepalidatepickerkmp.annotations

`ExperimentalNepaliDatePickerApi` — opt-in marker guarding APIs whose shape may still change.

# Package dev.shivathapaa.nepalidatepickerkmp.annotation

Compose stability markers (`Immutable`, `Stable`) declared as optional expectations so `:core` can
advertise Compose stability on Compose-capable targets without depending on Compose elsewhere.
