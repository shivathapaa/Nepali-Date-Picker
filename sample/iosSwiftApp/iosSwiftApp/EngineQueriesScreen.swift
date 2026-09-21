//
//  EngineQueriesScreen.swift
//  Nepali Date Picker
//
//  The conversion engine asked questions a picker never asks out loud: how long a month is, which
//  days of it line up with the other calendar, where the supported range stops, and how a date turns
//  into whatever shape the next system wants.
//

import SwiftUI
import nepali_date_picker

struct EngineQueriesScreen: View {
    @State private var datePattern = "EEEE, dd MMMM yyyy"
    @State private var timePattern = "hh:mm a"
    @State private var compact = ""
    @State private var monthOffset: Int32 = 0

    private let converter = NepaliDateConverter.shared
    private let defaults = NepaliCalendarDefaults.shared

    /// How many of a month's days the listings print before they are cut short.
    private let previewDays = 4

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                nepaliMonthSection
                englishMonthSection
                pairedDaysSection
                reversePairedSection
                boundarySection
                convertibilitySection
                patternSection
                timePatternSection
                isoSection
                delimiterSection
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 16)
        }
        .navigationTitle("Engine queries")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            if compact.isEmpty { compact = compactToday }
        }
    }

    private var today: SimpleDate { converter.todayNepaliSimpleDate }

    private var todayEnglish: SimpleDate { converter.todayEnglishSimpleDate }

    /// The month the stepper is on, expressed as a Bikram Sambat month calendar.
    private var steppedMonth: NepaliMonthCalendar {
        let base = converter.getNepaliMonthCalendar(nepaliYear: today.year, nepaliMonth: today.month)
        guard monthOffset != 0 else { return base }
        return NepaliCalendarModel(locale: SampleDefaults.english)
            .plusNepaliMonths(fromNepaliCalendar: base, addedMonthsCount: monthOffset)
    }

    private var nepaliMonthSection: some View {
        DemoSection(
            title: "A Bikram Sambat month",
            subtitle: "getNepaliMonthCalendar answers the three facts a grid is built from: the month's length and the weekdays its first and last days fall on. Step through the months and watch the length change, since a Bikram Sambat month runs 29 to 32 days and the pattern differs year to year."
        ) {
            HStack(spacing: 8) {
                Button("Previous") { monthOffset -= 1 }.buttonStyle(.bordered)
                Button("Next") { monthOffset += 1 }.buttonStyle(.bordered)
                Button("Today") { monthOffset = 0 }.buttonStyle(.bordered)
            }
            .padding(.horizontal, 12)

            let month = steppedMonth
            LabeledValue(
                label: "Month",
                value: "\(month.year) " + converter.getMonthName(month: month.month, format: .full, language: .english)
            )
            LabeledValue(label: "Days in month", value: "\(month.totalDaysInMonth)")
            LabeledValue(
                label: "Opens on",
                value: converter.getWeekdayName(dayOfWeek: month.firstDayOfMonth, format: .full, language: .english)
            )
            LabeledValue(
                label: "Closes on",
                value: converter.getWeekdayName(dayOfWeek: month.lastDayOfMonth, format: .full, language: .english)
            )
            LabeledValue(
                label: "getTotalDaysInNepaliMonth",
                value: "\(converter.getTotalDaysInNepaliMonth(year: month.year, month: month.month))"
            )
        }
    }

    private var englishMonthSection: some View {
        DemoSection(
            title: "The Gregorian counterpart",
            subtitle: "getEnglishMonthCalendar is the same shape for the other calendar, and it carries the calendar system it describes, so a value passed around says which month it is."
        ) {
            let month = converter.getEnglishMonthCalendar(
                englishYear: todayEnglish.year,
                englishMonth: todayEnglish.month
            )
            LabeledValue(
                label: "Month",
                value: "\(month.year) " + converter.getEnglishMonthName(month: month.month, format: .full, language: .english)
            )
            LabeledValue(label: "Calendar system", value: month.calendarSystem.name)
            LabeledValue(label: "Days in month", value: "\(month.totalDaysInMonth)")
            LabeledValue(
                label: "Opens on",
                value: converter.getWeekdayName(dayOfWeek: month.firstDayOfMonth, format: .full, language: .english)
            )
            LabeledValue(
                label: "getTotalDaysInEnglishMonth",
                value: "\(converter.getTotalDaysInEnglishMonth(year: todayEnglish.year, month: todayEnglish.month))"
            )
        }
    }

    private var pairedDaysSection: some View {
        DemoSection(
            title: "A Bikram Sambat month, day by day in Gregorian",
            subtitle: "getEnglishCalendarsInNepaliMonth walks the month and hands back the Gregorian day each one lands on. This is what a dual-date grid is drawn from, and how a month that straddles two Gregorian months is detected."
        ) {
            let month = steppedMonth
            let paired = converter.getEnglishCalendarsInNepaliMonth(
                nepaliYear: month.year,
                nepaliMonth: month.month
            )
            LabeledValue(label: "Entries", value: "\(paired.count)")
            ForEach(Array(paired.prefix(previewDays).enumerated()), id: \.offset) { index, day in
                LabeledValue(label: "BS day \(index + 1)", value: day.text)
            }
            if let last = paired.last {
                LabeledValue(label: "BS day \(paired.count)", value: last.text)
            }
            LabeledValue(
                label: "Gregorian months crossed",
                value: Set(paired.map(\.month)).sorted().map { "\($0)" }.joined(separator: ", ")
            )
        }
    }

    private var reversePairedSection: some View {
        DemoSection(
            title: "A Gregorian month, day by day in Bikram Sambat",
            subtitle: "getNepaliCalendarsInEnglishMonthByDay is the reverse, and its answer is optional per day: a Gregorian day outside the supported range has no Bikram Sambat equivalent and comes back as nil rather than as a guess. getEnglishCalendarsInMonth fills the same month in as itself."
        ) {
            let paired = converter.getNepaliCalendarsInEnglishMonthByDay(
                englishYear: todayEnglish.year,
                englishMonth: todayEnglish.month
            )
            LabeledValue(label: "Entries", value: "\(paired.count)")
            LabeledValue(
                label: "Convertible",
                value: "\(paired.compactMap(\.nepaliCalendar).count)"
            )
            ForEach(paired.prefix(previewDays), id: \.englishDayOfMonth) { day in
                LabeledValue(
                    label: "AD day \(day.englishDayOfMonth)",
                    value: day.nepaliCalendar?.text ?? "no equivalent"
                )
            }

            let ownDays = converter.getEnglishCalendarsInMonth(
                englishYear: todayEnglish.year,
                englishMonth: todayEnglish.month
            )
            ForEach(Array(ownDays.prefix(previewDays).enumerated()), id: \.offset) { _, day in
                LabeledValue(
                    label: day.text,
                    value: converter.getWeekdayName(dayOfWeek: day.dayOfWeek, format: .medium, language: .english)
                )
            }
        }
    }

    private var boundarySection: some View {
        DemoSection(
            title: "Where the calendar stops",
            subtitle: "Conversion is table-driven, so the library knows exactly how far it reaches. Extending either range means extending that table, which makes these facts about the build rather than settings."
        ) {
            LabeledValue(
                label: "NepaliYearRange",
                value: "\(defaults.NepaliYearRange.first) to \(defaults.NepaliYearRange.last)"
            )
            LabeledValue(
                label: "EnglishYearRange",
                value: "\(defaults.EnglishYearRange.first) to \(defaults.EnglishYearRange.last)"
            )
            LabeledValue(
                label: "GregorianYearRange",
                value: "\(defaults.GregorianYearRange.first) to \(defaults.GregorianYearRange.last)"
            )
            LabeledValue(label: "First BS day", value: defaults.startingNepaliCalendar.text)
            LabeledValue(label: "The same day in AD", value: defaults.startingEnglishCalendar.text)
            LabeledValue(label: "Last BS day", value: defaults.endNepaliCalendar.text)
            LabeledValue(label: "First day of the week", value: "\(defaults.FIRST_DAY_OF_WEEK)")

            Divider().padding(.horizontal, 12)

            ForEach(Array(narrowedRanges.enumerated()), id: \.offset) { _, range in
                LabeledValue(
                    label: "BS \(range.0)..\(range.1)",
                    value: gregorianSpan(first: range.0, last: range.1)
                )
            }
        }
    }

    private var convertibilitySection: some View {
        DemoSection(
            title: "The convertible window is narrower than the year range",
            subtitle: "The Bikram Sambat year opens in mid-April, so the first Gregorian year is only partly covered. isEnglishDateConvertible is the check a Gregorian-first screen runs before it converts anything."
        ) {
            let min = defaults.minConvertibleEnglishDate
            let max = defaults.maxConvertibleEnglishDate
            LabeledValue(label: "minConvertibleEnglishDate", value: min.text)
            LabeledValue(label: "maxConvertibleEnglishDate", value: max.text)
            LabeledValue(
                label: "\(min.year)/1/1",
                value: converter.isEnglishDateConvertible(englishYYYY: min.year, englishMM: 1, englishDD: 1) ? "yes" : "no"
            )
            LabeledValue(
                label: "The day before the anchor",
                value: converter.isEnglishDateConvertible(
                    englishYYYY: min.year, englishMM: min.month, englishDD: min.dayOfMonth - 1
                ) ? "yes" : "no"
            )
            LabeledValue(
                label: "The anchor itself",
                value: converter.isEnglishDateConvertible(
                    englishYYYY: min.year, englishMM: min.month, englishDD: min.dayOfMonth
                ) ? "yes" : "no"
            )
            LabeledValue(
                label: "The last convertible day",
                value: converter.isEnglishDateConvertible(
                    englishYYYY: max.year, englishMM: max.month, englishDD: max.dayOfMonth
                ) ? "yes" : "no"
            )
            LabeledValue(
                label: "One year past the end",
                value: converter.isEnglishDateConvertible(englishYYYY: max.year + 1, englishMM: 1, englishDD: 1) ? "yes" : "no"
            )
        }
    }

    private var patternSection: some View {
        DemoSection(
            title: "A pattern of your own",
            subtitle: "Tokens: yyyy yy for the year, MMMM MMM MM M for the month, dd d for the day, D for the day of the year, EEEE E EEEEE for the weekday, ee e for its number, w for the week of the year. Anything else is copied through, and there is no escape syntax, so a literal word made of token letters will be rewritten."
        ) {
            TextField("Date pattern", text: $datePattern)
                .textFieldStyle(.roundedBorder)
                .autocorrectionDisabled()
                .padding(.horizontal, 12)

            let nepali = converter.todayNepaliCalendar
            let english = converter.todayEnglishCalendar
            LabeledValue(
                label: "Bikram Sambat",
                value: converter.formatNepaliDateByUnicodePattern(
                    unicodePattern: datePattern, calendar: nepali, language: .english
                )
            )
            LabeledValue(
                label: "Bikram Sambat (नेपाली)",
                value: converter.formatNepaliDateByUnicodePattern(
                    unicodePattern: datePattern, calendar: nepali, language: .nepali
                )
            )
            LabeledValue(
                label: "Gregorian",
                value: converter.formatEnglishDateByUnicodePattern(
                    unicodePattern: datePattern, calendar: english, language: .english
                )
            )
            LabeledValue(
                label: "Gregorian (नेपाली)",
                value: converter.formatEnglishDateByUnicodePattern(
                    unicodePattern: datePattern, calendar: english, language: .nepali
                )
            )
        }
    }

    private var timePatternSection: some View {
        DemoSection(
            title: "A pattern for a time",
            subtitle: "Tokens: HH H for the 24-hour clock, hh h for the 12-hour one, mm m ss s for minutes and seconds, S through SSSS for fractions, a A for the meridiem in either case. The date-time formatters take both families at once."
        ) {
            TextField("Time pattern", text: $timePattern)
                .textFieldStyle(.roundedBorder)
                .autocorrectionDisabled()
                .padding(.horizontal, 12)

            let time = converter.currentTime
            LabeledValue(
                label: "English",
                value: converter.formatTimeByUnicodePattern(
                    unicodePattern: timePattern, time: time, language: .english
                )
            )
            LabeledValue(
                label: "Nepali",
                value: converter.formatTimeByUnicodePattern(
                    unicodePattern: timePattern, time: time, language: .nepali
                )
            )
            LabeledValue(
                label: "Date and time together",
                value: converter.formatNepaliDateTimeByUnicodePattern(
                    unicodePattern: "yyyy/MM/dd HH:mm:ss",
                    calendar: converter.todayNepaliCalendar,
                    time: time,
                    language: .english
                )
            )
            LabeledValue(
                label: "Gregorian, same shape",
                value: converter.formatEnglishDateTimeByUnicodePattern(
                    unicodePattern: "yyyy/MM/dd HH:mm:ss",
                    calendar: converter.todayEnglishCalendar,
                    time: time,
                    language: .english
                )
            )
        }
    }

    private var isoSection: some View {
        DemoSection(
            title: "ISO 8601, both ways round",
            subtitle: "A Bikram Sambat date carries a Nepali wall clock; the Gregorian pair carries the same clock against the converted date. Each has a parser that reads its own output back into a calendar and a time."
        ) {
            let time = converter.currentTime
            let nepaliIso = converter.formatNepaliDateTimeToIsoFormat(nepaliDate: today, time: time)
            let englishIso = converter.formatEnglishDateNepaliTimeToIsoFormat(
                englishDate: todayEnglish, time: time
            )
            LabeledValue(label: "BS date, ISO", value: nepaliIso)
            LabeledValue(
                label: "Read back (BS)",
                value: converter.getNepaliDateTimeFromIsoFormat(isoDateTime: nepaliIso).customCalendar.text
            )
            LabeledValue(label: "AD date, ISO", value: englishIso)
            LabeledValue(
                label: "Read back (AD)",
                value: converter.getEnglishDateNepaliTimeFromIsoFormat(isoDateTime: englishIso).customCalendar.text
            )
        }
    }

    private var delimiterSection: some View {
        DemoSection(
            title: "Formatting and parsing a compact date",
            subtitle: "NepaliDateFormatter is the primitive the text fields are built on: four patterns, formatted with either digit script and parsed back. replaceDelimiter rewrites the separator afterwards without re-parsing anything."
        ) {
            TextField("Text to parse", text: $compact)
                .textFieldStyle(.roundedBorder)
                .autocorrectionDisabled()
                .padding(.horizontal, 12)

            LabeledValue(
                label: "As YYYY/MM/DD",
                value: NepaliDateFormatter.shared
                    .parse(input: compact, pattern: .yyyySlashMmSlashDd)?.text ?? "no match"
            )
            LabeledValue(
                label: "As DD/MM/YYYY",
                value: NepaliDateFormatter.shared
                    .parse(input: compact, pattern: .ddSlashMmSlashYyyy)?.text ?? "no match"
            )
            LabeledValue(label: "Formatted", value: slashedToday)
            LabeledValue(
                label: "Dashed",
                value: converter.replaceDelimiter(dateString: slashedToday, newDelimiter: "-", oldDelimiter: "/")
            )
            LabeledValue(
                label: "Devanagari, dotted",
                value: converter.replaceDelimiter(
                    dateString: NepaliDateFormatter.shared.format(
                        date: today, pattern: .yyyySlashMmSlashDd, script: .devanagari
                    ),
                    newDelimiter: ".",
                    oldDelimiter: "/"
                )
            )
            LabeledValue(
                label: "A time, spaced",
                value: converter.replaceDelimiter(
                    dateString: converter.getFormattedTimeInEnglish(
                        simpleTime: converter.currentTime, use12HourFormat: true
                    ),
                    newDelimiter: " ",
                    oldDelimiter: ":"
                )
            )
        }
    }

    /// Today in the slash pattern, the starting text of the parse demo.
    private var slashedToday: String {
        NepaliDateFormatter.shared.format(date: today, pattern: .yyyySlashMmSlashDd, script: .latin)
    }

    private var compactToday: String { slashedToday }

    /// Bikram Sambat windows the Gregorian mapping is shown for.
    private var narrowedRanges: [(Int32, Int32)] {
        [
            (defaults.NepaliYearRange.first, defaults.NepaliYearRange.last),
            (2080, 2085),
            (2000, 2010),
            (defaults.NepaliYearRange.last, defaults.NepaliYearRange.last)
        ]
    }

    /// The English years a Bikram Sambat window maps onto, clamped into the supported range.
    private func gregorianSpan(first: Int32, last: Int32) -> String {
        let range = defaults.gregorianYearRangeFor(
            nepaliYearRange: KotlinIntRange(start: first, endInclusive: last)
        )
        return "AD \(range.first) to \(range.last)"
    }
}

#Preview {
    NavigationStack { EngineQueriesScreen() }
}
