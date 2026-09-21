// swift-tools-version: 5.9

import PackageDescription

// The Kotlin framework arrives as a binary target. During development it is
// staged locally by tool/stage_ios_framework.sh; the release pipeline
// replaces the local target with the published download URL and checksum
// from the Nepali-Date-Picker-SPM repository before publishing to pub.dev.
let package = Package(
    name: "nepali_date_picker_kmp",
    platforms: [
        .iOS("15.0")
    ],
    products: [
        .library(name: "nepali-date-picker-kmp", targets: ["nepali_date_picker_kmp"])
    ],
    dependencies: [
        .package(name: "FlutterFramework", path: "../FlutterFramework")
    ],
    targets: [
        .target(
            name: "nepali_date_picker_kmp",
            dependencies: [
                .product(name: "FlutterFramework", package: "FlutterFramework"),
                .target(name: "nepali_date_picker")
            ]
        ),
        .binaryTarget(
            name: "nepali_date_picker",
            path: "Frameworks/nepali_date_picker.xcframework"
        )
    ]
)
