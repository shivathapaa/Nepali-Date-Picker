#!/usr/bin/env bash
# Renders the Swift Package Manager repository's README from the template beside
# this script.
#
# Every {{TOKEN}} in the template is replaced by the environment variable of the
# same name, so adding a fact to the README needs a new variable in the workflow
# and no change here. A token with no variable behind it fails the release rather
# than publishing literal braces.
#
# Usage: .github/spm/render.sh <output-path>
set -euo pipefail

here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
template="$here/README.template.md"
out="${1:?usage: render.sh <output-path>}"

python3 - "$template" "$out" <<'PY'
import os
import re
import sys

template_path, out_path = sys.argv[1], sys.argv[2]
text = open(template_path, encoding="utf-8").read()

tokens = sorted(set(re.findall(r"{{([A-Z0-9_]+)}}", text)))
missing = [token for token in tokens if not os.environ.get(token)]
if missing:
    sys.exit("::error::The README template needs these values: " + ", ".join(missing))

for token in tokens:
    text = text.replace("{{" + token + "}}", os.environ[token])

with open(out_path, "w", encoding="utf-8") as handle:
    handle.write(text)

print("Rendered %s from %d values: %s" % (out_path, len(tokens), ", ".join(tokens)))
PY
