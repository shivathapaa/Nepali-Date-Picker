//
//  SelectableDatesScreen.swift
//  Nepali Date Picker
//
//  Restricting which dates can be chosen, through the library helpers and a Swift policy.
//

import SwiftUI
import nepali_date_picker

struct SelectableDatesScreen: View {
    @State private var afterToday: CustomCalendar?
    @State private var beforeToday: CustomCalendar?
    @State private var inRange: CustomCalendar?
    @State private var evenDays: CustomCalendar?

    private let converter = NepaliDateConverter.shared

    private var today: SimpleDate { converter.todayNepaliSimpleDate }

    private var rangeStart: SimpleDate { converter.todayNepaliCalendar.simple }

    private var rangeEnd: SimpleDate {
        converter.getNepaliCalendarAfterAdditionOrSubtraction(
            year: today.year,
            month: today.month,
            dayOfMonth: today.dayOfMonth,
            daysToAdjust: 30
        ).simple
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                DemoSection(
                    title: "Future dates only",
                    subtitle: "AfterDateSelectable disables everything up to and including today."
                ) {
                    AutoSized(measurementHeight: 560) { report in
                        NepaliDatePickerView(
                            onHeightChange: report,
                            initialSelectedDate: nil,
                            selectableDates: converter.AfterDateSelectable(
                                simpleDate: today,
                                includeDate: false
                            )
                        ) { afterToday = $0 }
                    }
                    SelectionSummary(selection: afterToday)
                }

                DemoSection(
                    title: "Past dates only",
                    subtitle: "BeforeDateSelectable with the boundary day kept selectable."
                ) {
                    AutoSized(measurementHeight: 560) { report in
                        NepaliDatePickerView(
                            onHeightChange: report,
                            initialSelectedDate: nil,
                            selectableDates: converter.BeforeDateSelectable(
                                simpleDate: today,
                                includeDate: true
                            )
                        ) { beforeToday = $0 }
                    }
                    SelectionSummary(selection: beforeToday)
                }

                DemoSection(
                    title: "A bounded window",
                    subtitle: "DateRangeSelectable limits the picker to the next thirty days."
                ) {
                    AutoSized(measurementHeight: 560) { report in
                        NepaliDatePickerView(
                            onHeightChange: report,
                            initialSelectedDate: nil,
                            selectableDates: converter.DateRangeSelectable(
                                minDate: rangeStart,
                                maxDate: rangeEnd,
                                includeMinDate: true,
                                includeMaxDate: true
                            )
                        ) { inRange = $0 }
                    }
                    LabeledValue(label: "Window", value: "\(rangeStart.text) to \(rangeEnd.text)")
                    SelectionSummary(selection: inRange)
                }

                DemoSection(
                    title: "A policy written in Swift",
                    subtitle: "NepaliSelectableDates is a Kotlin interface, so the app can implement it directly. This one allows even days only."
                ) {
                    AutoSized(measurementHeight: 560) { report in
                        NepaliDatePickerView(
                            onHeightChange: report,
                            initialSelectedDate: nil,
                            selectableDates: EvenDaysOnly()
                        ) { evenDays = $0 }
                    }
                    SelectionSummary(selection: evenDays)
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 16)
        }
        .navigationTitle("Selectable dates")
    }
}
