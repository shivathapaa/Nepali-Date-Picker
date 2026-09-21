// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import Flutter
import UIKit
import nepali_date_picker

/// Presents the native modal pickers over the key window's root controller
/// and drives the iOS appearance proxy. Dialog calls complete their Dart
/// future on the user's confirm or dismiss.
final class PickerHostApiImpl: PickerHostApi {

  func showDatePickerDialog(config: DialogConfigDto) async throws -> CalendarDto? {
    try await presentDialog { finish in
      NepaliDateDialogViewControllersKt.NepaliDatePickerDialogViewController(
        initialSelectedDate: config.initialSelectedDate?.toCore,
        locale: config.locale.toCore,
        yearRangeStart: Int32(config.yearRangeStart),
        yearRangeEnd: Int32(config.yearRangeEnd),
        selectableDates: selectableFromDto(config.selectable),
        calendarOptions: self.calendarOptions(config),
        options: self.dialogOptions(config),
        events: config.eventOptions?.toIos,
        onHeightChange: { _ in },
        onConfirm: { date in finish(date?.toDto) },
        onDismiss: { finish(nil) }
      )
    }
  }

  func showFullScreenDatePickerDialog(config: DialogConfigDto) async throws -> CalendarDto? {
    try await presentDialog { finish in
      NepaliDateDialogViewControllersKt.NepaliDatePickerFullScreenDialogViewController(
        initialSelectedDate: config.initialSelectedDate?.toCore,
        locale: config.locale.toCore,
        yearRangeStart: Int32(config.yearRangeStart),
        yearRangeEnd: Int32(config.yearRangeEnd),
        selectableDates: selectableFromDto(config.selectable),
        calendarOptions: self.calendarOptions(config),
        options: self.dialogOptions(config),
        events: config.eventOptions?.toIos,
        onHeightChange: { _ in },
        onConfirm: { date in finish(date?.toDto) },
        onDismiss: { finish(nil) }
      )
    }
  }

  /// Presents the controller the builder makes over the key window and
  /// resumes exactly once, on the user's confirm or dismiss.
  private func presentDialog(
    _ makeController: @escaping (_ finish: @escaping (CalendarDto?) -> Void) -> UIViewController
  ) async throws -> CalendarDto? {
    try await withCheckedThrowingContinuation { continuation in
      DispatchQueue.main.async {
        guard let root = PickerPlatformView.rootViewController() else {
          continuation.resume(
            throwing: PigeonError(
              code: "NO_ACTIVITY", message: "No key window to present over", details: nil))
          return
        }
        var controller: UIViewController?
        var completed = false
        let finish: (CalendarDto?) -> Void = { picked in
          guard !completed else { return }
          completed = true
          controller?.dismiss(animated: false)
          continuation.resume(returning: picked)
        }
        let dialog = makeController(finish)
        controller = dialog
        dialog.modalPresentationStyle = .overFullScreen
        root.present(dialog, animated: false)
      }
    }
  }

  func applyAppearance(appearance: AppearanceDto) throws {
    let proxy = NepaliPickerAppearance.shared
    switch appearance.brightness {
    case .system: proxy.brightness = .system
    case .light: proxy.brightness = .light
    case .dark: proxy.brightness = .dark
    }
    proxy.primaryArgb = Int32(truncatingIfNeeded: appearance.primaryArgb)
    proxy.onPrimaryArgb = Int32(truncatingIfNeeded: appearance.onPrimaryArgb)
    proxy.primaryContainerArgb = Int32(truncatingIfNeeded: appearance.primaryContainerArgb)
    proxy.onPrimaryContainerArgb = Int32(truncatingIfNeeded: appearance.onPrimaryContainerArgb)
    proxy.secondaryContainerArgb = Int32(truncatingIfNeeded: appearance.secondaryContainerArgb)
    proxy.onSecondaryContainerArgb =
      Int32(truncatingIfNeeded: appearance.onSecondaryContainerArgb)
    proxy.surfaceArgb = Int32(truncatingIfNeeded: appearance.surfaceArgb)
    proxy.onSurfaceArgb = Int32(truncatingIfNeeded: appearance.onSurfaceArgb)
    proxy.surfaceVariantArgb = Int32(truncatingIfNeeded: appearance.surfaceVariantArgb)
    proxy.onSurfaceVariantArgb = Int32(truncatingIfNeeded: appearance.onSurfaceVariantArgb)
    proxy.outlineArgb = Int32(truncatingIfNeeded: appearance.outlineArgb)
  }

  func resetAppearance() throws {
    NepaliPickerAppearance.shared.reset()
  }

  private func calendarOptions(_ config: DialogConfigDto) -> NepaliCalendarOptions {
    let options = NepaliCalendarOptions()
    options.showModeToggle = config.showModeToggle
    options.showTodayButton = config.showTodayButton
    options.showEnglishDate = config.showEnglishDate
    options.englishDateLocale = config.englishDateLocale?.toCore
    options.initialCalendarSystem = calendarSystemOfEra(config.initialCalendarSystemEra)
    options.showCalendarSystemToggle = config.showCalendarSystemToggle
    options.showAdjacentMonthDays = config.showAdjacentMonthDays
    return options
  }

  private func dialogOptions(_ config: DialogConfigDto) -> NepaliDialogOptions {
    let options = NepaliDialogOptions()
    options.title = config.title
    options.confirmText = config.confirmText
    options.dismissText = config.dismissText
    options.tonalElevation = Float(config.tonalElevation)
    options.cornerRadius = Float(config.cornerRadius)
    return options
  }
}
