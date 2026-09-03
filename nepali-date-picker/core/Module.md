# Module core

Compose-free Bikram Sambat engine. Converts between BS and AD dates, resolves month layouts,
formats dates in Nepali or English locales, and compares calendars — all on plain `kotlinx-datetime`
with zero Compose dependency, so it ships on server, CLI, and every native target.

`NepaliDateConverter` is the entry point for most consumers. `NepaliCalendarModel` is the underlying
locale-aware engine, and `NepaliCalendarDefaults` holds the supported year ranges and boundary
calendars. The supported range (BS 1970..2100 / AD 1913..2043) is bounded by the conversion table in
`data/NepaliYearMonthMap.kt`.

Indexing is 1-based: month/weekday 1 = Baisakh / Sunday, 12 = Chaitra; `era` 1 = AD, 2 = BS.

# Package dev.shivathapaa.nepalidatepickerkmp.calendar_model

The conversion and formatting engine. `NepaliCalendarModel` performs BS↔AD conversion, month-detail
lookup, formatting, and comparison; `NepaliDateConverter` is the public facade over it;
`NepaliCalendarDefaults` exposes year ranges and boundary calendars.

# Package dev.shivathapaa.nepalidatepickerkmp.data

Immutable calendar models (`CustomCalendar`, `SimpleDate`, `SimpleTime`, `NepaliMonthCalendar`),
locale (`NepaliDateLocale`), digit scripts, and the `NepaliDateFormatter` contract that drives
locale-aware rendering.

# Package dev.shivathapaa.nepalidatepickerkmp.holiday

Holiday SPI. `NepaliHolidayProvider` and the working-day helpers let a consumer plug in holiday data;
no holiday dataset ships with the library by design.

# Package dev.shivathapaa.nepalidatepickerkmp.annotations

`ExperimentalNepaliDatePickerApi` — opt-in marker guarding APIs whose shape may still change.

# Package dev.shivathapaa.nepalidatepickerkmp.annotation

Compose stability markers (`Immutable`, `Stable`) declared as optional expectations so `:core` can
advertise Compose stability on Compose-capable targets without depending on Compose elsewhere.
