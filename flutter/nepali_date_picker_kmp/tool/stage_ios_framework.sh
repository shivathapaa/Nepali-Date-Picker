#!/usr/bin/env bash
# Builds the nepali_date_picker XCFramework from the KMP source tree and
# stages it where the plugin's Package.swift expects its local binary target.
# Run once before building the example for iOS, and after any :ui change.
#
# The staged framework is gitignored; the release pipeline swaps the local
# binary target for the released download URL before publishing to pub.dev.

set -euo pipefail

PLUGIN_DIR="$(cd "$(dirname "$0")/.." && pwd)"
REPO_ROOT="$(cd "$PLUGIN_DIR/../.." && pwd)"
SOURCE="$REPO_ROOT/nepali-date-picker/ui/build/XCFrameworks/release/nepali_date_picker.xcframework"
DEST_DIR="$PLUGIN_DIR/ios/nepali_date_picker_kmp/Frameworks"

"$REPO_ROOT/gradlew" -p "$REPO_ROOT" :nepali-date-picker:ui:assembleNepali-date-pickerReleaseXCFramework

rm -rf "$DEST_DIR/nepali_date_picker.xcframework"
mkdir -p "$DEST_DIR"
cp -R "$SOURCE" "$DEST_DIR/"

echo "Staged $(du -sh "$DEST_DIR/nepali_date_picker.xcframework" | cut -f1) at $DEST_DIR"
