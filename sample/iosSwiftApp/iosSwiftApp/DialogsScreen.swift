//
//  DialogsScreen.swift
//  Nepali Date Picker
//
//  The modal and full-screen dialog variants, presented over SwiftUI.
//

import SwiftUI
import nepali_date_picker

struct DialogsScreen: View {
    @State private var showingDialog = false
    @State private var showingFullScreen = false
    @State private var showingNepaliDialog = false
    @State private var showingSwitchableDialog = false
    @State private var confirmed: CustomCalendar?

    private let preselectedDate = SimpleDate(year: 2081, month: 3, dayOfMonth: 5)

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                DemoSection(
                    title: "Dialog",
                    subtitle: "The calendar inside the library's own dialog, with confirm and dismiss buttons."
                ) {
                    Button("Open dialog") { showingDialog = true }
                        .buttonStyle(.borderedProminent)
                        .padding(.horizontal, 12)
                    LabeledValue(label: "Confirmed", value: confirmed?.text ?? "none")
                }

                DemoSection(
                    title: "Full-screen dialog",
                    subtitle: "The edge-to-edge variant, better suited to small screens."
                ) {
                    Button("Open full-screen dialog") { showingFullScreen = true }
                        .buttonStyle(.bordered)
                        .padding(.horizontal, 12)
                }

                DemoSection(
                    title: "Localized dialog",
                    subtitle: "The same dialog in Nepali, opening on a pre-selected date."
                ) {
                    Button("Open Nepali dialog") { showingNepaliDialog = true }
                        .buttonStyle(.bordered)
                        .padding(.horizontal, 12)
                }

                DemoSection(
                    title: "Switchable dialog",
                    subtitle: "The hosted calendar carries the B.S. / A.D. switch, both calendars per cell, and the neighbouring months filling its edges."
                ) {
                    Button("Open switchable dialog") { showingSwitchableDialog = true }
                        .buttonStyle(.bordered)
                        .padding(.horizontal, 12)
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 16)
        }
        .navigationTitle("Dialogs")
        .fullScreenCover(isPresented: $showingDialog) {
            NepaliDatePickerDialogView(
                initialSelectedDate: preselectedDate,
                onConfirm: { date in
                    confirmed = date
                    showingDialog = false
                },
                onDismiss: { showingDialog = false }
            )
            .ignoresSafeArea()
            .background(.clear)
        }
        .fullScreenCover(isPresented: $showingFullScreen) {
            NepaliDatePickerDialogView(
                initialSelectedDate: nil,
                fullScreen: true,
                title: "Pick a date",
                onConfirm: { date in
                    confirmed = date
                    showingFullScreen = false
                },
                onDismiss: { showingFullScreen = false }
            )
            .ignoresSafeArea()
        }
        .fullScreenCover(isPresented: $showingNepaliDialog) {
            NepaliDatePickerDialogView(
                initialSelectedDate: preselectedDate,
                locale: SampleDefaults.nepali,
                confirmText: "ठिक छ",
                dismissText: "रद्द",
                onConfirm: { date in
                    confirmed = date
                    showingNepaliDialog = false
                },
                onDismiss: { showingNepaliDialog = false }
            )
            .ignoresSafeArea()
        }
        .fullScreenCover(isPresented: $showingSwitchableDialog) {
            NepaliDatePickerDialogView(
                initialSelectedDate: SimpleDate(year: 2083, month: 4, dayOfMonth: 15),
                showEnglishDate: true,
                showCalendarSystemToggle: true,
                showAdjacentMonthDays: true,
                onConfirm: { date in
                    confirmed = date
                    showingSwitchableDialog = false
                },
                onDismiss: { showingSwitchableDialog = false }
            )
            .ignoresSafeArea()
            .background(.clear)
        }
    }
}
