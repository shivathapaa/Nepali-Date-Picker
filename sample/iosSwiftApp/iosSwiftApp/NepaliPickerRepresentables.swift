//
//  NepaliPickerRepresentables.swift
//  Nepali Date Picker
//
//  Bridges the library's Compose pickers into SwiftUI. Each Kotlin factory returns a
//  UIViewController hosting one picker, so every wrapper here is a thin representable.
//

import SwiftUI
import nepali_date_picker

/// Shared defaults so every screen agrees on the year range and locale.
enum SampleDefaults {
    static let yearRange: ClosedRange<Int32> = {
        let range = NepaliCalendarDefaults.shared.NepaliYearRange
        return range.first...range.last
    }()

    /// Corner radius for every typed field, in points. The Material default is nearly square, which
    /// reads as unfinished next to rounded SwiftUI controls.
    static let fieldCornerRadius: Float = 14

    static let english = NepaliDateLocale(
        language: .english,
        dateFormat: .long_,
        weekDayName: .short_,
        monthName: .full,
        digitScript: nil
    )

    static let nepali = NepaliDateLocale(
        language: .nepali,
        dateFormat: .long_,
        weekDayName: .short_,
        monthName: .full,
        digitScript: nil
    )

    /// Range headlines print both ends, which wraps badly at picker width. A numeric style keeps
    /// them on one line.
    static let englishRange = NepaliDateLocale(
        language: .english,
        dateFormat: .shortYmd,
        weekDayName: .short_,
        monthName: .full,
        digitScript: nil
    )

    // For dates rendered as plain text, where nothing has to fit a narrow column and the full
    // weekday name is the point.
    static let englishText = NepaliDateLocale(
        language: .english,
        dateFormat: .full,
        weekDayName: .full,
        monthName: .full,
        digitScript: nil
    )

    static let nepaliText = NepaliDateLocale(
        language: .nepali,
        dateFormat: .full,
        weekDayName: .full,
        monthName: .full,
        digitScript: nil
    )
}

/// The full calendar picker, optionally paired with its Gregorian equivalent.
struct NepaliDatePickerView: UIViewControllerRepresentable {
    var onHeightChange: (CGFloat) -> Void = { _ in }
    var initialSelectedDate: SimpleDate?
    var locale: NepaliDateLocale = SampleDefaults.english
    var yearRange: ClosedRange<Int32> = SampleDefaults.yearRange
    var selectableDates: NepaliSelectableDates?
    var showModeToggle: Bool = true
    var showTodayButton: Bool = true
    var showEnglishDate: Bool = false
    var englishDateLocale: NepaliDateLocale?
    var initialCalendarSystem: CalendarSystem = .bikramSambat
    var showCalendarSystemToggle: Bool = false
    var showAdjacentMonthDays: Bool = false
    var onDateSelected: (CustomCalendar?) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        let options = NepaliCalendarOptions()
        options.showModeToggle = showModeToggle
        options.showTodayButton = showTodayButton
        options.showEnglishDate = showEnglishDate
        options.englishDateLocale = englishDateLocale
        options.initialCalendarSystem = initialCalendarSystem
        options.showCalendarSystemToggle = showCalendarSystemToggle
        options.showAdjacentMonthDays = showAdjacentMonthDays

        return NepaliDatePickerViewControllersKt.NepaliDatePickerViewController(
            initialSelectedDate: initialSelectedDate,
            locale: locale,
            yearRangeStart: yearRange.lowerBound,
            yearRangeEnd: yearRange.upperBound,
            selectableDates: selectableDates,
            options: options,
            onHeightChange: { onHeightChange(CGFloat($0)) },
            onDateSelected: onDateSelected
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

/// The compact field that opens the calendar in a popup.
struct NepaliDatePickerDockedView: UIViewControllerRepresentable {
    var onHeightChange: (CGFloat) -> Void = { _ in }
    var initialSelectedDate: SimpleDate?
    var locale: NepaliDateLocale = SampleDefaults.english
    var yearRange: ClosedRange<Int32> = SampleDefaults.yearRange
    var selectableDates: NepaliSelectableDates?
    var dateFormatStyle: NepaliDateFormatStyle = .medium
    var showTodayButton: Bool = true
    var label: String?
    var placeholder: String?
    var cornerRadius: Float = SampleDefaults.fieldCornerRadius
    var popupShadowElevation: Float = 6
    var initialCalendarSystem: CalendarSystem = .bikramSambat
    var showCalendarSystemToggle: Bool = false
    var showAdjacentMonthDays: Bool = false
    var onDateSelected: (CustomCalendar?) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        let options = NepaliDockedOptions()
        options.dateFormatStyle = dateFormatStyle
        options.showTodayButton = showTodayButton
        options.label = label
        options.placeholder = placeholder
        options.cornerRadius = cornerRadius
        options.popupShadowElevation = popupShadowElevation
        options.initialCalendarSystem = initialCalendarSystem
        options.showCalendarSystemToggle = showCalendarSystemToggle
        options.showAdjacentMonthDays = showAdjacentMonthDays

        return NepaliDatePickerViewControllersKt.NepaliDatePickerDockedViewController(
            initialSelectedDate: initialSelectedDate,
            locale: locale,
            yearRangeStart: yearRange.lowerBound,
            yearRangeEnd: yearRange.upperBound,
            selectableDates: selectableDates,
            options: options,
            onHeightChange: { onHeightChange(CGFloat($0)) },
            onDateSelected: onDateSelected
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

/// The scrolling year/month/day wheel. Always has a selection.
struct NepaliWheelDatePickerView: UIViewControllerRepresentable {
    var onHeightChange: (CGFloat) -> Void = { _ in }
    var initialDate: SimpleDate?
    var locale: NepaliDateLocale = SampleDefaults.english
    var yearRange: ClosedRange<Int32> = SampleDefaults.yearRange
    var selectableDates: NepaliSelectableDates?
    var itemHeight: Float = 44
    var visibleItemCount: Int32 = 5
    var cornerRadius: Float = 20
    var initialCalendarSystem: CalendarSystem = .bikramSambat
    var showCalendarSystemToggle: Bool = false
    var onDateChange: (CustomCalendar) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        let options = NepaliWheelOptions()
        options.itemHeight = itemHeight
        options.visibleItemCount = visibleItemCount
        options.cornerRadius = cornerRadius
        options.initialCalendarSystem = initialCalendarSystem
        options.showCalendarSystemToggle = showCalendarSystemToggle

        return NepaliDatePickerViewControllersKt.NepaliWheelDatePickerViewController(
            initialDate: initialDate,
            locale: locale,
            yearRangeStart: yearRange.lowerBound,
            yearRangeEnd: yearRange.upperBound,
            selectableDates: selectableDates,
            options: options,
            onHeightChange: { onHeightChange(CGFloat($0)) },
            onDateChange: onDateChange
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

/// The range calendar, which tracks a start and an end date.
struct NepaliDateRangePickerView: UIViewControllerRepresentable {
    var onHeightChange: (CGFloat) -> Void = { _ in }
    var initialSelectedStartDate: SimpleDate?
    var initialSelectedEndDate: SimpleDate?
    var locale: NepaliDateLocale = SampleDefaults.english
    var yearRange: ClosedRange<Int32> = SampleDefaults.yearRange
    var selectableDates: NepaliSelectableDates?
    var showModeToggle: Bool = true
    var showTodayButton: Bool = true
    var showMonthsVertically: Bool = true
    var showYearPickerAndMonthNavigation: Bool = true
    var showEnglishDate: Bool = false
    var englishDateLocale: NepaliDateLocale?
    var initialCalendarSystem: CalendarSystem = .bikramSambat
    var showCalendarSystemToggle: Bool = false
    var showAdjacentMonthDays: Bool = false
    var onRangeSelected: (CustomCalendar?, CustomCalendar?) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        let options = NepaliRangeCalendarOptions()
        options.showModeToggle = showModeToggle
        options.showTodayButton = showTodayButton
        options.showMonthsVertically = showMonthsVertically
        options.showYearPickerAndMonthNavigation = showYearPickerAndMonthNavigation
        options.showEnglishDate = showEnglishDate
        options.englishDateLocale = englishDateLocale
        options.initialCalendarSystem = initialCalendarSystem
        options.showCalendarSystemToggle = showCalendarSystemToggle
        options.showAdjacentMonthDays = showAdjacentMonthDays

        return NepaliDateRangeViewControllersKt.NepaliDateRangePickerViewController(
            initialSelectedStartDate: initialSelectedStartDate,
            initialSelectedEndDate: initialSelectedEndDate,
            locale: locale,
            yearRangeStart: yearRange.lowerBound,
            yearRangeEnd: yearRange.upperBound,
            selectableDates: selectableDates,
            options: options,
            onHeightChange: { onHeightChange(CGFloat($0)) },
            onRangeSelected: onRangeSelected
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

/// A single typed date entry field, outlined or filled.
struct NepaliDateFieldView: UIViewControllerRepresentable {
    var onHeightChange: (CGFloat) -> Void = { _ in }
    var initialValue: SimpleDate?
    var locale: NepaliDateLocale = SampleDefaults.english
    var dateFormat: NepaliDateFormatter.Pattern = .yyyySlashMmSlashDd
    var yearRange: ClosedRange<Int32> = SampleDefaults.yearRange
    var selectableDates: NepaliSelectableDates?
    var outlined: Bool = true
    var label: String?
    var placeholder: String?
    var supportingText: String?
    var isError: Bool = false
    var enabled: Bool = true
    var readOnly: Bool = false
    var confirmButtonText: String?
    var dismissButtonText: String?
    var cornerRadius: Float = SampleDefaults.fieldCornerRadius
    var initialCalendarSystem: CalendarSystem = .bikramSambat
    var showCalendarSystemToggle: Bool = false
    var showAdjacentMonthDays: Bool = false
    var onValueChange: (SimpleDate?) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        let options = NepaliFieldOptions()
        options.outlined = outlined
        options.label = label
        options.placeholder = placeholder
        options.supportingText = supportingText
        options.isError = isError
        options.enabled = enabled
        options.readOnly = readOnly
        options.confirmButtonText = confirmButtonText
        options.dismissButtonText = dismissButtonText
        options.cornerRadius = cornerRadius
        options.initialCalendarSystem = initialCalendarSystem
        options.showCalendarSystemToggle = showCalendarSystemToggle
        options.showAdjacentMonthDays = showAdjacentMonthDays

        return NepaliDateFieldViewControllersKt.NepaliDateFieldViewController(
            initialValue: initialValue,
            locale: locale,
            dateFormat: dateFormat,
            yearRangeStart: yearRange.lowerBound,
            yearRangeEnd: yearRange.upperBound,
            selectableDates: selectableDates,
            options: options,
            onHeightChange: { onHeightChange(CGFloat($0)) },
            onValueChange: onValueChange
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

/// The paired start and end entry fields.
struct NepaliDateRangeFieldView: UIViewControllerRepresentable {
    var onHeightChange: (CGFloat) -> Void = { _ in }
    var initialStartValue: SimpleDate?
    var initialEndValue: SimpleDate?
    var locale: NepaliDateLocale = SampleDefaults.english
    var yearRange: ClosedRange<Int32> = SampleDefaults.yearRange
    var selectableDates: NepaliSelectableDates?
    var dateFormat: NepaliDateFormatter.Pattern = .yyyySlashMmSlashDd
    var outlined: Bool = true
    var startLabel: String?
    var endLabel: String?
    var supportingText: String?
    var isStartError: Bool = false
    var isEndError: Bool = false
    var enabled: Bool = true
    var readOnly: Bool = false
    var confirmButtonText: String?
    var dismissButtonText: String?
    var cornerRadius: Float = SampleDefaults.fieldCornerRadius
    var initialCalendarSystem: CalendarSystem = .bikramSambat
    var showCalendarSystemToggle: Bool = false
    var showAdjacentMonthDays: Bool = false
    var onRangeChange: (SimpleDate?, SimpleDate?) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        let options = NepaliRangeFieldOptions()
        options.outlined = outlined
        options.startLabel = startLabel
        options.endLabel = endLabel
        options.supportingText = supportingText
        options.isStartError = isStartError
        options.isEndError = isEndError
        options.enabled = enabled
        options.readOnly = readOnly
        options.confirmButtonText = confirmButtonText
        options.dismissButtonText = dismissButtonText
        options.cornerRadius = cornerRadius
        options.initialCalendarSystem = initialCalendarSystem
        options.showCalendarSystemToggle = showCalendarSystemToggle
        options.showAdjacentMonthDays = showAdjacentMonthDays

        return NepaliDateRangeViewControllersKt.NepaliDateRangeFieldViewController(
            initialStartValue: initialStartValue,
            initialEndValue: initialEndValue,
            locale: locale,
            dateFormat: dateFormat,
            yearRangeStart: yearRange.lowerBound,
            yearRangeEnd: yearRange.upperBound,
            selectableDates: selectableDates,
            options: options,
            onHeightChange: { onHeightChange(CGFloat($0)) },
            onRangeChange: onRangeChange
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

/// The modal dialog holding a calendar. It draws its own scrim, so put it in an `overlay`.
struct NepaliDatePickerDialogView: UIViewControllerRepresentable {
    var onHeightChange: (CGFloat) -> Void = { _ in }
    var initialSelectedDate: SimpleDate?
    var locale: NepaliDateLocale = SampleDefaults.english
    var yearRange: ClosedRange<Int32> = SampleDefaults.yearRange
    var selectableDates: NepaliSelectableDates?
    var fullScreen: Bool = false
    var title: String?
    var confirmText: String = "OK"
    var dismissText: String = "Cancel"
    var tonalElevation: Float = 6
    var cornerRadius: Float = 28
    var showEnglishDate: Bool = false
    var initialCalendarSystem: CalendarSystem = .bikramSambat
    var showCalendarSystemToggle: Bool = false
    var showAdjacentMonthDays: Bool = false
    var onConfirm: (CustomCalendar?) -> Void
    var onDismiss: () -> Void

    private var options: NepaliDialogOptions {
        let options = NepaliDialogOptions()
        options.title = title
        options.confirmText = confirmText
        options.dismissText = dismissText
        options.tonalElevation = tonalElevation
        options.cornerRadius = cornerRadius
        return options
    }

    /// Configures the calendar the dialog hosts, as opposed to the dialog chrome around it.
    private var calendarOptions: NepaliCalendarOptions {
        let options = NepaliCalendarOptions()
        options.showEnglishDate = showEnglishDate
        options.initialCalendarSystem = initialCalendarSystem
        options.showCalendarSystemToggle = showCalendarSystemToggle
        options.showAdjacentMonthDays = showAdjacentMonthDays
        return options
    }

    func makeUIViewController(context: Context) -> UIViewController {
        if fullScreen {
            return NepaliDateDialogViewControllersKt.NepaliDatePickerFullScreenDialogViewController(
                initialSelectedDate: initialSelectedDate,
                locale: locale,
                yearRangeStart: yearRange.lowerBound,
                yearRangeEnd: yearRange.upperBound,
                selectableDates: selectableDates,
                calendarOptions: calendarOptions,
                options: options,
                onHeightChange: { _ in },
                onConfirm: onConfirm,
                onDismiss: onDismiss
            )
        }
        return NepaliDateDialogViewControllersKt.NepaliDatePickerDialogViewController(
            initialSelectedDate: initialSelectedDate,
            locale: locale,
            yearRangeStart: yearRange.lowerBound,
            yearRangeEnd: yearRange.upperBound,
            selectableDates: selectableDates,
            calendarOptions: calendarOptions,
            options: options,
            onHeightChange: { _ in },
            onConfirm: onConfirm,
            onDismiss: onDismiss
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

/// The `B.S.` / `A.D.` switch on its own, for driving a picker from the app's own chrome.
struct NepaliCalendarSystemToggleView: UIViewControllerRepresentable {
    var onHeightChange: (CGFloat) -> Void = { _ in }
    var initialCalendarSystem: CalendarSystem = .bikramSambat
    var language: NepaliDatePickerLang = .english
    var onCalendarSystemChange: (CalendarSystem) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        NepaliCalendarSystemToggleViewControllerKt.NepaliCalendarSystemToggleViewController(
            initialCalendarSystem: initialCalendarSystem,
            language: language,
            onHeightChange: { onHeightChange(CGFloat($0)) },
            onCalendarSystemChange: onCalendarSystemChange
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
