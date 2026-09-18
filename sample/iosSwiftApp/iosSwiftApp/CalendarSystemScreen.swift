//
//  CalendarSystemScreen.swift
//  Nepali Date Picker
//
//  Showing either calendar in the same picker, and filling the grid's empty cells with the
//  neighbouring months.
//

import SwiftUI
import nepali_date_picker

struct CalendarSystemScreen: View {
    @State private var switchable: CustomCalendar?
    @State private var gregorianFirst: CustomCalendar?
    @State private var dual: CustomCalendar?
    @State private var filled: CustomCalendar?
    @State private var external: CustomCalendar?
    @State private var externalSystem: CalendarSystem = .bikramSambat
    @State private var wheel: CustomCalendar?
    @State private var docked: CustomCalendar?
    @State private var typed: SimpleDate?
    @State private var range: (CustomCalendar?, CustomCalendar?) = (nil, nil)

    /// BS 2083-06-01 is AD 2026-09-17, so a switch here visibly lands on September.
    private let seed = SimpleDate(year: 2083, month: 6, dayOfMonth: 1)

    /// Shrawan 2083 needs all six rows, so its borrowed days are easy to spot.
    private let tallMonth = SimpleDate(year: 2083, month: 4, dayOfMonth: 15)

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                DemoSection(
                    title: "With the B.S. / A.D. switch",
                    subtitle: "One grid, either calendar. Switching changes only the display: the same day stays selected and the date reported back is always Bikram Sambat."
                ) {
                    AutoSized(measurementHeight: 580) { report in
                        NepaliDatePickerView(
                            onHeightChange: report,
                            initialSelectedDate: seed,
                            showCalendarSystemToggle: true
                        ) { switchable = $0 }
                    }
                    SelectionSummary(selection: switchable)
                }

                DemoSection(
                    title: "Gregorian-first, no switch",
                    subtitle: "Opens on the Gregorian grid for an English-first screen. Days before AD 1913-04-13 have no Bikram Sambat equivalent and stay inert."
                ) {
                    AutoSized(measurementHeight: 580) { report in
                        NepaliDatePickerView(
                            onHeightChange: report,
                            initialSelectedDate: seed,
                            initialCalendarSystem: .gregorian
                        ) { gregorianFirst = $0 }
                    }
                    SelectionSummary(selection: gregorianFirst)
                }

                DemoSection(
                    title: "Both calendars, switchable",
                    subtitle: "Every day paired with its counterpart, plus a switch for which calendar leads."
                ) {
                    AutoSized(measurementHeight: 640) { report in
                        NepaliDatePickerView(
                            onHeightChange: report,
                            initialSelectedDate: seed,
                            showEnglishDate: true,
                            showCalendarSystemToggle: true
                        ) { dual = $0 }
                    }
                    SelectionSummary(selection: dual)
                }

                DemoSection(
                    title: "Neighbouring months in the empty cells",
                    subtitle: "The blank cells hold the days either side of the month, drawn faded. Tapping one picks that day and moves the grid to its month."
                ) {
                    AutoSized(measurementHeight: 580) { report in
                        NepaliDatePickerView(
                            onHeightChange: report,
                            initialSelectedDate: tallMonth,
                            showCalendarSystemToggle: true,
                            showAdjacentMonthDays: true
                        ) { filled = $0 }
                    }
                    SelectionSummary(selection: filled)
                }

                DemoSection(
                    title: "Switch outside the picker",
                    subtitle: "The switch on its own, driving the grid from the app's own chrome instead of from inside the picker."
                ) {
                    AutoSized(measurementHeight: 60) { report in
                        NepaliCalendarSystemToggleView(onHeightChange: report) { externalSystem = $0 }
                    }
                    AutoSized(measurementHeight: 580) { report in
                        NepaliDatePickerView(
                            onHeightChange: report,
                            initialSelectedDate: seed,
                            initialCalendarSystem: externalSystem
                        ) { external = $0 }
                        // The picker seeds its calendar once, so a new one is built per selection.
                        .id(externalSystem)
                    }
                    SelectionSummary(selection: external)
                }

                DemoSection(
                    title: "Wheel, switchable",
                    subtitle: "The wheels spin in either calendar. There is no grid here, so the fill option does not apply."
                ) {
                    AutoSized(measurementHeight: 320) { report in
                        NepaliWheelDatePickerView(
                            onHeightChange: report,
                            initialDate: seed,
                            showCalendarSystemToggle: true
                        ) { wheel = $0 }
                    }
                    SelectionSummary(selection: wheel)
                }

                DemoSection(
                    title: "Docked, Gregorian-first and filled",
                    subtitle: "The popup calendar opens on the Gregorian month with its edges filled; the field still writes the Bikram Sambat date."
                ) {
                    AutoSized(measurementHeight: 120) { report in
                        NepaliDatePickerDockedView(
                            onHeightChange: report,
                            initialSelectedDate: seed,
                            label: "Date (A.D.)",
                            initialCalendarSystem: .gregorian,
                            showCalendarSystemToggle: true,
                            showAdjacentMonthDays: true
                        ) { docked = $0 }
                    }
                    SelectionSummary(selection: docked)
                }

                DemoSection(
                    title: "Field typed in Gregorian",
                    subtitle: "Typed and displayed in Gregorian, reported in Bikram Sambat. The calendar its trailing icon opens carries the switch and the filled edges."
                ) {
                    AutoSized(measurementHeight: 120) { report in
                        NepaliDateFieldView(
                            onHeightChange: report,
                            initialValue: seed,
                            outlined: false,
                            label: "Date (A.D.)",
                            initialCalendarSystem: .gregorian,
                            showCalendarSystemToggle: true,
                            showAdjacentMonthDays: true
                        ) { typed = $0 }
                    }
                    LabeledValue(label: "Parsed (BS)", value: typed?.text ?? "none")
                }

                DemoSection(
                    title: "Range picker, switchable and filled",
                    subtitle: "The shaded band follows whichever calendar is on screen and reaches onto the borrowed days, so a range crossing a month edge stays whole."
                ) {
                    AutoSized(measurementHeight: 680) { report in
                        NepaliDateRangePickerView(
                            onHeightChange: report,
                            initialSelectedStartDate: SimpleDate(year: 2083, month: 4, dayOfMonth: 28),
                            initialSelectedEndDate: SimpleDate(year: 2083, month: 5, dayOfMonth: 4),
                            locale: SampleDefaults.englishRange,
                            showMonthsVertically: false,
                            showCalendarSystemToggle: true,
                            showAdjacentMonthDays: true
                        ) { start, end in range = (start, end) }
                    }
                    LabeledValue(label: "Start", value: range.0?.text ?? "none")
                    LabeledValue(label: "End", value: range.1?.text ?? "none")
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 16)
        }
        .navigationTitle("Calendar switch")
    }
}
