#!/usr/bin/env bash
# Builds the release APK and records its size. Output: build/benchmark-results/size.json
set -euo pipefail
cd "$(dirname "$0")/.."

./gradlew assembleRelease -PappVersionName=benchmark -PappVersionCode=1 --console=plain -q

apk=$(ls app/build/outputs/apk/release/*.apk | head -n 1)
size_of() { stat -c%s "$1" 2>/dev/null || stat -f%z "$1"; }
apk_bytes=$(size_of "$apk")

# compressed bytes inside the APK, grouped by kind
sum_of() { unzip -lv "$apk" | awk -v pat="$1" '$NF ~ pat { s += $3 } END { print s + 0 }'; }
dex_bytes=$(sum_of '\.dex$')
res_bytes=$(sum_of '^res/|resources\.arsc$')
font_bytes=$(sum_of '^res/font/')
lib_bytes=$(sum_of '^lib/')

mkdir -p build/benchmark-results
cat > build/benchmark-results/size.json <<JSON
{
  "apk_bytes": $apk_bytes,
  "dex_bytes": $dex_bytes,
  "resources_bytes": $res_bytes,
  "fonts_bytes": $font_bytes,
  "native_libs_bytes": $lib_bytes
}
JSON

echo "APK: $(basename "$apk")"
awk -v a="$apk_bytes" -v d="$dex_bytes" -v r="$res_bytes" -v f="$font_bytes" -v l="$lib_bytes" 'BEGIN {
  printf "  apk        %8.2f KB\n  dex        %8.2f KB\n  resources  %8.2f KB\n  fonts      %8.2f KB\n  native     %8.2f KB\n", a/1024, d/1024, r/1024, f/1024, l/1024 }'
echo "Largest entries (compressed bytes):"
unzip -lv "$apk" | awk 'NR>3 {print $3, $8}' | sort -n -r | head -10
