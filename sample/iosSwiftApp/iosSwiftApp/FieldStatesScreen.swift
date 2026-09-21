//
//  FieldStatesScreen.swift
//  Nepali Date Picker
//
//  What a typed field can be told about itself: the error it is in, the text under it, whether it
//  accepts input at all, and the words on the buttons of the calendar behind it.
//

import SwiftUI
import nepali_date_picker

struct FieldStatesScreen: View {
    @State private var validated: SimpleDate?
    @State private var touched = false
    @State private var patterned: [SimpleDate?] = Array(repeating: nil, count: 4)
    @State private var rangeStart: SimpleDate?
    @State private var rangeEnd: SimpleDate?

    private let converter = NepaliDateConverter.shared

    /// Every input pattern the library takes, in the order the enum declares them.
    private let patterns: [(NepaliDateFormatter.Pattern, String)] = [
        (.yyyySlashMmSlashDd, "YYYY/MM/DD"),
        (.yyyyDashMmDashDd, "YYYY-MM-DD"),
        (.ddSlashMmSlashYyyy, "DD/MM/YYYY"),
        (.ddDashMmDashYyyy, "DD-MM-YYYY")
    ]

    /// A rule the validation demo judges against, so the error state has a reason to appear.
    private var futureOnly: NepaliSelectableDates {
        converter.AfterDateSelectable(simpleDate: converter.todayNepaliSimpleDate, includeDate: true)
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                validationSection
                patternsSection
                disabledSection
                readOnlySection
                dialogWordsSection
                rangeStatesSection
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 16)
        }
        .navigationTitle("Field states")
        .navigationBarTitleDisplayMode(.inline)
    }

    private var validationSection: some View {
        DemoSection(
            title: "Error and supporting text",
            subtitle: "A field reports null until what is typed is a complete date that passes the rule it was given. Drive isError from that, and put the reason in supportingText. This one only accepts today or later."
        ) {
            AutoSized(measurementHeight: 160) { report in
                NepaliDateFieldView(
                    onHeightChange: report,
                    initialValue: nil,
                    selectableDates: futureOnly,
                    label: "Delivery date",
                    supportingText: showError
                        ? "Enter a complete date from today onwards"
                        : "Format: YYYY/MM/DD",
                    isError: showError
                ) { value in
                    touched = true
                    validated = value
                }
            }
            .id(showError)
            LabeledValue(label: "Accepted", value: validated?.text ?? "none")
        }
    }

    private var showError: Bool { touched && validated == nil }

    private var patternsSection: some View {
        DemoSection(
            title: "Every input pattern",
            subtitle: "The pattern fixes the field order and the separator for both the mask and the text written back. NepaliDateFormatter formats and parses the same four without a field at all."
        ) {
            ForEach(Array(patterns.enumerated()), id: \.offset) { index, entry in
                AutoSized(measurementHeight: 140) { report in
                    NepaliDateFieldView(
                        onHeightChange: report,
                        initialValue: nil,
                        dateFormat: entry.0,
                        label: entry.1
                    ) { patterned[index] = $0 }
                }
                LabeledValue(label: entry.1, value: patterned[index]?.text ?? "incomplete")
            }
            Divider().padding(.horizontal, 12)
            ForEach(Array(patterns.enumerated()), id: \.offset) { _, entry in
                LabeledValue(
                    label: "format \(entry.1)",
                    value: NepaliDateFormatter.shared.format(
                        date: converter.todayNepaliSimpleDate,
                        pattern: entry.0,
                        script: .latin
                    )
                )
            }
            LabeledValue(
                label: "Devanagari digits",
                value: NepaliDateFormatter.shared.format(
                    date: converter.todayNepaliSimpleDate,
                    pattern: .yyyySlashMmSlashDd,
                    script: .devanagari
                )
            )
            LabeledValue(
                label: "parse 2081/06/12",
                value: NepaliDateFormatter.shared
                    .parse(input: "2081/06/12", pattern: .yyyySlashMmSlashDd)?.text ?? "no match"
            )
            LabeledValue(
                label: "parse the same as DD/MM",
                value: NepaliDateFormatter.shared
                    .parse(input: "2081/06/12", pattern: .ddSlashMmSlashYyyy)?.text ?? "no match"
            )
        }
    }

    private var disabledSection: some View {
        DemoSection(
            title: "Disabled",
            subtitle: "enabled = false greys the field and stops both typing and the calendar button, for a form waiting on something else to be filled in first."
        ) {
            AutoSized(measurementHeight: 140) { report in
                NepaliDateFieldView(
                    onHeightChange: report,
                    initialValue: SimpleDate(year: 2081, month: 2, dayOfMonth: 8),
                    label: "Locked date",
                    supportingText: "Unlocked once the form above is complete",
                    enabled: false
                ) { _ in }
            }
        }
    }

    private var readOnlySection: some View {
        DemoSection(
            title: "Read only",
            subtitle: "readOnly keeps the field looking ordinary and focusable but refuses edits, which is the shape a confirmation screen wants: the value is legible and copyable, and it cannot be changed by accident."
        ) {
            AutoSized(measurementHeight: 140) { report in
                NepaliDateFieldView(
                    onHeightChange: report,
                    initialValue: converter.todayNepaliSimpleDate,
                    label: "Booked for",
                    readOnly: true
                ) { _ in }
            }
        }
    }

    private var dialogWordsSection: some View {
        DemoSection(
            title: "The filled style and its own words",
            subtitle: "outlined = false gives the filled Material style, whose trailing icon opens a calendar. The two button labels and the corner radius are the field's own, so a localized app never shows an English OK."
        ) {
            AutoSized(measurementHeight: 140) { report in
                NepaliDateFieldView(
                    onHeightChange: report,
                    initialValue: nil,
                    locale: SampleDefaults.nepali,
                    outlined: false,
                    label: "मिति",
                    placeholder: "वर्ष/महिना/गते",
                    confirmButtonText: "ठिक छ",
                    dismissButtonText: "रद्द",
                    cornerRadius: 20
                ) { _ in }
            }
        }
    }

    private var rangeStatesSection: some View {
        DemoSection(
            title: "A range pair in every state",
            subtitle: "The two sides carry their own labels and their own error flags, because a range is wrong on one end at a time. This one marks the end red until it is on or after the start."
        ) {
            AutoSized(measurementHeight: 180) { report in
                NepaliDateRangeFieldView(
                    onHeightChange: report,
                    initialStartValue: nil,
                    initialEndValue: nil,
                    outlined: false,
                    startLabel: "Leave from",
                    endLabel: "Leave until",
                    supportingText: endBeforeStart
                        ? "The end cannot fall before the start"
                        : "Both sides accept Devanagari digits",
                    isStartError: false,
                    isEndError: endBeforeStart,
                    cornerRadius: 20
                ) { start, end in
                    rangeStart = start
                    rangeEnd = end
                }
            }
            .id(endBeforeStart)
            LabeledValue(label: "Start", value: rangeStart?.text ?? "incomplete")
            LabeledValue(label: "End", value: rangeEnd?.text ?? "incomplete")
        }
    }

    /// True once both ends are complete and the end is the earlier of the two.
    private var endBeforeStart: Bool {
        guard let start = rangeStart, let end = rangeEnd else { return false }
        return converter.getNepaliDaysInBetween(startDate: start, endDate: end) < 0
    }
}

#Preview {
    NavigationStack { FieldStatesScreen() }
}
