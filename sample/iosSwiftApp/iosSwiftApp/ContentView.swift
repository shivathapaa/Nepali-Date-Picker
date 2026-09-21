//
//  ContentView.swift
//  Nepali Date Picker
//

import SwiftUI
import nepali_date_picker

struct ContentView: View {
    private let converter = NepaliDateConverter.shared

    @StateObject private var appearance = AppearanceModel()
    @Environment(\.colorScheme) private var systemColorScheme

    /// What the pickers are actually drawing at, which is the device style until the menu pins one.
    private var dark: Bool {
        switch appearance.brightness {
        case .system: return systemColorScheme == .dark
        case .light: return false
        case .dark: return true
        }
    }

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

                Section("Pickers") {
                    NavigationLink("Pickers") { PickersScreen() }
                    NavigationLink("Calendar switch") { CalendarSystemScreen() }
                    NavigationLink("Wheel & Docked") { WheelDockedScreen() }
                    NavigationLink("Dialogs") { DialogsScreen() }
                    NavigationLink("Chrome and dimensions") { ChromeOptionsScreen() }
                    NavigationLink("Customization") { CustomizationScreen() }
                }

                Section("Fields") {
                    NavigationLink("Text fields") { FieldsScreen() }
                    NavigationLink("Field states") { FieldStatesScreen() }
                    NavigationLink("Selectable dates") { SelectableDatesScreen() }
                    NavigationLink("Composing rules") { PolicyCompositionScreen() }
                }

                Section("Calendar") {
                    NavigationLink("Month calendar") { CalendarScreen() }
                }

                Section("Events") {
                    NavigationLink("Events") { EventsScreen() }
                    NavigationLink("Event queries") { EventQueriesScreen() }
                }

                Section("Engine") {
                    NavigationLink("Utilities") { UtilitiesScreen() }
                    NavigationLink("Engine queries") { EngineQueriesScreen() }
                }
            }
            .navigationTitle("Nepali Date Picker")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    SampleAppearanceMenu(appearance: appearance, dark: dark)
                }
            }
        }
        .preferredColorScheme(appearance.brightness.preferredColorScheme)
        .tint(appearance.tint(dark: dark))
        .task { appearance.apply(systemDark: systemColorScheme == .dark) }
        .onChange(of: appearance.palette) { appearance.apply(systemDark: systemColorScheme == .dark) }
        .onChange(of: appearance.brightness) { appearance.apply(systemDark: systemColorScheme == .dark) }
        .onChange(of: systemColorScheme) { appearance.apply(systemDark: systemColorScheme == .dark) }
    }
}

#Preview {
    ContentView()
}
