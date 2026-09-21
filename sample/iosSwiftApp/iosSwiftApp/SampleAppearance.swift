//
//  SampleAppearance.swift
//  Nepali Date Picker
//
//  Drives `NepaliPickerAppearance`, the shared proxy the library reads its colours from, and the
//  SwiftUI chrome around the hosted pickers, so both change together.
//

import Combine
import SwiftUI
import nepali_date_picker

/// Which brightness the showcase asks for, independently of the device setting.
enum SampleBrightness: String, CaseIterable, Identifiable {
    case system, light, dark

    var id: String { rawValue }

    var label: String {
        switch self {
        case .system: return "System"
        case .light: return "Light"
        case .dark: return "Dark"
        }
    }

    /// What SwiftUI should render the surrounding list and navigation bar at. `nil` follows the
    /// device, which is what `system` means.
    var preferredColorScheme: ColorScheme? {
        switch self {
        case .system: return nil
        case .light: return .light
        case .dark: return .dark
        }
    }

    var bridged: NepaliPickerBrightness {
        switch self {
        case .system: return .system
        case .light: return .light
        case .dark: return .dark
        }
    }
}

/// The showcase's current palette and brightness, and the one place that writes them through to the
/// library.
///
/// The library resolves `system` against the device itself, but the palette's colours are chosen
/// here, so the resolved brightness has to be passed in from the view that observes it.
@MainActor
final class AppearanceModel: ObservableObject {
    @Published var palette: SamplePalette = .standard
    @Published var brightness: SampleBrightness = .system

    /// The accent SwiftUI's own controls take, so the navigation bar matches the calendar below it.
    func tint(dark: Bool) -> Color {
        Color(argbValue: palette.roles(dark: dark).primary)
    }

    /// Pushes the current choice into the shared appearance proxy. Every hosted picker on screen
    /// repaints, so this is safe to call whenever the palette, the mode or the device style changes.
    func apply(systemDark: Bool) {
        let dark = brightness == .system ? systemDark : brightness == .dark
        let roles = palette.roles(dark: dark)
        let appearance = NepaliPickerAppearance.shared

        appearance.brightness = brightness.bridged
        appearance.primaryArgb = argb(roles.primary)
        appearance.onPrimaryArgb = argb(roles.onPrimary)
        appearance.primaryContainerArgb = argb(roles.primaryContainer)
        appearance.onPrimaryContainerArgb = argb(roles.onPrimaryContainer)
        appearance.secondaryContainerArgb = argb(roles.secondaryContainer)
        appearance.onSecondaryContainerArgb = argb(roles.onSecondaryContainer)
        appearance.surfaceArgb = argb(roles.surface)
        appearance.onSurfaceArgb = argb(roles.onSurface)
        appearance.surfaceVariantArgb = argb(roles.surfaceVariant)
        appearance.onSurfaceVariantArgb = argb(roles.onSurfaceVariant)
        appearance.outlineArgb = argb(roles.outline)
    }
}

/// The toolbar control: one menu holding the three brightness modes and every palette, each marked
/// when it is the active one.
struct SampleAppearanceMenu: View {
    @ObservedObject var appearance: AppearanceModel
    let dark: Bool

    var body: some View {
        Menu {
            Picker("Brightness", selection: $appearance.brightness) {
                ForEach(SampleBrightness.allCases) { mode in
                    Text(mode.label).tag(mode)
                }
            }
            .pickerStyle(.inline)

            Picker("Palette", selection: $appearance.palette) {
                ForEach(SamplePalette.allCases) { entry in
                    Label {
                        Text(entry.label)
                    } icon: {
                        // A menu tints a template symbol with the menu's own accent, which would
                        // paint every swatch the same colour. An image that carries its own colour
                        // is left alone.
                        Image(uiImage: .swatch(argbValue: entry.roles(dark: dark).primary))
                    }
                    .tag(entry)
                }
            }
            .pickerStyle(.inline)
        } label: {
            Image(systemName: "paintpalette")
        }
        .accessibilityLabel("Appearance")
    }
}

extension UIImage {
    /// A filled dot in the given colour, drawn rather than tinted so a menu cannot recolour it.
    static func swatch(argbValue: UInt32, diameter: CGFloat = 16) -> UIImage {
        let size = CGSize(width: diameter, height: diameter)
        return UIGraphicsImageRenderer(size: size).image { context in
            UIColor(Color(argbValue: argbValue)).setFill()
            context.cgContext.fillEllipse(in: CGRect(origin: .zero, size: size))
        }
        .withRenderingMode(.alwaysOriginal)
    }
}

extension Color {
    /// A `0xAARRGGBB` literal as a SwiftUI colour, matching how the bridge reads the same value.
    init(argbValue: UInt32) {
        self.init(
            .sRGB,
            red: Double((argbValue >> 16) & 0xFF) / 255,
            green: Double((argbValue >> 8) & 0xFF) / 255,
            blue: Double(argbValue & 0xFF) / 255,
            opacity: Double((argbValue >> 24) & 0xFF) / 255
        )
    }
}
