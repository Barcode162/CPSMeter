# CPS Meter

Measures your real clicks-per-second on the device — actual hardware touch
events, not a simulation.

## Features
- **Live CPS** — taps within the last 1 second, updated ~20×/sec.
- **Peak CPS** — highest live value during the run.
- **Total taps**, **time**, and **average CPS**.
- **Test modes:** Free (untimed), 5s, 10s. A timed test starts on your first
  tap and freezes at the end with your average.
- Multi-touch aware — every finger-down counts (`ACTION_DOWN` + `ACTION_POINTER_DOWN`).

## How it works
The tap zone reads raw `MotionEvent`s and timestamps each finger-down with
`SystemClock.elapsedRealtime()` (monotonic, hardware-backed). Live CPS is the
count of timestamps inside a sliding 1000 ms window.

## Build
```
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; $env:ANDROID_HOME='C:\Users\Barcode\AppData\Local\Android\Sdk'
cd G:\CPSMeter; .\gradlew.bat assembleDebug
```
APK: `app/build/outputs/apk/debug/app-debug.apk`

## Project layout
```
app/src/main/java/com/joshua/cpsmeter/MainActivity.kt   tap counting + stats
```
No special permissions required.
