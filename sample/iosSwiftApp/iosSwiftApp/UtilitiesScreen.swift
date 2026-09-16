//
//  UtilitiesScreen.swift
//  Nepali Date Picker
//
//  The Compose-free core: conversions, formatting, time, ISO, working days and digit scripts.
//

import SwiftUI
import nepali_date_picker

struct UtilitiesScreen: View {
    private let converter = NepaliDateConverter.shared

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                todaySection
                conversionSection
                spanSection
                formattingSection
                timeSection
                isoSection
                workingDaysSection
                comparisonSection
                digitSection
                namesSection
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 16)
        }
        .navigationTitle("Utilities")
    }

    private var todaySection: some View {
        DemoSection(title: "Today", subtitle: "The current date in both calendars, plus the wall-clock time.") {
            let time = converter.currentTime
            LabeledValue(label: "Bikram Sambat", value: converter.todayNepaliCalendar.text)
            LabeledValue(label: "Gregorian", value: converter.todayEnglishSimpleDate.text)
            LabeledValue(label: "Time", value: "\(time.hour):\(time.minute):\(time.second)")
        }
    }

    private var conversionSection: some View {
        DemoSection(title: "Conversions", subtitle: "Convert freely between Bikram Sambat and Gregorian.") {
            LabeledValue(
                label: "2024-03-21 AD",
                value: converter.convertEnglishToNepali(englishYYYY: 2024, englishMM: 3, englishDD: 21).text + " BS"
            )
            LabeledValue(
                label: "2081-01-01 BS",
                value: converter.convertNepaliToEnglish(nepaliYYYY: 2081, nepaliMM: 1, nepaliDD: 1).text + " AD"
            )
        }
    }

    private var spanSection: some View {
        DemoSection(title: "Spans and month lengths", subtitle: "Day counts between dates and the length of a month.") {
            LabeledValue(
                label: "Days in BS 2081",
                value: "\(converter.getNepaliDaysInBetween(startDate: SimpleDate(year: 2081, month: 1, dayOfMonth: 1), endDate: SimpleDate(year: 2081, month: 12, dayOfMonth: 30)))"
            )
            LabeledValue(
                label: "Days in AD 2024",
                value: "\(converter.getEnglishDaysInBetween(startDate: SimpleDate(year: 2024, month: 1, dayOfMonth: 1), endDate: SimpleDate(year: 2024, month: 12, dayOfMonth: 31)))"
            )
            LabeledValue(label: "Days in BS 2081-01", value: "\(converter.getTotalDaysInNepaliMonth(year: 2081, month: 1))")
            LabeledValue(label: "Days in AD 2024-02", value: "\(converter.getTotalDaysInEnglishMonth(year: 2024, month: 2))")
        }
    }

    private var formattingSection: some View {
        DemoSection(title: "Formatting", subtitle: "Render a date through the locale, in English and Nepali.") {
            let today = converter.todayNepaliCalendar
            LabeledValue(
                label: "English",
                value: converter.formatNepaliDate(customCalendar: today, locale: SampleDefaults.englishText)
            )
            LabeledValue(
                label: "Nepali",
                value: converter.formatNepaliDate(customCalendar: today, locale: SampleDefaults.nepaliText)
            )
            LabeledValue(
                label: "Unicode pattern",
                value: converter.formatNepaliDateByUnicodePattern(
                    unicodePattern: "EEEE, MMMM d, yyyy",
                    calendar: today,
                    language: .english
                )
            )
        }
    }

    private var timeSection: some View {
        DemoSection(title: "Time formatting", subtitle: "12-hour and 24-hour clocks in English and Nepali.") {
            let time = converter.currentTime
            LabeledValue(label: "English 12h", value: converter.getFormattedTimeInEnglish(simpleTime: time, use12HourFormat: true))
            LabeledValue(label: "English 24h", value: converter.getFormattedTimeInEnglish(simpleTime: time, use12HourFormat: false))
            LabeledValue(label: "Nepali 12h", value: converter.getFormattedTimeInNepali(simpleTime: time, use12HourFormat: true))
            LabeledValue(label: "Nepali 24h", value: converter.getFormattedTimeInNepali(simpleTime: time, use12HourFormat: false))
        }
    }

    private var isoSection: some View {
        DemoSection(title: "ISO 8601", subtitle: "Serialize a BS date and time to ISO, then parse it back.") {
            let iso = converter.formatNepaliDateTimeToIsoFormat(
                nepaliDate: converter.todayNepaliCalendar.simple,
                time: converter.currentTime
            )
            LabeledValue(label: "ISO string", value: iso)
            LabeledValue(
                label: "Parsed back (BS)",
                value: converter.getNepaliDateTimeFromIsoFormat(isoDateTime: iso).customCalendar.text
            )
        }
    }

    private var workingDaysSection: some View {
        DemoSection(title: "Working days", subtitle: "Weekend and holiday aware arithmetic. Nepal observes a single-day weekend.") {
            let from = converter.todayNepaliCalendar.simple
            let weekend = NepaliWeekend.shared.Default
            let plusThirty = converter.getNepaliCalendarAfterAdditionOrSubtraction(
                year: from.year, month: from.month, dayOfMonth: from.dayOfMonth, daysToAdjust: 30
            ).simple
            let holidays = [2, 3].map { offset in
                converter.getNepaliCalendarAfterAdditionOrSubtraction(
                    year: from.year, month: from.month, dayOfMonth: from.dayOfMonth, daysToAdjust: Int32(offset)
                ).simple
            }

            LabeledValue(
                label: "Working days over 30 days",
                value: "\(converter.workingDaysBetween(start: from, end: plusThirty, provider: NoOpHolidayProvider.shared, weekend: weekend))"
            )
            LabeledValue(
                label: "Next working day",
                value: converter.nextWorkingDay(from: from, provider: NoOpHolidayProvider.shared, weekend: weekend).text
            )
            LabeledValue(
                label: "+5 working days",
                value: converter.addWorkingDays(from: from, days: 5, provider: NoOpHolidayProvider.shared, weekend: weekend).text
            )
            LabeledValue(
                label: "+5 working days, with holidays",
                value: converter.addWorkingDays(from: from, days: 5, provider: SampleHolidayProvider(holidays: holidays), weekend: weekend).text
            )
        }
    }

    private var comparisonSection: some View {
        DemoSection(title: "Comparisons", subtitle: "compareDates returns the sign of the difference.") {
            let today = converter.todayNepaliCalendar
            let later = converter.getNepaliCalendarAfterAdditionOrSubtraction(
                year: today.year, month: today.month, dayOfMonth: today.dayOfMonth, daysToAdjust: 10
            )
            let result = converter.compareDates(dateToCompareFrom: today, dateToCompareTo: later)
            LabeledValue(label: "today vs +10 days", value: "\(result) (\(result < 0 ? "earlier" : "later or equal"))")
        }
    }

    private var digitSection: some View {
        DemoSection(title: "Digit scripts", subtitle: "Localize any numeric string between Latin and Devanagari.") {
            LabeledValue(label: "2081 to Devanagari", value: converter.localizeDigits("2081", script: .devanagari))
            LabeledValue(label: "Script from a locale", value: converter.localizeDigits("1234567890", locale: SampleDefaults.nepaliText))
            LabeledValue(label: "२०८१ to Latin", value: converter.toLatinDigits("२०८१"))
        }
    }

    private var namesSection: some View {
        DemoSection(title: "Names", subtitle: "Month and weekday names in either language and length.") {
            LabeledValue(label: "BS month 1, full", value: converter.getMonthName(month: 1, format: .full, language: .english))
            LabeledValue(label: "BS month 1, Nepali", value: converter.getMonthName(month: 1, format: .full, language: .nepali))
            LabeledValue(label: "AD month 3, medium", value: converter.getEnglishMonthName(month: 3, format: .medium, language: .english))
            LabeledValue(label: "Weekday 1, full", value: converter.getWeekdayName(dayOfWeek: 1, format: .full, language: .english))
            LabeledValue(label: "Weekday 7, Nepali", value: converter.getWeekdayName(dayOfWeek: 7, format: .full, language: .nepali))
        }
    }
}
