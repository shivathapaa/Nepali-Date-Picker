//
//  CustomizationScreen.swift
//  Nepali Date Picker
//
//  Locale driven customization: language, format style, name formats and digit script.
//

import SwiftUI
import nepali_date_picker

struct CustomizationScreen: View {
    @State private var language: NepaliDatePickerLang = .english
    @State private var dateFormat: NepaliDateFormatStyle = .long_
    @State private var weekDayName: NameFormat = .short_
    @State private var monthName: NameFormat = .full
    @State private var digitScript: DigitScript?
    @State private var showModeToggle = true
    @State private var showTodayButton = true
    @State private var selection: CustomCalendar?

    private var locale: NepaliDateLocale {
        NepaliDateLocale(
            language: language,
            dateFormat: dateFormat,
            weekDayName: weekDayName,
            monthName: monthName,
            digitScript: digitScript
        )
    }

    private let today = NepaliDateConverter.shared.todayNepaliCalendar

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                DemoSection(
                    title: "Locale",
                    subtitle: "Every knob below feeds one NepaliDateLocale, which drives both the picker and the formatter."
                ) {
                    Picker("Language", selection: $language) {
                        Text("English").tag(NepaliDatePickerLang.english)
                        Text("Nepali").tag(NepaliDatePickerLang.nepali)
                    }
                    .pickerStyle(.segmented)
                    .padding(.horizontal, 12)

                    Picker("Weekdays", selection: $weekDayName) {
                        Text("S").tag(NameFormat.short_)
                        Text("Sun").tag(NameFormat.medium)
                    }
                    .pickerStyle(.segmented)
                    .padding(.horizontal, 12)

                    Picker("Months", selection: $monthName) {
                        Text("Full months").tag(NameFormat.full)
                        Text("Medium months").tag(NameFormat.medium)
                        Text("Short months").tag(NameFormat.short_)
                    }
                    .pickerStyle(.segmented)
                    .padding(.horizontal, 12)

                    Picker("Digits", selection: $digitScript) {
                        Text("Locale default").tag(DigitScript?.none)
                        Text("Latin").tag(DigitScript?.some(.latin))
                        Text("Devanagari").tag(DigitScript?.some(.devanagari))
                    }
                    .pickerStyle(.segmented)
                    .padding(.horizontal, 12)
                }

                DemoSection(
                    title: "Format style",
                    subtitle: "How a formatted date is written out. The result updates live below."
                ) {
                    Picker("Format", selection: $dateFormat) {
                        Text("Full").tag(NepaliDateFormatStyle.full)
                        Text("Long").tag(NepaliDateFormatStyle.long_)
                        Text("Medium").tag(NepaliDateFormatStyle.medium)
                        Text("Short Y-M-D").tag(NepaliDateFormatStyle.shortYmd)
                        Text("Short M-D-Y").tag(NepaliDateFormatStyle.shortMdy)
                        Text("Compact Y-M-D").tag(NepaliDateFormatStyle.compactYmd)
                        Text("Compact M-D-Y").tag(NepaliDateFormatStyle.compactMdy)
                    }
                    .pickerStyle(.menu)
                    .padding(.horizontal, 12)

                    LabeledValue(
                        label: "Today",
                        value: NepaliDateConverter.shared.formatNepaliDate(
                            customCalendar: today,
                            locale: locale
                        )
                    )
                }

                DemoSection(
                    title: "Picker chrome",
                    subtitle: "Toggle the calendar/input switch and the shortcut back to today."
                ) {
                    Toggle("Show mode toggle", isOn: $showModeToggle)
                        .padding(.horizontal, 12)
                    Toggle("Show today button", isOn: $showTodayButton)
                        .padding(.horizontal, 12)
                }

                DemoSection(
                    title: "Live picker",
                    subtitle: "The picker rebuilt with the settings above."
                ) {
                    AutoSized(initialHeight: 560) { report in
                        NepaliDatePickerView(
                            onHeightChange: report,
                            initialSelectedDate: nil,
                            locale: locale,
                            showModeToggle: showModeToggle,
                            showTodayButton: showTodayButton
                        ) { selection = $0 }
                    }
                    // A changed locale must rebuild the hosted controller, which SwiftUI only does
                    // when the view identity changes.
                    .id("\(language)-\(dateFormat)-\(weekDayName)-\(monthName)-\(String(describing: digitScript))-\(showModeToggle)-\(showTodayButton)")
                    SelectionSummary(selection: selection)
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 16)
        }
        .navigationTitle("Customization")
    }
}
