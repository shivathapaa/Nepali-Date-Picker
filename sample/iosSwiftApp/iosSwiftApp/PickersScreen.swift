//
//  PickersScreen.swift
//  Nepali Date Picker
//
//  The full calendar picker, including a pre-selected date and the paired Gregorian variant.
//

import SwiftUI
import nepali_date_picker

struct PickersScreen: View {
    @State private var plain: CustomCalendar?
    @State private var preselected: CustomCalendar?
    @State private var withEnglish: CustomCalendar?
    @State private var nepaliLocale: CustomCalendar?
    @State private var range: (CustomCalendar?, CustomCalendar?) = (nil, nil)

    /// A fixed date so the pre-selection demo does not move with the calendar.
    private let preselectedDate = SimpleDate(year: 2081, month: 1, dayOfMonth: 15)

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                DemoSection(
                    title: "Default picker",
                    subtitle: "No initial selection. The mode toggle switches between calendar and typed input."
                ) {
                    AutoSized(measurementHeight: 560) { report in
                        NepaliDatePickerView(onHeightChange: report, initialSelectedDate: nil) { plain = $0 }
                    }
                    SelectionSummary(selection: plain)
                }

                DemoSection(
                    title: "Pre-selected date",
                    subtitle: "Opens on Baisakh 15, 2081 with that date already chosen and its month displayed."
                ) {
                    AutoSized(measurementHeight: 560) { report in
                        NepaliDatePickerView(onHeightChange: report, initialSelectedDate: preselectedDate) { preselected = $0 }
                    }
                    SelectionSummary(selection: preselected)
                }

                DemoSection(
                    title: "With the Gregorian date",
                    subtitle: "Every Bikram Sambat day is annotated with its English equivalent."
                ) {
                    AutoSized(measurementHeight: 620) { report in
                        NepaliDatePickerView(
                            onHeightChange: report,
                            initialSelectedDate: preselectedDate,
                            showEnglishDate: true
                        ) { withEnglish = $0 }
                    }
                    SelectionSummary(selection: withEnglish)
                    if let english = withEnglish {
                        LabeledValue(
                            label: "Gregorian",
                            value: NepaliDateConverter.shared.convertNepaliToEnglish(
                                nepaliYYYY: english.year,
                                nepaliMM: english.month,
                                nepaliDD: english.dayOfMonth
                            ).text
                        )
                    }
                }

                DemoSection(
                    title: "Nepali language",
                    subtitle: "The same picker localized, with Devanagari month names and digits."
                ) {
                    AutoSized(measurementHeight: 560) { report in
                        NepaliDatePickerView(
                            onHeightChange: report,
                            initialSelectedDate: nil,
                            locale: SampleDefaults.nepali
                        ) { nepaliLocale = $0 }
                    }
                    SelectionSummary(selection: nepaliLocale)
                }

                DemoSection(
                    title: "Range picker",
                    subtitle: "Pick a start and an end. Months are stacked vertically."
                ) {
                    AutoSized(measurementHeight: 640) { report in
                        NepaliDateRangePickerView(
                            onHeightChange: report,
                            initialSelectedStartDate: preselectedDate,
                            initialSelectedEndDate: SimpleDate(year: 2081, month: 1, dayOfMonth: 25),
                            locale: SampleDefaults.englishRange
                        ) { start, end in range = (start, end) }
                    }
                    LabeledValue(label: "Start", value: range.0?.text ?? "none")
                    LabeledValue(label: "End", value: range.1?.text ?? "none")
                }

                DemoSection(
                    title: "Range with Gregorian dates",
                    subtitle: "The range calendar paired with English dates and horizontal paging."
                ) {
                    AutoSized(measurementHeight: 640) { report in
                        NepaliDateRangePickerView(
                            onHeightChange: report,
                            initialSelectedStartDate: nil,
                            initialSelectedEndDate: nil,
                            locale: SampleDefaults.englishRange,
                            showMonthsVertically: false,
                            showEnglishDate: true
                        ) { _, _ in }
                    }
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 16)
        }
        .navigationTitle("Pickers")
    }
}
