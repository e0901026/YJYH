#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
export JAVA_HOME="${JAVA_HOME:-/Applications/Android Studio.app/Contents/jbr/Contents/Home}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"

LAN_IP="${LAN_IP:-$(ipconfig getifaddr en0 2>/dev/null || true)}"
if [[ -z "$LAN_IP" ]]; then
  LAN_IP="$(ipconfig getifaddr en1 2>/dev/null || true)"
fi
if [[ -z "$LAN_IP" ]]; then
  echo "Cannot detect LAN IP. Set LAN_IP manually, for example: LAN_IP=192.168.0.110 $0" >&2
  exit 1
fi

export API_BASE_URL="${API_BASE_URL:-http://$LAN_IP:8080}"

cd "$ROOT_DIR/android-app"
./gradlew :app:assembleDebug

mkdir -p "$ROOT_DIR/releases/v0.6"
cp "$ROOT_DIR/android-app/app/build/outputs/apk/debug/app-debug.apk" \
  "$ROOT_DIR/releases/v0.6/YJYH-phone-loan-v0.6.0-lan-debug.apk"

echo "Built LAN APK:"
echo "$ROOT_DIR/releases/v0.6/YJYH-phone-loan-v0.6.0-lan-debug.apk"
echo "API_BASE_URL=$API_BASE_URL"
shasum -a 256 "$ROOT_DIR/releases/v0.6/YJYH-phone-loan-v0.6.0-lan-debug.apk"
