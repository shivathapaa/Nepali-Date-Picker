// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import Flutter
import UIKit
import nepali_date_picker

/// The creation-parameter map one embedded picker arrives with, decoded into
/// engine types. The map shape is produced by the Dart side's
/// `buildPickerCreationParams`; absent keys fall back to the same defaults
/// the ViewController factories use.
struct PickerParams {
  let map: [String: Any]

  init(_ args: Any?) {
    map = args as? [String: Any] ?? [:]
  }

  var variant: String { map["variant"] as? String ?? "datePicker" }

  var initialDate: SimpleDate? { date(map["initialDate"]) }
  var initialEndDate: SimpleDate? { date(map["initialEndDate"]) }

  var locale: NepaliDateLocale {
    guard let entry = map["locale"] as? [Any] else { return defaultLocale() }
    return locale(from: entry)
  }

  var englishLocale: NepaliDateLocale? {
    (map["englishLocale"] as? [Any]).map { locale(from: $0) }
  }

  var yearRangeStart: Int32 {
    (map["yearRangeStart"] as? NSNumber)?.int32Value
      ?? NepaliCalendarDefaults.shared.NepaliYearRange.first
  }

  var yearRangeEnd: Int32 {
    (map["yearRangeEnd"] as? NSNumber)?.int32Value
      ?? NepaliCalendarDefaults.shared.NepaliYearRange.last
  }

  var selectableDates: NepaliSelectableDates? {
    guard let entry = map["selectable"] as? [String: Any] else { return nil }
    let policy: PolicyDto? = {
      let weekly = entry["policyWeeklyOffDays"] as? [Any]
      let events = entry["policyEvents"] as? [Any]
      if weekly == nil && events == nil { return nil }
      return PolicyDto(
        weeklyOffDays: (weekly ?? []).compactMap { ($0 as? NSNumber)?.int64Value },
        events: (events ?? []).compactMap { row -> EventDto? in
          guard let fields = row as? [Any], fields.count >= 6 else { return nil }
          return EventDto(
            year: int64(fields[0]),
            month: int64(fields[1]),
            dayOfMonth: int64(fields[2]),
            name: fields[3] as? String ?? "",
            kind: EventKindDto(rawValue: int(fields[4])) ?? .observance,
            closesOffices: fields[5] as? Bool ?? false,
            id: nil,
            payload: nil
          )
        }
      )
    }()
    return selectableFromDto(
      SelectableDto(
        minDate: date(entry["minDate"]).map {
          DateDto(year: Int64($0.year), month: Int64($0.month), dayOfMonth: Int64($0.dayOfMonth))
        },
        maxDate: date(entry["maxDate"]).map {
          DateDto(year: Int64($0.year), month: Int64($0.month), dayOfMonth: Int64($0.dayOfMonth))
        },
        includeMinDate: entry["includeMinDate"] as? Bool ?? false,
        includeMaxDate: entry["includeMaxDate"] as? Bool ?? false,
        excludeWeekend: (entry["excludeWeekend"] as? [Any])?
          .compactMap { ($0 as? NSNumber)?.int64Value },
        excludeClosuresOf: policy
      ))
  }

  var events: NepaliEventOptions? {
    guard let entry = map["events"] as? [String: Any] else { return nil }
    let options = NepaliEventOptions()
    if let weekly = entry["weeklyOffDays"] as? [Any] {
      options.weeklyOffDays = weekly.compactMap { ($0 as? NSNumber).map { KotlinInt(int: $0.int32Value) } }
    }
    if let rows = entry["events"] as? [Any] {
      options.events = rows.compactMap { row -> NepaliEventInfo? in
        guard let fields = row as? [Any], fields.count >= 8 else { return nil }
        let info = NepaliEventInfo(
          year: Int32(int(fields[0])),
          month: Int32(int(fields[1])),
          dayOfMonth: Int32(int(fields[2])),
          name: fields[3] as? String ?? "",
          kind: eventKind(at: int(fields[4])),
          closesOffices: fields[5] as? Bool ?? false,
          colorArgb: Int32(truncatingIfNeeded: int64(fields[6])),
          indicate: fields[7] as? Bool ?? false
        )
        // The app's own handles, so a tapped entry comes back with its record.
        info.id = fields.count > 8 ? fields[8] as? String : nil
        info.payload = fields.count > 9 ? fields[9] as? String : nil
        return info
      }
    }
    options.markWeeklyOff = entry["markWeeklyOff"] as? Bool ?? true
    options.markEvents = entry["markEvents"] as? Bool ?? true
    options.tintContainer = entry["tintContainer"] as? Bool ?? false
    options.indicateWeeklyOff = entry["indicateWeeklyOff"] as? Bool ?? false
    options.describeEvents = entry["describeEvents"] as? Bool ?? true
    options.weeklyOffColorArgb = colorInt(entry["weeklyOffColorArgb"])
    options.publicHolidayColorArgb = colorInt(entry["publicHolidayColorArgb"])
    options.religiousColorArgb = colorInt(entry["religiousColorArgb"])
    options.regionalColorArgb = colorInt(entry["regionalColorArgb"])
    options.observanceColorArgb = colorInt(entry["observanceColorArgb"])
    options.markedContainerColorArgb = colorInt(entry["markedContainerColorArgb"])
    return options
  }

  var calendarSystem: CalendarSystem { calendarSystemOfEra(int64(map["initialCalendarSystemEra"] ?? 2)) }

  var language: NepaliDatePickerLang {
    int(map["language"] ?? 0) == 1 ? .nepali : .english
  }

  var dateFormat: NepaliDateFormatter.Pattern {
    (DatePatternDto(rawValue: int(map["dateFormat"] ?? 0)) ?? .yyyySlashMmSlashDd).toCore
  }

  var dateFormatStyle: NepaliDateFormatStyle {
    (DateFormatStyleDto(rawValue: int(map["dateFormatStyle"] ?? 2)) ?? .medium).toCore
  }

  func bool(_ key: String, _ fallback: Bool) -> Bool { map[key] as? Bool ?? fallback }

  func float(_ key: String, _ fallback: Float) -> Float {
    (map[key] as? NSNumber)?.floatValue ?? fallback
  }

  func int32(_ key: String, _ fallback: Int32) -> Int32 {
    (map[key] as? NSNumber)?.int32Value ?? fallback
  }

  func string(_ key: String) -> String? { map[key] as? String }

  private func date(_ value: Any?) -> SimpleDate? {
    guard let entry = value as? [Any], entry.count >= 3 else { return nil }
    return SimpleDate(
      year: Int32(int(entry[0])), month: Int32(int(entry[1])), dayOfMonth: Int32(int(entry[2])))
  }

  private func locale(from entry: [Any]) -> NepaliDateLocale {
    let digit = int(entry[4])
    return NepaliDateLocale(
      language: int(entry[0]) == 1 ? .nepali : .english,
      dateFormat: (DateFormatStyleDto(rawValue: int(entry[1])) ?? .long).toCore,
      weekDayName: (NameFormatDto(rawValue: int(entry[2])) ?? .full).toCore,
      monthName: (NameFormatDto(rawValue: int(entry[3])) ?? .full).toCore,
      digitScript: digit >= 0 ? (DigitScriptDto(rawValue: digit) ?? .latin).toCore : nil
    )
  }

  private func defaultLocale() -> NepaliDateLocale {
    NepaliDateLocale(
      language: .english, dateFormat: .long_, weekDayName: .full, monthName: .full,
      digitScript: nil)
  }

  private func colorInt(_ value: Any?) -> Int32 {
    Int32(truncatingIfNeeded: (value as? NSNumber)?.int64Value ?? 0)
  }
}

private func int(_ value: Any) -> Int {
  (value as? NSNumber)?.intValue ?? 0
}

private func eventKind(at index: Int) -> NepaliEventKind {
  let entries = NepaliEventKind.entries
  return entries.indices.contains(index) ? entries[index] : .observance
}

private func int64(_ value: Any) -> Int64 {
  (value as? NSNumber)?.int64Value ?? 0
}

/// Creates one embedded picker per platform view, keyed by view id for the
/// callback channel.
final class PickerPlatformViewFactory: NSObject, FlutterPlatformViewFactory {
  private let flutterApi: () -> PickerViewFlutterApi?

  init(flutterApi: @escaping () -> PickerViewFlutterApi?) {
    self.flutterApi = flutterApi
    super.init()
  }

  func createArgsCodec() -> FlutterMessageCodec & NSObjectProtocol {
    FlutterStandardMessageCodec.sharedInstance()
  }

  func create(withFrame frame: CGRect, viewIdentifier viewId: Int64, arguments args: Any?)
    -> FlutterPlatformView
  {
    PickerPlatformView(
      frame: frame, viewId: viewId, params: PickerParams(args), api: flutterApi())
  }
}

/// Hosts one of the library's UIViewController factories inside a Flutter
/// platform view and forwards its callbacks, tagged with the view's id.
final class PickerPlatformView: NSObject, FlutterPlatformView {
  private let controller: UIViewController

  init(frame: CGRect, viewId: Int64, params: PickerParams, api: PickerViewFlutterApi?) {
    // Kotlin function types box their primitives, so the height arrives as
    // a KotlinFloat. Failures are swallowed: a view may already be gone
    // when a late event lands, and there is nothing to do about a delivery
    // failure.
    let emit: (@escaping (PickerViewFlutterApi) async throws -> Void) -> Void = { block in
      guard let api else { return }
      Task { @MainActor in try? await block(api) }
    }
    let onHeight: (KotlinFloat) -> Void = { height in
      emit { try await $0.onHeightChanged(viewId: viewId, height: height.doubleValue) }
    }

    switch params.variant {
    case "docked":
      controller = NepaliDatePickerViewControllersKt.NepaliDatePickerDockedViewController(
        initialSelectedDate: params.initialDate,
        locale: params.locale,
        yearRangeStart: params.yearRangeStart,
        yearRangeEnd: params.yearRangeEnd,
        selectableDates: params.selectableDates,
        options: {
          let options = NepaliDockedOptions()
          options.dateFormatStyle = params.dateFormatStyle
          options.showTodayButton = params.bool("showTodayButton", true)
          options.label = params.string("label")
          options.placeholder = params.string("placeholder")
          options.initialCalendarSystem = params.calendarSystem
          options.showCalendarSystemToggle = params.bool("showCalendarSystemToggle", false)
          options.showAdjacentMonthDays = params.bool("showAdjacentMonthDays", false)
          return options
        }(),
        events: params.events,
        onHeightChange: onHeight,
        onDateSelected: { date in
          emit { try await $0.onDateSelected(viewId: viewId, date: date?.toDto) }
        }
      )

    case "wheel":
      controller = NepaliDatePickerViewControllersKt.NepaliWheelDatePickerViewController(
        initialDate: params.initialDate,
        locale: params.locale,
        yearRangeStart: params.yearRangeStart,
        yearRangeEnd: params.yearRangeEnd,
        selectableDates: params.selectableDates,
        options: {
          let options = NepaliWheelOptions()
          options.itemHeight = params.float("itemHeight", 44)
          options.visibleItemCount = params.int32("visibleItemCount", 5)
          options.initialCalendarSystem = params.calendarSystem
          options.showCalendarSystemToggle = params.bool("showCalendarSystemToggle", false)
          return options
        }(),
        onHeightChange: onHeight,
        onDateChange: { date in
          emit { try await $0.onDateSelected(viewId: viewId, date: date.toDto) }
        }
      )

    case "rangePicker":
      controller = NepaliDateRangeViewControllersKt.NepaliDateRangePickerViewController(
        initialSelectedStartDate: params.initialDate,
        initialSelectedEndDate: params.initialEndDate,
        locale: params.locale,
        yearRangeStart: params.yearRangeStart,
        yearRangeEnd: params.yearRangeEnd,
        selectableDates: params.selectableDates,
        options: {
          let options = NepaliRangeCalendarOptions()
          options.showModeToggle = params.bool("showModeToggle", true)
          options.showTodayButton = params.bool("showTodayButton", true)
          options.showMonthsVertically = params.bool("showMonthsVertically", true)
          options.showYearPickerAndMonthNavigation =
            params.bool("showYearPickerAndMonthNavigation", true)
          options.showEnglishDate = params.bool("showEnglishDate", false)
          options.englishDateLocale = params.englishLocale
          options.initialCalendarSystem = params.calendarSystem
          options.showCalendarSystemToggle = params.bool("showCalendarSystemToggle", false)
          options.showAdjacentMonthDays = params.bool("showAdjacentMonthDays", false)
          return options
        }(),
        events: params.events,
        onHeightChange: onHeight,
        onRangeSelected: { start, end in
          emit { try await $0.onRangeSelected(viewId: viewId, start: start?.toDto, end: end?.toDto) }
        }
      )

    case "dateField":
      controller = NepaliDateFieldViewControllersKt.NepaliDateFieldViewController(
        initialValue: params.initialDate,
        locale: params.locale,
        dateFormat: params.dateFormat,
        yearRangeStart: params.yearRangeStart,
        yearRangeEnd: params.yearRangeEnd,
        selectableDates: params.selectableDates,
        options: {
          let options = NepaliFieldOptions()
          options.outlined = params.bool("outlined", true)
          options.label = params.string("label")
          options.placeholder = params.string("placeholder")
          options.supportingText = params.string("supportingText")
          options.isError = params.bool("isError", false)
          options.enabled = params.bool("enabled", true)
          options.readOnly = params.bool("readOnly", false)
          options.confirmButtonText = params.string("confirmButtonText")
          options.dismissButtonText = params.string("dismissButtonText")
          options.initialCalendarSystem = params.calendarSystem
          options.showCalendarSystemToggle = params.bool("showCalendarSystemToggle", false)
          options.showAdjacentMonthDays = params.bool("showAdjacentMonthDays", false)
          return options
        }(),
        events: params.events,
        onHeightChange: onHeight,
        onValueChange: { value in
          emit { try await $0.onValueChanged(viewId: viewId, value: value?.toDateDto) }
        }
      )

    case "rangeField":
      controller = NepaliDateRangeViewControllersKt.NepaliDateRangeFieldViewController(
        initialStartValue: params.initialDate,
        initialEndValue: params.initialEndDate,
        locale: params.locale,
        dateFormat: params.dateFormat,
        yearRangeStart: params.yearRangeStart,
        yearRangeEnd: params.yearRangeEnd,
        selectableDates: params.selectableDates,
        options: {
          let options = NepaliRangeFieldOptions()
          options.outlined = params.bool("outlined", true)
          options.startLabel = params.string("startLabel")
          options.endLabel = params.string("endLabel")
          options.supportingText = params.string("supportingText")
          options.isStartError = params.bool("isStartError", false)
          options.isEndError = params.bool("isEndError", false)
          options.enabled = params.bool("enabled", true)
          options.readOnly = params.bool("readOnly", false)
          options.confirmButtonText = params.string("confirmButtonText")
          options.dismissButtonText = params.string("dismissButtonText")
          options.initialCalendarSystem = params.calendarSystem
          options.showCalendarSystemToggle = params.bool("showCalendarSystemToggle", false)
          options.showAdjacentMonthDays = params.bool("showAdjacentMonthDays", false)
          return options
        }(),
        events: params.events,
        onHeightChange: onHeight,
        onRangeChange: { start, end in
          emit { try await $0.onRangeValueChanged(viewId: viewId, start: start?.toDateDto, end: end?.toDateDto) }
        }
      )

    case "calendar":
      controller = NepaliCalendarViewControllersKt.NepaliCalendarViewController(
        initialSelectedDate: params.initialDate,
        locale: params.locale,
        yearRangeStart: params.yearRangeStart,
        yearRangeEnd: params.yearRangeEnd,
        options: {
          let options = NepaliCalendarViewOptions()
          options.showTodayButton = params.bool("showTodayButton", true)
          options.showCalendarSystemToggle = params.bool("showCalendarSystemToggle", false)
          options.showAdjacentMonthDays = params.bool("showAdjacentMonthDays", true)
          options.showSecondaryDates = params.bool("showSecondaryDates", true)
          options.secondaryDateLocale = params.englishLocale
          options.initialCalendarSystem = params.calendarSystem
          options.showDaySummary = params.bool("showDaySummary", false)
          options.showMonthEvents = params.bool("showMonthEvents", false)
          return options
        }(),
        events: params.events,
        onHeightChange: onHeight,
        onDaySelected: { date, _ in
          emit { try await $0.onDateSelected(viewId: viewId, date: date.toDto) }
        },
        onEventTapped: { event in
          emit { try await $0.onEventTapped(viewId: viewId, event: event.toDto) }
        }
      )

    case "calendarSystemToggle":
      controller =
        NepaliCalendarSystemToggleViewControllerKt.NepaliCalendarSystemToggleViewController(
          initialCalendarSystem: params.calendarSystem,
          language: params.language,
          onHeightChange: onHeight,
          onCalendarSystemChange: { system in
            emit { try await $0.onCalendarSystemChanged(viewId: viewId, era: Int64(system.era)) }
          }
        )

    default:
      controller = NepaliDatePickerViewControllersKt.NepaliDatePickerViewController(
        initialSelectedDate: params.initialDate,
        locale: params.locale,
        yearRangeStart: params.yearRangeStart,
        yearRangeEnd: params.yearRangeEnd,
        selectableDates: params.selectableDates,
        options: {
          let options = NepaliCalendarOptions()
          options.showModeToggle = params.bool("showModeToggle", true)
          options.showTodayButton = params.bool("showTodayButton", true)
          options.showEnglishDate = params.bool("showEnglishDate", false)
          options.englishDateLocale = params.englishLocale
          options.initialCalendarSystem = params.calendarSystem
          options.showCalendarSystemToggle = params.bool("showCalendarSystemToggle", false)
          options.showAdjacentMonthDays = params.bool("showAdjacentMonthDays", false)
          return options
        }(),
        events: params.events,
        onHeightChange: onHeight,
        onDateSelected: { date in
          emit { try await $0.onDateSelected(viewId: viewId, date: date?.toDto) }
        }
      )
    }

    controller.view.frame = frame
    super.init()

    // Hosting under the root controller forwards appearance transitions,
    // which Compose needs to start and stop rendering.
    if let root = PickerPlatformView.rootViewController() {
      root.addChild(controller)
      controller.didMove(toParent: root)
    }
  }

  func view() -> UIView { controller.view }

  deinit {
    controller.willMove(toParent: nil)
    controller.view.removeFromSuperview()
    controller.removeFromParent()
  }

  static func rootViewController() -> UIViewController? {
    UIApplication.shared.connectedScenes
      .compactMap { $0 as? UIWindowScene }
      .flatMap { $0.windows }
      .first { $0.isKeyWindow }?
      .rootViewController
  }
}
