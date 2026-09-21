//
//  ChromeOptionsScreen.swift
//  Nepali Date Picker
//
//  The dimensions and chrome each surface takes: how tall a wheel row is, how far a popup floats,
//  what a docked field writes, and which parts of a range calendar are drawn at all.
//

import SwiftUI
import nepali_date_picker

struct ChromeOptionsScreen: View {
    @State private var wheelCompact: CustomCalendar?
    @State private var wheelRoomy: CustomCalendar?
    @State private var docked: CustomCalendar?
    @State private var narrowed: CustomCalendar?
    @State private var rangeStripped: (CustomCalendar?, CustomCalendar?) = (nil, nil)
    @State private var rangePaired: (CustomCalendar?, CustomCalendar?) = (nil, nil)
    @State private var dialogStyle: DialogStyle?
    @State private var confirmed: CustomCalendar?
    @State private var formatStyle: NepaliDateFormatStyle = .medium

    private let converter = NepaliDateConverter.shared
    private let seed = SimpleDate(year: 2081, month: 6, dayOfMonth: 10)

    /// Which styled dialog is on screen, or nil for none.
    enum DialogStyle: Identifiable {
        case square
        case pillowed

        var id: Self { self }

        /// Corner radius of the dialog surface, in points.
        var cornerRadius: Float { self == .square ? 2 : 36 }

        /// Tonal elevation of the dialog surface, in points.
        var tonalElevation: Float { self == .square ? 0 : 16 }

        var title: String { self == .square ? "Square and flat" : "Rounded and raised" }
    }

    /// The seven ways a formatted date can be written, for the docked field's readout.
    private let formatStyles: [(NepaliDateFormatStyle, String)] = [
        (.full, "Full"),
        (.long_, "Long"),
        (.medium, "Medium"),
        (.shortYmd, "Short Y-M-D"),
        (.shortMdy, "Short M-D-Y"),
        (.compactYmd, "Compact Y-M-D"),
        (.compactMdy, "Compact M-D-Y")
    ]

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                wheelSection
                dockedFormatSection
                dockedPopupSection
                narrowedRangeSection
                strippedRangeSection
                pairedRangeSection
                dialogSection
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 16)
        }
        .navigationTitle("Chrome and dimensions")
        .navigationBarTitleDisplayMode(.inline)
        .overlay {
            if let style = dialogStyle {
                NepaliDatePickerDialogView(
                    initialSelectedDate: seed,
                    title: style.title,
                    tonalElevation: style.tonalElevation,
                    cornerRadius: style.cornerRadius,
                    onConfirm: { date in
                        confirmed = date
                        dialogStyle = nil
                    },
                    onDismiss: { dialogStyle = nil }
                )
                .ignoresSafeArea()
            }
        }
    }

    private var wheelSection: some View {
        DemoSection(
            title: "How big a wheel is",
            subtitle: "itemHeight is the height of one row and visibleItemCount how many are on screen, so the two together set the wheel's height. An odd count keeps the selected row centred."
        ) {
            LabeledValue(label: "Compact", value: "32 pt rows, 3 visible")
            AutoSized(measurementHeight: 200) { report in
                NepaliWheelDatePickerView(
                    onHeightChange: report,
                    initialDate: seed,
                    itemHeight: 32,
                    visibleItemCount: 3,
                    cornerRadius: 4
                ) { wheelCompact = $0 }
            }
            SelectionSummary(selection: wheelCompact)

            LabeledValue(label: "Roomy", value: "56 pt rows, 7 visible")
            AutoSized(measurementHeight: 460) { report in
                NepaliWheelDatePickerView(
                    onHeightChange: report,
                    initialDate: seed,
                    locale: SampleDefaults.nepali,
                    itemHeight: 56,
                    visibleItemCount: 7,
                    cornerRadius: 32
                ) { wheelRoomy = $0 }
            }
            SelectionSummary(selection: wheelRoomy)
        }
    }

    private var dockedFormatSection: some View {
        DemoSection(
            title: "What a docked field writes",
            subtitle: "dateFormatStyle decides the shape of the text in the field. A compact style keeps a narrow column on one line; a full style spells the weekday out."
        ) {
            Picker("Format", selection: $formatStyle) {
                ForEach(Array(formatStyles.enumerated()), id: \.offset) { _, entry in
                    Text(entry.1).tag(entry.0)
                }
            }
            .pickerStyle(.menu)
            .padding(.horizontal, 12)

            AutoSized(measurementHeight: 140) { report in
                NepaliDatePickerDockedView(
                    onHeightChange: report,
                    initialSelectedDate: seed,
                    dateFormatStyle: formatStyle,
                    label: "Appointment"
                ) { docked = $0 }
            }
            .id(formatStyle)
            SelectionSummary(selection: docked)

            ForEach(Array(formatStyles.enumerated()), id: \.offset) { _, entry in
                LabeledValue(
                    label: entry.1,
                    value: converter.formatNepaliDate(
                        customCalendar: converter.getNepaliCalendar(
                            nepaliYYYY: seed.year, nepaliMM: seed.month, nepaliDD: seed.dayOfMonth
                        ),
                        locale: NepaliDateLocale(
                            language: .english,
                            dateFormat: entry.0,
                            weekDayName: .full,
                            monthName: .full,
                            digitScript: nil
                        )
                    )
                )
            }
        }
    }

    private var dockedPopupSection: some View {
        DemoSection(
            title: "How the popup sits",
            subtitle: "cornerRadius shapes the field and popupShadowElevation says how far the calendar floats above the page. A Compose popup is clipped to its host, so raise measurementHeight to see it open inline."
        ) {
            AutoSized(measurementHeight: 520) { report in
                NepaliDatePickerDockedView(
                    onHeightChange: report,
                    initialSelectedDate: nil,
                    dateFormatStyle: .long_,
                    showTodayButton: false,
                    label: "Pick a day",
                    placeholder: "Nothing chosen yet",
                    cornerRadius: 28,
                    popupShadowElevation: 24
                ) { _ in }
            }
        }
    }

    private var narrowedRangeSection: some View {
        DemoSection(
            title: "A narrowed year range",
            subtitle: "yearRange bounds the year list and clamps the grid. Five years is the window a booking screen wants, and the state clamps rather than throws when it is handed something outside."
        ) {
            let today = converter.todayNepaliSimpleDate
            AutoSized(measurementHeight: 560) { report in
                NepaliDatePickerView(
                    onHeightChange: report,
                    initialSelectedDate: today,
                    yearRange: (today.year - 2)...(today.year + 2)
                ) { narrowed = $0 }
            }
            LabeledValue(label: "Offered years", value: "\(today.year - 2) to \(today.year + 2)")
            LabeledValue(
                label: "Gregorian equivalent",
                value: gregorianSpan(first: today.year - 2, last: today.year + 2)
            )
            SelectionSummary(selection: narrowed)
        }
    }

    private var strippedRangeSection: some View {
        DemoSection(
            title: "A range calendar with its chrome off",
            subtitle: "showModeToggle hides the calendar / typed-input switch and showYearPickerAndMonthNavigation hides the header controls, which leaves a scrolling wall of months: the shape a long-stay booking wants."
        ) {
            AutoSized(measurementHeight: 640) { report in
                NepaliDateRangePickerView(
                    onHeightChange: report,
                    initialSelectedStartDate: seed,
                    initialSelectedEndDate: SimpleDate(year: 2081, month: 6, dayOfMonth: 20),
                    locale: SampleDefaults.englishRange,
                    showModeToggle: false,
                    showTodayButton: false,
                    showYearPickerAndMonthNavigation: false
                ) { start, end in rangeStripped = (start, end) }
            }
            LabeledValue(label: "Start", value: rangeStripped.0?.text ?? "none")
            LabeledValue(label: "End", value: rangeStripped.1?.text ?? "none")
        }
    }

    private var pairedRangeSection: some View {
        DemoSection(
            title: "A range calendar showing both calendars",
            subtitle: "showEnglishDate pairs every cell with its Gregorian day, and englishDateLocale formats that half on its own, so the small number can stay Latin while the large one is Devanagari."
        ) {
            AutoSized(measurementHeight: 700) { report in
                NepaliDateRangePickerView(
                    onHeightChange: report,
                    initialSelectedStartDate: seed,
                    initialSelectedEndDate: nil,
                    locale: SampleDefaults.nepali,
                    showMonthsVertically: false,
                    showEnglishDate: true,
                    englishDateLocale: SampleDefaults.englishRange
                ) { start, end in rangePaired = (start, end) }
            }
            LabeledValue(label: "Start", value: rangePaired.0?.text ?? "none")
            LabeledValue(label: "End", value: rangePaired.1?.text ?? "none")
        }
    }

    private var dialogSection: some View {
        DemoSection(
            title: "The dialog's own surface",
            subtitle: "cornerRadius and tonalElevation shape the floating panel. The full-screen variant ignores both, because it has no floating surface to shape."
        ) {
            Button("Square and flat") { dialogStyle = .square }
                .buttonStyle(.bordered)
                .padding(.horizontal, 12)
            Button("Rounded and raised") { dialogStyle = .pillowed }
                .buttonStyle(.bordered)
                .padding(.horizontal, 12)
            LabeledValue(label: "Confirmed", value: confirmed?.text ?? "none")
        }
    }

    /// The English years a Bikram Sambat window maps onto, so both calendars page the same days.
    private func gregorianSpan(first: Int32, last: Int32) -> String {
        let range = NepaliCalendarDefaults.shared.gregorianYearRangeFor(
            nepaliYearRange: KotlinIntRange(start: first, endInclusive: last)
        )
        return "\(range.first) to \(range.last)"
    }
}

#Preview {
    NavigationStack { ChromeOptionsScreen() }
}
