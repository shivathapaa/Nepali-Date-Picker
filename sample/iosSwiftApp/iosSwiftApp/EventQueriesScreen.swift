//
//  EventQueriesScreen.swift
//  Nepali Date Picker
//
//  The half the picker never draws: what a day is, what a month holds, how many working days lie
//  between two dates, and an event that runs longer than a day.
//

import SwiftUI
import nepali_date_picker

struct EventQueriesScreen: View {
    @State private var inspected: CustomCalendar?
    @State private var blocked: CustomCalendar?

    private let converter = NepaliDateConverter.shared

    private var data: SampleEventData { SampleEventData(today: converter.todayNepaliSimpleDate) }

    /// The office: Saturday off, plus the named days and the festival span.
    private var officePolicy: NepaliCalendarPolicy {
        NepaliCalendarPolicy(
            weeklyOffDays: [Weekday.saturday].boxedSet,
            provider: SampleCalendarEvents(data.events + data.festivalSpan)
        )
    }

    /// The same list without a weekly rule, for showing what the events alone do to the counting.
    private var eventsOnlyPolicy: NepaliCalendarPolicy {
        NepaliCalendarPolicy(
            weeklyOffDays: [],
            provider: SampleCalendarEvents(data.festivalSpan)
        )
    }

    /// A span of observances: named on every day it covers, closing none of them.
    private var leavePolicy: NepaliCalendarPolicy {
        NepaliCalendarPolicy(
            weeklyOffDays: [],
            provider: SampleCalendarEvents(data.leaveSpan)
        )
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                spanSection
                inspectSection
                monthSection
                countingSection
                blockingSection
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 16)
        }
        .navigationTitle("Event queries")
        .navigationBarTitleDisplayMode(.inline)
    }

    private var spanSection: some View {
        DemoSection(
            title: "An event that runs longer than a day",
            subtitle: "spanningDays(days:) and spanningThrough(end:) turn one event into the per-day entries the calendar reads. Each keeps the name, kind, closesOffices and id of the event it came from, so an agenda folds them back by that id."
        ) {
            let festival = data.festivalSpan
            let leave = data.leaveSpan
            LabeledValue(label: "Festival entries", value: "\(festival.count)")
            LabeledValue(label: "First day", value: festival.first?.date.text ?? "none")
            LabeledValue(label: "Last day", value: festival.last?.date.text ?? "none")
            LabeledValue(label: "Closes the office", value: festival.contains { $0.closesOffices } ? "yes" : "no")
            LabeledValue(label: "Leave entries", value: "\(leave.count)")
            LabeledValue(label: "Leave closes the office", value: leave.contains { $0.closesOffices } ? "yes" : "no")
        }
    }

    private var inspectSection: some View {
        DemoSection(
            title: "What the selected day is",
            subtitle: "The picker draws a colour and dots and no text. statusOf answers the rest: whether the week closes the day, whether anything named on it does, and what those things are."
        ) {
            AutoSized(measurementHeight: 560) { report in
                NepaliDatePickerView(
                    onHeightChange: report,
                    initialSelectedDate: nil,
                    events: markingOptions
                ) { inspected = $0 }
            }
            if let day = inspected?.simple {
                let status = officePolicy.statusOf(date: day)
                LabeledValue(label: "Day", value: day.text)
                LabeledValue(label: "Weekly off", value: status.isWeeklyOff ? "yes" : "no")
                LabeledValue(label: "Non-working", value: status.isNonWorking ? "yes" : "no")
                LabeledValue(label: "Strongest kind", value: status.primaryKind?.name ?? "none")
                LabeledValue(
                    label: "Named on it",
                    value: status.names.isEmpty ? "nothing" : status.names.joined(separator: ", ")
                )
                LabeledValue(label: "Closures", value: "\(status.closures.count)")
            } else {
                LabeledValue(label: "Day", value: "pick one")
            }
        }
    }

    private var monthSection: some View {
        DemoSection(
            title: "A whole month at once",
            subtitle: "monthStatus resolves the month's first weekday once and walks forward, so a wall-patro list costs one call rather than one per cell. eventsIn lists the same month in date order."
        ) {
            let month = inspected?.simple ?? converter.todayNepaliSimpleDate
            let status = officePolicy.monthStatus(year: month.year, month: month.month)
            let entries = officePolicy.eventsIn(year: month.year, month: month.month)
            LabeledValue(label: "Month", value: "\(month.year)/\(month.month)")
            LabeledValue(label: "Days", value: "\(status.count)")
            LabeledValue(label: "Closed days", value: "\(status.filter(\.isNonWorking).count)")
            LabeledValue(label: "Weekly off days", value: "\(status.filter(\.isWeeklyOff).count)")
            LabeledValue(label: "Day entries", value: "\(entries.count)")
            ForEach(uniqueRows(entries), id: \.self) { row in
                LabeledValue(label: row.name, value: row.date)
            }
        }
    }

    private var countingSection: some View {
        DemoSection(
            title: "Counting working days",
            subtitle: "The same policy drives the arithmetic, so a span of closures takes its days out of the count while a span of observances leaves them in."
        ) {
            let from = converter.todayNepaliSimpleDate
            let to = data.offset(21)
            LabeledValue(
                label: "Working days in 21, office",
                value: "\(converter.workingDaysBetween(start: from, end: to, policy: officePolicy))"
            )
            LabeledValue(
                label: "Working days in 21, festival only",
                value: "\(converter.workingDaysBetween(start: from, end: to, policy: eventsOnlyPolicy))"
            )
            LabeledValue(
                label: "Working days in 21, leave only",
                value: "\(converter.workingDaysBetween(start: from, end: to, policy: leavePolicy))"
            )
            LabeledValue(
                label: "Next working day",
                value: converter.nextWorkingDay(from: from, policy: officePolicy).text
            )
            LabeledValue(
                label: "+5 working days",
                value: converter.addWorkingDays(from: from, days: 5, policy: officePolicy).text
            )
            LabeledValue(
                label: "-5 working days",
                value: converter.addWorkingDays(from: from, days: -5, policy: officePolicy).text
            )
        }
    }

    private var blockingSection: some View {
        DemoSection(
            title: "Marking never blocks on its own",
            subtitle: "asSelectableDates() turns the same policy into a picker rule, which is the only thing that stops a day being chosen. The festival's ten days go with it; the leave's do not, because an observance leaves the day worked."
        ) {
            AutoSized(measurementHeight: 560) { report in
                NepaliDatePickerView(
                    onHeightChange: report,
                    initialSelectedDate: nil,
                    selectableDates: officePolicy.asSelectableDates(),
                    events: markingOptions
                ) { blocked = $0 }
            }
            SelectionSummary(selection: blocked)
        }
    }

    /// The same data as options, so the grid above each readout shows what the numbers describe.
    private var markingOptions: NepaliEventOptions {
        let options = NepaliEventOptions()
        options.weeklyOffDays = [Weekday.saturday].boxed
        options.events = (data.events + data.festivalSpan).map { $0.asEventInfo() }
        return options
    }

    /// One row per named thing in the month, folded by the id a span's days share.
    private func uniqueRows(_ entries: [NepaliCalendarEvent]) -> [EventRow] {
        var seen = Set<String>()
        return entries.compactMap { entry in
            let key = entry.id ?? entry.name
            guard seen.insert(key).inserted else { return nil }
            return EventRow(name: entry.name, date: entry.date.text)
        }
    }
}

/// One line of the month list: what it is called and the day it falls on.
struct EventRow: Hashable {
    let name: String
    let date: String
}

#Preview {
    NavigationStack { EventQueriesScreen() }
}
