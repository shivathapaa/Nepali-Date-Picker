//
//  PolicyCompositionScreen.swift
//  Nepali Date Picker
//
//  Building a selection rule out of parts. A policy is one way to say which days are open; the
//  wrappers it is built from stay available, so a screen can start from a bare window and narrow it
//  one concern at a time.
//

import SwiftUI
import nepali_date_picker

struct PolicyCompositionScreen: View {
    @State private var windowed: CustomCalendar?
    @State private var weekendsOff: CustomCalendar?
    @State private var closuresOff: CustomCalendar?
    @State private var closuresOnly: CustomCalendar?
    @State private var merged: CustomCalendar?

    private let converter = NepaliDateConverter.shared

    private var data: SampleEventData { SampleEventData(today: converter.todayNepaliSimpleDate) }

    private var today: SimpleDate { converter.todayNepaliSimpleDate }

    /// The national list every rule below starts from.
    private var national: NepaliEventProvider {
        SampleCalendarEvents(data.events + data.festivalSpan)
    }

    /// One school's own closures, kept separate so the merge has two sides to join.
    private var school: NepaliEventProvider {
        SampleCalendarEvents([
            NepaliCalendarEvent(
                date: data.offset(5),
                name: "Founders Day (school)",
                kind: .regional,
                closesOffices: true,
                id: "founders-day",
                payload: nil
            ),
            NepaliCalendarEvent(
                date: data.offset(12),
                name: "Exam break (school)",
                kind: .regional,
                closesOffices: true,
                id: "exam-break",
                payload: nil
            )
        ])
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                windowSection
                weekendSection
                closureSection
                filteredSection
                mergedSection
                arithmeticSection
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 16)
        }
        .navigationTitle("Composing rules")
        .navigationBarTitleDisplayMode(.inline)
    }

    private var windowSection: some View {
        DemoSection(
            title: "Start from a window",
            subtitle: "AfterDateSelectable is the plainest rule there is: everything before today is out and everything after is in. Each section below adds one more condition to this same starting point."
        ) {
            AutoSized(measurementHeight: 560) { report in
                NepaliDatePickerView(
                    onHeightChange: report,
                    initialSelectedDate: nil,
                    selectableDates: futureOnly,
                    events: markingOptions
                ) { windowed = $0 }
            }
            SelectionSummary(selection: windowed)
        }
    }

    private var weekendSection: some View {
        DemoSection(
            title: "Take the week out",
            subtitle: "excludingWeekends narrows a rule by the days an institution never opens. The week is stated as weekday numbers, 1 for Sunday through 7 for Saturday, so Nepal's office week is [7]."
        ) {
            AutoSized(measurementHeight: 560) { report in
                NepaliDatePickerView(
                    onHeightChange: report,
                    initialSelectedDate: nil,
                    selectableDates: EventHelpersKt.excludingWeekends(
                        futureOnly,
                        weekend: [Weekday.saturday].boxedSet
                    ),
                    events: markingOptions
                ) { weekendsOff = $0 }
            }
            SelectionSummary(selection: weekendsOff)
        }
    }

    private var closureSection: some View {
        DemoSection(
            title: "Take the holidays out too",
            subtitle: "excludingClosures adds a provider's closing days on top. The two wrappers stack in either order, and neither one knows about the other, which is what lets a screen build its rule from whatever it happens to know."
        ) {
            AutoSized(measurementHeight: 560) { report in
                NepaliDatePickerView(
                    onHeightChange: report,
                    initialSelectedDate: nil,
                    selectableDates: EventHelpersKt.excludingClosures(
                        EventHelpersKt.excludingWeekends(
                            futureOnly,
                            weekend: [Weekday.saturday].boxedSet
                        ),
                        provider: national
                    ),
                    events: markingOptions
                ) { closuresOff = $0 }
            }
            SelectionSummary(selection: closuresOff)
        }
    }

    private var filteredSection: some View {
        DemoSection(
            title: "Only the kinds this screen cares about",
            subtitle: "filtered narrows a shared list before it is consulted, so a screen that only shuts for government holidays keeps the religious and regional days selectable while still seeing them marked."
        ) {
            AutoSized(measurementHeight: 560) { report in
                NepaliDatePickerView(
                    onHeightChange: report,
                    initialSelectedDate: nil,
                    selectableDates: EventHelpersKt.excludingClosures(
                        futureOnly,
                        provider: EventHelpersKt.filtered(national) { event in
                            KotlinBoolean(bool: event.kind == .governmentpublic)
                        }
                    ),
                    events: markingOptions
                ) { closuresOnly = $0 }
            }
            SelectionSummary(selection: closuresOnly)
        }
    }

    private var mergedSection: some View {
        DemoSection(
            title: "Two lists, one rule",
            subtitle: "plus joins two providers into one that answers from both, which is how an institution adds its own closures to the national list without either side being rewritten. The policy below is the school's week and both lists."
        ) {
            AutoSized(measurementHeight: 560) { report in
                NepaliDatePickerView(
                    onHeightChange: report,
                    initialSelectedDate: nil,
                    selectableDates: schoolPolicy.asSelectableDates(),
                    events: mergedMarkingOptions
                ) { merged = $0 }
            }
            SelectionSummary(selection: merged)
            ForEach(mergedRows, id: \.self) { row in
                LabeledValue(label: row.name, value: row.date)
            }
        }
    }

    private var arithmeticSection: some View {
        DemoSection(
            title: "The same composition, counting",
            subtitle: "A policy drives the arithmetic as well as the grid, so the days a rule blocks and the days a count skips are the same days by construction rather than by agreement."
        ) {
            let to = data.offset(21)
            LabeledValue(
                label: "Working days in 21, office",
                value: "\(converter.workingDaysBetween(start: today, end: to, policy: officePolicy))"
            )
            LabeledValue(
                label: "Working days in 21, school",
                value: "\(converter.workingDaysBetween(start: today, end: to, policy: schoolPolicy))"
            )
            LabeledValue(
                label: "Next working day, office",
                value: converter.nextWorkingDay(from: today, policy: officePolicy).text
            )
            LabeledValue(
                label: "Next working day, school",
                value: converter.nextWorkingDay(from: today, policy: schoolPolicy).text
            )
            LabeledValue(
                label: "+10 working days, school",
                value: converter.addWorkingDays(from: today, days: 10, policy: schoolPolicy).text
            )
        }
    }

    /// Today onwards, the window every rule on this screen narrows.
    private var futureOnly: NepaliSelectableDates {
        converter.AfterDateSelectable(simpleDate: today, includeDate: true)
    }

    /// Saturday off, the national list.
    private var officePolicy: NepaliCalendarPolicy {
        NepaliCalendarPolicy(weeklyOffDays: [Weekday.saturday].boxedSet, provider: national)
    }

    /// Saturday and Sunday off, the national list plus the school's own.
    private var schoolPolicy: NepaliCalendarPolicy {
        NepaliCalendarPolicy(
            weeklyOffDays: [Weekday.saturday, Weekday.sunday].boxedSet,
            provider: EventHelpersKt.plus(national, other: school)
        )
    }

    /// The national list as marking, so every grid above shows what its rule is reacting to.
    private var markingOptions: NepaliEventOptions {
        let options = NepaliEventOptions()
        options.weeklyOffDays = [Weekday.saturday].boxed
        options.events = (data.events + data.festivalSpan).map { $0.asEventInfo() }
        return options
    }

    /// Both lists as marking, for the merged section.
    private var mergedMarkingOptions: NepaliEventOptions {
        let options = NepaliEventOptions()
        options.weeklyOffDays = [Weekday.saturday, Weekday.sunday].boxed
        options.events = (data.events + data.festivalSpan).map { $0.asEventInfo() }
            + school.events(year: today.year).map { $0.asEventInfo() }
        return options
    }

    /// One row per named thing the merged policy knows about this month, folded by shared id.
    private var mergedRows: [EventRow] {
        var seen = Set<String>()
        return schoolPolicy.eventsIn(year: today.year, month: today.month).compactMap { entry in
            let key = entry.id ?? entry.name
            guard seen.insert(key).inserted else { return nil }
            return EventRow(name: entry.name, date: entry.date.text)
        }
    }
}

#Preview {
    NavigationStack { PolicyCompositionScreen() }
}
