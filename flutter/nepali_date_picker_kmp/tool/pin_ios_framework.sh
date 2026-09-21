#!/usr/bin/env bash
# Replaces the plugin's local iOS binary target with the released download
# URL and checksum for the given version, read from the Nepali-Date-Picker-SPM
# repository's Package.swift at that tag. The release pipeline runs this
# right before `dart pub publish`, so the pub package needs no vendored
# framework.
#
# Usage: tool/pin_ios_framework.sh 3.3.0

set -euo pipefail

VERSION="${1:?usage: pin_ios_framework.sh <version>}"
PLUGIN_DIR="$(cd "$(dirname "$0")/.." && pwd)"
PACKAGE_SWIFT="$PLUGIN_DIR/ios/nepali_date_picker_kmp/Package.swift"
SPM_MANIFEST_URL="https://raw.githubusercontent.com/shivathapaa/Nepali-Date-Picker-SPM/$VERSION/Package.swift"

# One release event publishes every channel at once, and the SPM repository's
# tag is written by its own workflow, so wait for it rather than racing it.
MANIFEST=""
for attempt in $(seq 1 30); do
  if MANIFEST="$(curl --fail --silent "$SPM_MANIFEST_URL")"; then
    break
  fi
  echo "SPM manifest for $VERSION not published yet (attempt $attempt/30); retrying in 60s" >&2
  sleep 60
done

if [[ -z "$MANIFEST" ]]; then
  echo "Gave up waiting for $SPM_MANIFEST_URL; release the SPM repository first" >&2
  exit 1
fi

URL="$(printf '%s' "$MANIFEST" | grep -A2 '"nepali-date-picker",$' | grep -o 'https://[^"]*nepali_date_picker\.xcframework\.zip')"
CHECKSUM="$(printf '%s' "$MANIFEST" | grep -A3 '"nepali-date-picker",$' | grep -o 'checksum: "[a-f0-9]*"' | grep -o '[a-f0-9]\{64\}')"

if [[ -z "$URL" || -z "$CHECKSUM" ]]; then
  echo "Could not read the ui binary target for $VERSION from $SPM_MANIFEST_URL" >&2
  exit 1
fi

URL="$URL" CHECKSUM="$CHECKSUM" python3 - "$PACKAGE_SWIFT" <<'EOF'
import os
import re
import sys

path = sys.argv[1]
source = open(path).read()
replacement = (
    '.binaryTarget(\n'
    '            name: "nepali_date_picker",\n'
    f'            url: "{os.environ["URL"]}",\n'
    f'            checksum: "{os.environ["CHECKSUM"]}"\n'
    '        )'
)
pinned, count = re.subn(
    r'\.binaryTarget\(\s*name: "nepali_date_picker",\s*path: "[^"]*"\s*\)',
    replacement,
    source,
)
if count != 1:
    raise SystemExit(f"expected one local binary target in {path}, found {count}")
open(path, "w").write(pinned)
print(f"Pinned nepali_date_picker to {os.environ['URL']}")
EOF
