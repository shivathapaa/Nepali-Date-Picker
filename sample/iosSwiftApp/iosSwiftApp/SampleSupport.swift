//
//  SampleSupport.swift
//  Nepali Date Picker
//
//  Small helpers shared by the showcase screens.
//

import SwiftUI
import nepali_date_picker

extension SimpleDate {
    var text: String { "\(year)/\(month)/\(dayOfMonth)" }
}

extension CustomCalendar {
    var text: String { "\(year)/\(month)/\(dayOfMonth)" }

    /// The library's typed dates are separate structs, so drop the calendar detail when a plain
    /// year/month/day triple is all an API needs.
    var simple: SimpleDate { SimpleDate(year: year, month: month, dayOfMonth: dayOfMonth) }
}

/// A titled block with an explanation, matching the layout of the Compose sample.
///
/// The text is inset but the content is not: a calendar grid needs every available point, and
/// nesting two levels of padding squeezes the day columns until the Gregorian sub-labels clip.
struct DemoSection<Content: View>: View {
    let title: String
    let subtitle: String
    @ViewBuilder var content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            VStack(alignment: .leading, spacing: 4) {
                Text(title).font(.headline)
                Text(subtitle).font(.caption).foregroundStyle(.secondary)
            }
            .padding(.horizontal, 12)
            content
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.vertical, 14)
        .background(Color(.secondarySystemBackground), in: RoundedRectangle(cornerRadius: 12))
    }
}

/// Sizes a hosted picker to the height Compose reports, instead of a guessed constant.
///
/// A `UIViewControllerRepresentable` has no intrinsic height, so a fixed frame either crops the
/// calendar or leaves dead space under a text field. The builder receives the reporting closure to
/// hand to the representable.
/// Sizes a hosted picker to the height Compose reports.
///
/// The hosted scene is always given `measurementHeight`, while the surrounding layout takes the
/// height Compose reports back. Keeping those two apart is the whole point: Compose measures
/// inside the frame it is given, so if the scene shrank with the layout, a report could never
/// exceed the current frame and the content would be trapped at its smallest size. Switching a
/// picker to typed input and back is exactly that case.
///
/// Measuring the content unbounded would be tidier, but Compose forbids infinite height
/// constraints above a vertically scrolling component and the range picker has one, so a generous
/// finite height is the workable version.
struct AutoSized<Content: View>: View {
    /// Height the scene is measured in. Generous enough for the tallest mode the picker can show.
    var measurementHeight: CGFloat = 420
    @ViewBuilder var content: (@escaping (CGFloat) -> Void) -> Content

    @State private var measured: CGFloat?

    var body: some View {
        content { reported in
            // Compose reports on every layout pass; ignore the noise.
            if measured == nil || abs(measured! - reported) > 0.5 { measured = reported }
        }
        .frame(height: measurementHeight, alignment: .top)
        .frame(height: measured ?? measurementHeight, alignment: .top)
        .clipped()
        // Clip the scene out of hit testing too, so the part hanging below the visible height
        // cannot swallow taps meant for whatever follows it.
        .contentShape(Rectangle())
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .padding(.horizontal, 4)
    }
}

/// One label and its computed value.
struct LabeledValue: View {
    let label: String
    let value: String

    var body: some View {
        HStack(alignment: .top) {
            Text(label).font(.subheadline)
            Spacer(minLength: 12)
            Text(value)
                .font(.subheadline.monospaced())
                .multilineTextAlignment(.trailing)
                .foregroundStyle(.primary)
        }
        .padding(.horizontal, 12)
    }
}

/// Shows the current selection under a picker.
struct SelectionSummary: View {
    let selection: CustomCalendar?

    var body: some View {
        LabeledValue(label: "Selected", value: selection.map(\.text) ?? "none")
    }
}

/// A selectable-date policy written in Swift to prove the Kotlin interface is implementable from
/// the consuming app, not just from the library's own helpers.
final class EvenDaysOnly: NepaliSelectableDates {
    func isSelectableDate(customCalendar: CustomCalendar) -> Bool {
        customCalendar.dayOfMonth % 2 == 0
    }

    func isSelectableYear(year: Int32) -> Bool { true }
}

/// A holiday provider written in Swift, used by the working-day arithmetic demo.
final class SampleHolidayProvider: NepaliHolidayProvider {
    private let holidays: Set<String>

    init(holidays: [SimpleDate]) {
        self.holidays = Set(holidays.map(\.text))
    }

    func holidays(year: Int32) -> Set<HolidayEntry> { [] }

    func isHoliday(date: SimpleDate) -> Bool { holidays.contains(date.text) }
}
