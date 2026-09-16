//
//  WheelDockedScreen.swift
//  Nepali Date Picker
//
//  The wheel picker and the docked field that opens a calendar popup.
//

import SwiftUI
import nepali_date_picker

struct WheelDockedScreen: View {
    @State private var wheel: CustomCalendar?
    @State private var nepaliWheel: CustomCalendar?
    @State private var docked: CustomCalendar?
    @State private var dockedShort: CustomCalendar?

    private let preselectedDate = SimpleDate(year: 2081, month: 6, dayOfMonth: 10)

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                DemoSection(
                    title: "Wheel picker",
                    subtitle: "Scrolling year, month and day columns. Starts on today when no date is given."
                ) {
                    AutoSized(measurementHeight: 240) { report in
                        NepaliWheelDatePickerView(onHeightChange: report, initialDate: nil) { wheel = $0 }
                    }
                    SelectionSummary(selection: wheel)
                }

                DemoSection(
                    title: "Wheel, pre-selected and localized",
                    subtitle: "Opens on Ashoj 10, 2081 with Nepali labels and Devanagari digits."
                ) {
                    AutoSized(measurementHeight: 240) { report in
                        NepaliWheelDatePickerView(
                            onHeightChange: report,
                            initialDate: preselectedDate,
                            locale: SampleDefaults.nepali
                        ) { nepaliWheel = $0 }
                    }
                    SelectionSummary(selection: nepaliWheel)
                }

                DemoSection(
                    title: "Docked picker",
                    subtitle: "A compact field sized to its content. A Compose popup is clipped to its host, so raise measurementHeight if you want the calendar to open inline."
                ) {
                    AutoSized { report in
                        NepaliDatePickerDockedView(onHeightChange: report, initialSelectedDate: preselectedDate) { docked = $0 }
                    }
                    SelectionSummary(selection: docked)
                }

                DemoSection(
                    title: "Docked with a compact format",
                    subtitle: "The same field writing the selection in the compact year-first style."
                ) {
                    AutoSized { report in
                        NepaliDatePickerDockedView(
                            onHeightChange: report,
                            initialSelectedDate: nil,
                            dateFormatStyle: .compactYmd,
                            showTodayButton: false
                        ) { dockedShort = $0 }
                    }
                    SelectionSummary(selection: dockedShort)
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 16)
        }
        .navigationTitle("Wheel & Docked")
    }
}
