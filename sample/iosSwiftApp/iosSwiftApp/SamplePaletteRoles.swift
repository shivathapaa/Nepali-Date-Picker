//
//  SamplePaletteRoles.swift
//  Nepali Date Picker
//
//  The colour roles behind each sample palette, in the same values the Compose sample uses, so the
//  two showcases render the same calendar.
//

import Foundation

/// The Material roles `NepaliPickerAppearance` takes, as `0xAARRGGBB` literals.
struct PaletteRoles {
    let primary: UInt32
    let onPrimary: UInt32
    let primaryContainer: UInt32
    let onPrimaryContainer: UInt32
    let secondaryContainer: UInt32
    let onSecondaryContainer: UInt32
    let surface: UInt32
    let onSurface: UInt32
    let surfaceVariant: UInt32
    let onSurfaceVariant: UInt32
    let outline: UInt32
}

/// A palette the showcase can switch to, named by the hue it is built around.
enum SamplePalette: String, CaseIterable, Identifiable {
    case standard, green, blue, orange, red, yellow

    var id: String { rawValue }

    var label: String {
        switch self {
        case .standard: return "Default"
        case .green: return "Green"
        case .blue: return "Blue"
        case .orange: return "Orange"
        case .red: return "Red"
        case .yellow: return "Yellow"
        }
    }

    func roles(dark: Bool) -> PaletteRoles {
        switch self {
        case .standard: return dark ? Self.standardDark : Self.standardLight
        case .green: return dark ? Self.greenDark : Self.greenLight
        case .blue: return dark ? Self.blueDark : Self.blueLight
        case .orange: return dark ? Self.orangeDark : Self.orangeLight
        case .red: return dark ? Self.redDark : Self.redLight
        case .yellow: return dark ? Self.yellowDark : Self.yellowLight
        }
    }

    private static let standardLight = PaletteRoles(
        primary: 0xFF6750A4, onPrimary: 0xFFFFFFFF,
        primaryContainer: 0xFFEADDFF, onPrimaryContainer: 0xFF21005D,
        secondaryContainer: 0xFFE8DEF8, onSecondaryContainer: 0xFF1D192B,
        surface: 0xFFFEF7FF, onSurface: 0xFF1D1B20,
        surfaceVariant: 0xFFE7E0EC, onSurfaceVariant: 0xFF49454F,
        outline: 0xFF79747E
    )

    private static let standardDark = PaletteRoles(
        primary: 0xFFD0BCFF, onPrimary: 0xFF381E72,
        primaryContainer: 0xFF4F378B, onPrimaryContainer: 0xFFEADDFF,
        secondaryContainer: 0xFF4A4458, onSecondaryContainer: 0xFFE8DEF8,
        surface: 0xFF141218, onSurface: 0xFFE6E0E9,
        surfaceVariant: 0xFF49454F, onSurfaceVariant: 0xFFCAC4D0,
        outline: 0xFF938F99
    )

    private static let greenLight = PaletteRoles(
        primary: 0xFF4C662B, onPrimary: 0xFFFFFFFF,
        primaryContainer: 0xFFCDEDA3, onPrimaryContainer: 0xFF354E16,
        secondaryContainer: 0xFFDCE7C8, onSecondaryContainer: 0xFF404A33,
        surface: 0xFFF9FAEF, onSurface: 0xFF1A1C16,
        surfaceVariant: 0xFFE1E4D5, onSurfaceVariant: 0xFF44483D,
        outline: 0xFF75796C
    )

    private static let greenDark = PaletteRoles(
        primary: 0xFFB1D18A, onPrimary: 0xFF1F3701,
        primaryContainer: 0xFF354E16, onPrimaryContainer: 0xFFCDEDA3,
        secondaryContainer: 0xFF404A33, onSecondaryContainer: 0xFFDCE7C8,
        surface: 0xFF12140E, onSurface: 0xFFE2E3D8,
        surfaceVariant: 0xFF44483D, onSurfaceVariant: 0xFFC5C8BA,
        outline: 0xFF8F9285
    )

    private static let blueLight = PaletteRoles(
        primary: 0xFF415F91, onPrimary: 0xFFFFFFFF,
        primaryContainer: 0xFFD6E3FF, onPrimaryContainer: 0xFF284777,
        secondaryContainer: 0xFFDAE2F9, onSecondaryContainer: 0xFF3E4759,
        surface: 0xFFF9F9FF, onSurface: 0xFF191C20,
        surfaceVariant: 0xFFE0E2EC, onSurfaceVariant: 0xFF44474E,
        outline: 0xFF74777F
    )

    private static let blueDark = PaletteRoles(
        primary: 0xFFAAC7FF, onPrimary: 0xFF0A305F,
        primaryContainer: 0xFF284777, onPrimaryContainer: 0xFFD6E3FF,
        secondaryContainer: 0xFF3E4759, onSecondaryContainer: 0xFFDAE2F9,
        surface: 0xFF111318, onSurface: 0xFFE2E2E9,
        surfaceVariant: 0xFF44474E, onSurfaceVariant: 0xFFC4C6D0,
        outline: 0xFF8E9099
    )

    private static let orangeLight = PaletteRoles(
        primary: 0xFF8B5000, onPrimary: 0xFFFFFFFF,
        primaryContainer: 0xFFFFDCC2, onPrimaryContainer: 0xFF6A3B00,
        secondaryContainer: 0xFFFFDCC2, onSecondaryContainer: 0xFF5A422D,
        surface: 0xFFFFF8F5, onSurface: 0xFF221A14,
        surfaceVariant: 0xFFF3DFD1, onSurfaceVariant: 0xFF51453A,
        outline: 0xFF837468
    )

    private static let orangeDark = PaletteRoles(
        primary: 0xFFFFB870, onPrimary: 0xFF4A2800,
        primaryContainer: 0xFF6A3B00, onPrimaryContainer: 0xFFFFDCC2,
        secondaryContainer: 0xFF5A422D, onSecondaryContainer: 0xFFFFDCC2,
        surface: 0xFF1A120C, onSurface: 0xFFF0DFD4,
        surfaceVariant: 0xFF51453A, onSurfaceVariant: 0xFFD6C3B5,
        outline: 0xFF9E8E81
    )

    private static let redLight = PaletteRoles(
        primary: 0xFF904A43, onPrimary: 0xFFFFFFFF,
        primaryContainer: 0xFFFFDAD6, onPrimaryContainer: 0xFF73342D,
        secondaryContainer: 0xFFFFDAD6, onSecondaryContainer: 0xFF5D3F3B,
        surface: 0xFFFFF8F7, onSurface: 0xFF231919,
        surfaceVariant: 0xFFF5DDDA, onSurfaceVariant: 0xFF534341,
        outline: 0xFF857371
    )

    private static let redDark = PaletteRoles(
        primary: 0xFFFFB4AB, onPrimary: 0xFF561E19,
        primaryContainer: 0xFF73342D, onPrimaryContainer: 0xFFFFDAD6,
        secondaryContainer: 0xFF5D3F3B, onSecondaryContainer: 0xFFFFDAD6,
        surface: 0xFF1A1110, onSurface: 0xFFF1DEDC,
        surfaceVariant: 0xFF534341, onSurfaceVariant: 0xFFD8C2BE,
        outline: 0xFFA08C8A
    )

    private static let yellowLight = PaletteRoles(
        primary: 0xFF6D5E0F, onPrimary: 0xFFFFFFFF,
        primaryContainer: 0xFFF8E287, onPrimaryContainer: 0xFF534600,
        secondaryContainer: 0xFFEEE2BC, onSecondaryContainer: 0xFF4E472A,
        surface: 0xFFFFF9EE, onSurface: 0xFF1E1B13,
        surfaceVariant: 0xFFEAE2D0, onSurfaceVariant: 0xFF4B4739,
        outline: 0xFF7C7767
    )

    private static let yellowDark = PaletteRoles(
        primary: 0xFFDBC66E, onPrimary: 0xFF3A3000,
        primaryContainer: 0xFF534600, onPrimaryContainer: 0xFFF8E287,
        secondaryContainer: 0xFF4E472A, onSecondaryContainer: 0xFFEEE2BC,
        surface: 0xFF15130B, onSurface: 0xFFE9E2D0,
        surfaceVariant: 0xFF4B4739, onSurfaceVariant: 0xFFCDC6B4,
        outline: 0xFF969080
    )
}
