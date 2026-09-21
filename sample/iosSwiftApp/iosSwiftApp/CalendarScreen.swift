//
//  CalendarScreen.swift
//  Nepali Date Picker
//
//  The browsable month calendar from Swift: a grid that fills its frame, the picked day written
//  out, the month listed under it, and the record each event carries in its payload.
//

import SwiftUI
import nepali_date_picker

struct CalendarScreen: View {
    @State private var picked: CustomCalendar?
    @State private var status: NepaliDayStatus?
    @State private var tapped: NepaliCalendarEvent?

    private let data = SampleEventData(today: NepaliDateConverter.shared.todayNepaliSimpleDate)

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                calendarSection
                payloadSection
                plainSection
                schoolSection
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 16)
        }
        .navigationTitle("Calendar")
        .navigationBarTitleDisplayMode(.inline)
    }

    /// The events this screen marks: the national closures plus the app's own records.
    private func options(weeklyOffDays: [Int32] = [Weekday.saturday]) -> NepaliEventOptions {
        let options = NepaliEventOptions()
        options.weeklyOffDays = weeklyOffDays.boxed
        options.events = (data.events + data.richEvents).map { $0.asEventInfo() }
        return options
    }

    private var calendarSection: some View {
        DemoSection(
            title: "A calendar and what is on it",
            subtitle: "The day's summary and the month's list are stacked inside the one hosted controller, because a calendar and a list in two controllers cannot share a selection."
        ) {
            AutoSized(measurementHeight: 1400) { report in
                NepaliCalendarView(
                    onHeightChange: report,
                    events: options(),
                    showDaySummary: true,
                    showMonthEvents: true,
                    onDaySelected: { day, dayStatus in
                        picked = day
                        status = dayStatus
                    },
                    onEventTapped: { tapped = $0 }
                )
            }
            SelectionSummary(selection: picked)
            LabeledValue(
                label: "Day",
                value: status.map { $0.isNonWorking ? "Closed" : "Working day" } ?? "none"
            )
        }
    }

    private var payloadSection: some View {
        DemoSection(
            title: "What an event can carry",
            subtitle: "The library hands an event back with its id and payload untouched. Everything below the name is this sample reading its own JSON out of that payload."
        ) {
            if let tapped, let payload = SampleEventPayload.decode(from: tapped) {
                VStack(alignment: .leading, spacing: 6) {
                    Label(tapped.name, systemImage: payload.symbolName)
                        .font(.headline)
                        .foregroundStyle(Color(argbValue: UInt32(truncatingIfNeeded: payload.accentArgb)))
                    Text(payload.description).font(.subheadline)
                    if let venue = payload.venue { LabeledValue(label: "Where", value: venue) }
                    if let organizer = payload.organizer { LabeledValue(label: "Who", value: organizer) }
                    if let banner = payload.bannerUrl { LabeledValue(label: "Banner", value: banner) }
                    if let icon = payload.iconUrl { LabeledValue(label: "Icon", value: icon) }
                    if let link = payload.link { LabeledValue(label: "Link", value: link) }
                    LabeledValue(label: "Correlates to", value: tapped.id ?? "no id")
                    if !payload.tags.isEmpty {
                        Text(payload.tags.joined(separator: " · "))
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            } else {
                Text("Tap a line of the month's list above.")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
        }
    }

    private var plainSection: some View {
        DemoSection(
            title: "One calendar at a time",
            subtitle: "Without the second number a cell carries one date, and the switch turns the whole grid Gregorian without changing what is picked."
        ) {
            AutoSized(measurementHeight: 620) { report in
                NepaliCalendarView(
                    onHeightChange: report,
                    events: options(),
                    showCalendarSystemToggle: true,
                    showSecondaryDates: false,
                    onDaySelected: { day, _ in picked = day }
                )
            }
        }
    }

    private var schoolSection: some View {
        DemoSection(
            title: "A school's week",
            subtitle: "The same calendar under an institution closed Saturday and Sunday."
        ) {
            AutoSized(measurementHeight: 900) { report in
                NepaliCalendarView(
                    onHeightChange: report,
                    events: options(weeklyOffDays: [Weekday.saturday, Weekday.sunday]),
                    showDaySummary: true,
                    onDaySelected: { day, dayStatus in
                        picked = day
                        status = dayStatus
                    }
                )
            }
        }
    }
}
