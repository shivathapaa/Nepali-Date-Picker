//
//  ContentView.swift
//  Nepali Date Picker
//

import SwiftUI
import nepali_date_picker

struct ContentView: View {
    private let converter = NepaliDateConverter.shared

    var body: some View {
        NavigationStack {
            List {
                Section("Today") {
                    LabeledValue(
                        label: "Bikram Sambat",
                        value: converter.formatNepaliDate(
                            customCalendar: converter.todayNepaliCalendar,
                            locale: SampleDefaults.englishText
                        )
                    )
                    LabeledValue(
                        label: "नेपाली",
                        value: converter.formatNepaliDate(
                            customCalendar: converter.todayNepaliCalendar,
                            locale: SampleDefaults.nepaliText
                        )
                    )
                    LabeledValue(label: "Gregorian", value: converter.todayEnglishSimpleDate.text)
                }

                Section("Showcase") {
                    NavigationLink("Pickers") { PickersScreen() }
                    NavigationLink("Calendar switch") { CalendarSystemScreen() }
                    NavigationLink("Wheel & Docked") { WheelDockedScreen() }
                    NavigationLink("Dialogs") { DialogsScreen() }
                    NavigationLink("Text fields") { FieldsScreen() }
                    NavigationLink("Customization") { CustomizationScreen() }
                    NavigationLink("Selectable dates") { SelectableDatesScreen() }
                    NavigationLink("Utilities") { UtilitiesScreen() }
                }
            }
            .navigationTitle("Nepali Date Picker")
        }
    }
}

#Preview {
    ContentView()
}
