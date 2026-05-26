#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
export JAVA_HOME="${JAVA_HOME:-/Applications/Android Studio.app/Contents/jbr/Contents/Home}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
ADB="$ANDROID_HOME/platform-tools/adb"

cd "$ROOT_DIR/android-app"
./gradlew :app:assembleDebug

"$ADB" install -r "$ROOT_DIR/android-app/app/build/outputs/apk/debug/app-debug.apk"
"$ADB" shell am force-stop com.yjyh.phoneloan
"$ADB" shell am start -n com.yjyh.phoneloan/.MainActivity
