# Module serialization

Optional `kotlinx-serialization` support for the `:core` calendar models. Adds `KSerializer`
instances so `CustomCalendar`, `SimpleDate`, `SimpleTime`, and the other model types cross process
and storage boundaries without a hand-written mapping layer. Pure Kotlin, no Compose, shipped on the
same expanded target matrix as `:core`.

# Package dev.shivathapaa.nepalidatepickerkmp.serialization

Serializers for the core calendar models. Register or reference these when persisting or
transmitting Nepali Date Picker types through a `kotlinx-serialization` format.

`NepaliDatePickerSerializersModule` binds every published type: `SimpleDate`, `SimpleTime`,
`CustomCalendar`, `NepaliMonthCalendar`, `MonthCalendar`, `CalendarSystem`, and the event types
`NepaliCalendarEvent`, `NepaliEventKind` and `NepaliDayStatus`. A `CalendarSystem` is written as its
`era` number, which is the form `CustomCalendarSerializer` already uses and is stable against
reordering the enum.

An event is written as `{"date", "name", "kind"}`, with `closesOffices` added only when it disagrees
with what its kind usually means and `id` / `payload` only when set, so a cached holiday list stays
compact. A `NepaliEventKind` is written as its name rather than its ordinal, so adding a kind later
cannot silently rewrite what a stored file means; an unknown name fails rather than being guessed.
