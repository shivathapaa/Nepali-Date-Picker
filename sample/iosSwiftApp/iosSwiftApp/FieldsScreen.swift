//
//  FieldsScreen.swift
//  Nepali Date Picker
//
//  Typed date entry: the outlined and filled single fields, and the range pair.
//

import SwiftUI
import nepali_date_picker

struct FieldsScreen: View {
    @State private var outlined: SimpleDate?
    @State private var filled: SimpleDate?
    @State private var dayFirst: SimpleDate?
    @State private var rangeStart: SimpleDate?
    @State private var rangeEnd: SimpleDate?

    private let preselectedDate = SimpleDate(year: 2081, month: 2, dayOfMonth: 8)

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                DemoSection(
                    title: "Outlined field",
                    subtitle: "Typed entry masked to YYYY/MM/DD, pre-filled and validated as you type."
                ) {
                    AutoSized { report in
                        NepaliDateFieldView(onHeightChange: report, initialValue: preselectedDate) { outlined = $0 }
                    }
                    LabeledValue(label: "Parsed", value: outlined?.text ?? "incomplete")
                }

                DemoSection(
                    title: "Filled field",
                    subtitle: "The same entry in the filled Material style, starting empty."
                ) {
                    AutoSized { report in
                        NepaliDateFieldView(onHeightChange: report, initialValue: nil, outlined: false) { filled = $0 }
                    }
                    LabeledValue(label: "Parsed", value: filled?.text ?? "incomplete")
                }

                DemoSection(
                    title: "Day-first pattern, in Nepali",
                    subtitle: "A DD-MM-YYYY mask with Devanagari digits."
                ) {
                    AutoSized { report in
                        NepaliDateFieldView(
                            onHeightChange: report,
                            initialValue: nil,
                            locale: SampleDefaults.nepali,
                            dateFormat: .ddDashMmDashYyyy
                        ) { dayFirst = $0 }
                    }
                    LabeledValue(label: "Parsed", value: dayFirst?.text ?? "incomplete")
                }

                DemoSection(
                    title: "Range fields",
                    subtitle: "Start and end entry side by side, rejecting an end before the start."
                ) {
                    AutoSized { report in
                        NepaliDateRangeFieldView(
                            onHeightChange: report,
                            initialStartValue: preselectedDate,
                            initialEndValue: nil
                        ) { start, end in
                            rangeStart = start
                            rangeEnd = end
                        }
                    }
                    LabeledValue(label: "Start", value: rangeStart?.text ?? "incomplete")
                    LabeledValue(label: "End", value: rangeEnd?.text ?? "incomplete")
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 16)
        }
        .navigationTitle("Text fields")
    }
}
