// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

/// Nepali (Bikram Sambat) date picker and conversion engine for Flutter,
/// bridging the compiled Kotlin Multiplatform library that also ships to
/// Maven Central, Swift Package Manager and npm.
///
/// The engine lives behind [NepaliDateConverter], [NepaliCalendarPolicy] and
/// the wire formatters; the Material3 pickers are native Compose UI exposed
/// as widgets ([NepaliDatePicker], [NepaliDateRangePicker], the fields, the
/// wheel) and as modal dialogs ([showNepaliDatePickerDialog],
/// [showNepaliDatePickerFullScreenDialog]). [NepaliCalendar] is the browsable
/// month calendar, where the events an app keeps are the point rather than an
/// accent.
library;

export 'src/appearance.dart' show NepaliPickerAppearance, NepaliPickerBrightness;
export 'src/converter.dart' show NepaliDateConverter;
export 'src/model/events.dart' show NepaliDayStatus, NepaliEvent, NepaliEventKind;
export 'src/model/locale.dart'
    show
        CalendarSystem,
        DigitScript,
        NameFormat,
        NepaliDateFormatStyle,
        NepaliDateLocale,
        NepaliLanguage;
export 'src/model/nepali_date.dart'
    show NepaliDate, NepaliDateTime, NepaliMonthInfo;
export 'src/model/picker_options.dart'
    show NepaliPickerEvent, NepaliPickerEventOptions, NepaliSelectableDates;
export 'src/model/simple_types.dart' show SimpleDate, SimpleTime, YearRange;
export 'src/pickers/nepali_calendar.dart' show NepaliCalendar;
export 'src/pickers/nepali_calendar_system_toggle.dart'
    show NepaliCalendarSystemToggle;
export 'src/pickers/nepali_date_field.dart' show NepaliDateField;
export 'src/pickers/nepali_date_picker.dart' show NepaliDatePicker;
export 'src/pickers/nepali_date_picker_dialogs.dart'
    show showNepaliDatePickerDialog, showNepaliDatePickerFullScreenDialog;
export 'src/pickers/nepali_date_picker_docked.dart' show NepaliDatePickerDocked;
export 'src/pickers/nepali_date_range_field.dart' show NepaliDateRangeField;
export 'src/pickers/nepali_date_range_picker.dart' show NepaliDateRangePicker;
export 'src/pickers/nepali_wheel_date_picker.dart' show NepaliWheelDatePicker;
export 'src/policy.dart' show NepaliCalendarPolicy, NepaliEventSpans;
export 'src/wire_format.dart'
    show DatePattern, NepaliDateFormatter, NepaliTimeFormatter;
