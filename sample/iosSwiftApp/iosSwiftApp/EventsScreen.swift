//
//  EventsScreen.swift
//  Nepali Date Picker
//
//  Marking days from Swift: the week an institution never opens, the events it keeps, and the
//  colours and dots they are drawn with.
//

import SwiftUI
import nepali_date_picker

struct EventsScreen: View {
    @State private var marked: CustomCalendar?
    @State private var schoolDay: CustomCalendar?
    @State private var branded: CustomCalendar?
    @State private var dotted: CustomCalendar?
    @State private var docked: CustomCalendar?
    @State private var rangeStart: CustomCalendar?
    @State private var rangeEnd: CustomCalendar?
    @State private var typed: SimpleDate?
    @State private var markWeeklyOff = true
    @State private var markEvents = true
    @State private var tintContainer = false
    @State private var indicateWeeklyOff = false
    @State private var describeEvents = true

    private let converter = NepaliDateConverter.shared

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                officeSection
                schoolSection
                switchesSection
                paletteSection
                dotsSection
                dockedSection
                rangeSection
                fieldSection
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 16)
        }
        .navigationTitle("Events")
        .navigationBarTitleDisplayMode(.inline)
    }

    /// The days this screen marks: three named days a few days out from today.
    private var sampleEvents: [NepaliCalendarEvent] {
        SampleEventData(today: converter.todayNepaliSimpleDate).events
    }

    private func options(
        weeklyOffDays: [Int32] = [Weekday.saturday],
        events: [NepaliEventInfo]? = nil
    ) -> NepaliEventOptions {
        let options = NepaliEventOptions()
        options.weeklyOffDays = weeklyOffDays.boxed
        options.events = events ?? sampleEvents.map { $0.asEventInfo() }
        return options
    }

    private var officeSection: some View {
        DemoSection(
            title: "An office week",
            subtitle: "weeklyOffDays is 1 for Sunday through 7 for Saturday, so Nepal's office week is [7]. The week colours its days and never dots them, which leaves the dots for what is scheduled."
        ) {
            AutoSized(measurementHeight: 560) { report in
                NepaliDatePickerView(
                    onHeightChange: report,
                    initialSelectedDate: nil,
                    events: options()
                ) { marked = $0 }
            }
            SelectionSummary(selection: marked)
        }
    }

    private var schoolSection: some View {
        DemoSection(
            title: "A school week",
            subtitle: "The same call with [7, 1]: Saturday and Sunday both closed, and the same event list underneath."
        ) {
            AutoSized(measurementHeight: 560) { report in
                NepaliDatePickerView(
                    onHeightChange: report,
                    initialSelectedDate: nil,
                    events: options(weeklyOffDays: [Weekday.saturday, Weekday.sunday])
                ) { schoolDay = $0 }
            }
            SelectionSummary(selection: schoolDay)
        }
    }

    private var switchesSection: some View {
        DemoSection(
            title: "What the marking may draw",
            subtitle: "Every switch a display style has, live. markWeeklyOff and markEvents turn the two colour channels off entirely, tintContainer gives a closed day a disc as well as a coloured number, indicateWeeklyOff spends a dot on the week, and describeEvents adds the names after the date for a screen reader."
        ) {
            VStack(alignment: .leading, spacing: 6) {
                Toggle("markWeeklyOff", isOn: $markWeeklyOff)
                Toggle("markEvents", isOn: $markEvents)
                Toggle("tintContainer", isOn: $tintContainer)
                Toggle("indicateWeeklyOff", isOn: $indicateWeeklyOff)
                Toggle("describeEvents", isOn: $describeEvents)
            }
            .font(.subheadline)
            .padding(.horizontal, 12)

            AutoSized(measurementHeight: 560) { report in
                NepaliDatePickerView(
                    onHeightChange: report,
                    initialSelectedDate: nil,
                    events: switchedOptions
                ) { _ in }
            }
            // The hosted controller reads its options once, and the representable has no update
            // path, so a flipped switch needs a new view identity to take effect.
            .id("\(markWeeklyOff)-\(markEvents)-\(tintContainer)-\(indicateWeeklyOff)-\(describeEvents)")
        }
    }

    private var switchedOptions: NepaliEventOptions {
        let options = options()
        options.markWeeklyOff = markWeeklyOff
        options.markEvents = markEvents
        options.tintContainer = tintContainer
        options.indicateWeeklyOff = indicateWeeklyOff
        options.describeEvents = describeEvents
        return options
    }

    private var paletteSection: some View {
        DemoSection(
            title: "Colours of your own",
            subtitle: "Compose colours do not cross the Objective-C bridge, so a colour arrives as 0xAARRGGBB and 0 keeps whatever the theme resolved. Every slot is overridable one at a time."
        ) {
            AutoSized(measurementHeight: 560) { report in
                NepaliDatePickerView(
                    onHeightChange: report,
                    initialSelectedDate: nil,
                    events: brandedOptions
                ) { branded = $0 }
            }
            SelectionSummary(selection: branded)
        }
    }

    private var brandedOptions: NepaliEventOptions {
        let options = options()
        options.weeklyOffColorArgb = argb(0xFF8E24AA)
        options.publicHolidayColorArgb = argb(0xFFD32F2F)
        options.religiousColorArgb = argb(0xFF1E88E5)
        options.regionalColorArgb = argb(0xFF00897B)
        options.observanceColorArgb = argb(0xFF6D4C41)
        options.markedContainerColorArgb = argb(0x22D32F2F)
        options.tintContainer = true
        return options
    }

    private var dotsSection: some View {
        DemoSection(
            title: "Dots are the app's own events",
            subtitle: "An entry with indicate set draws a dot in its own colour. A cell carries three at most, so a day with four events still reads cleanly."
        ) {
            AutoSized(measurementHeight: 560) { report in
                NepaliDatePickerView(
                    onHeightChange: report,
                    initialSelectedDate: nil,
                    events: dottedOptions
                ) { dotted = $0 }
            }
            SelectionSummary(selection: dotted)
        }
    }

    private var dottedOptions: NepaliEventOptions {
        let data = SampleEventData(today: converter.todayNepaliSimpleDate)
        let options = NepaliEventOptions()
        options.weeklyOffDays = [Weekday.saturday].boxed
        options.events = data.events.map { $0.asEventInfo() }
            + data.ownEvents.enumerated().map { index, event in
                event.asEventInfo(colorArgb: SampleEventData.dotColors[index % SampleEventData.dotColors.count], indicate: true)
            }
        return options
    }

    private var dockedSection: some View {
        DemoSection(
            title: "The docked field",
            subtitle: "Every factory that hosts a calendar takes the same options, so the dropdown marks its days identically."
        ) {
            AutoSized(measurementHeight: 420) { report in
                NepaliDatePickerDockedView(
                    onHeightChange: report,
                    initialSelectedDate: nil,
                    events: options(),
                    label: "Pick a day"
                ) { docked = $0 }
            }
            SelectionSummary(selection: docked)
        }
    }

    private var rangeSection: some View {
        DemoSection(
            title: "The range calendar",
            subtitle: "Marks survive a range selection: inside a chosen range the shading leads and the marking follows it."
        ) {
            AutoSized(measurementHeight: 620) { report in
                NepaliDateRangePickerView(
                    onHeightChange: report,
                    initialSelectedStartDate: nil,
                    initialSelectedEndDate: nil,
                    events: options(),
                    showMonthsVertically: false
                ) { start, end in
                    rangeStart = start
                    rangeEnd = end
                }
            }
            LabeledValue(
                label: "Range",
                value: rangeStart.map { "\($0.text) to \(rangeEnd?.text ?? "...")" } ?? "none"
            )
        }
    }

    private var fieldSection: some View {
        DemoSection(
            title: "The text field's own calendar",
            subtitle: "A field types or picks, and the calendar behind its trailing icon is a full picker, so it marks the same days."
        ) {
            AutoSized(measurementHeight: 220) { report in
                NepaliDateFieldView(
                    onHeightChange: report,
                    initialValue: nil,
                    events: options(),
                    label: "Date"
                ) { typed = $0 }
            }
            LabeledValue(label: "Typed or picked", value: typed?.text ?? "none")
        }
    }
}

#Preview {
    NavigationStack { EventsScreen() }
}
