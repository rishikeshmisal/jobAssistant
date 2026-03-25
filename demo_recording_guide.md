# Demo Recording Guide — Job Assistant

This document covers every command needed to build, seed, and record the Maestro demo.

---

## Prerequisites

- Android device connected via USB (or emulator running)
- Maestro CLI installed: `brew install maestro` (macOS)
- ADB in PATH: verify with `adb devices`

---

## Step 1 — Build and install the debug APK

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The `-r` flag reinstalls over an existing build without wiping data.

---

## Step 2 — Push the CSV file to the device

Required for Scene 07 (CSV Import). Must be done before running any flow.

```bash
adb push sample_jobs.csv /sdcard/Download/sample_jobs.csv
```

Verify it landed:

```bash
adb shell ls /sdcard/Download/sample_jobs.csv
```

---

## Step 3 — Seed test data (run once, not recorded)

This flow clears app state, completes onboarding, and seeds 32 synthetic jobs
plus a user profile and career insights via the in-app debug button.

```bash
maestro test flows/00_setup.yaml
```

This flow takes ~30 seconds. Wait for it to complete fully before proceeding.
After it finishes the app will be on the Dashboard with all data loaded.

---

## Step 4 — Record the full demo

```bash
maestro record --local flows/full_demo.yaml demo_recording.mp4
```

- Estimated runtime: ~4 minutes
- Output video: `./demo_recording.mp4` in the current directory

> **Note:** `maestro test --video` does not exist in Maestro 2.x.
> Use `maestro record --local <flow> <output.mp4>` for local video recording.

---

## Running individual scenes

Each scene can be tested independently without recording:

```bash
maestro test flows/01_onboarding.yaml
maestro test flows/02_add_job.yaml
maestro test flows/03_quick_evaluate.yaml
maestro test flows/04_job_detail.yaml
maestro test flows/05_dashboard.yaml
maestro test flows/06_insights.yaml
maestro test flows/07_csv_import.yaml
maestro test flows/08_theme.yaml
```

Run all flows in sequence (no video):

```bash
maestro test flows/
```

---

## Uploading to Maestro Cloud (shareable link)

```bash
maestro cloud --apiKey <your_api_key> flows/full_demo.yaml
```

Returns a public URL: `https://cloud.mobile.dev/run/xxxxx`

---

## Re-seeding between runs

If you need to re-record or re-run after the demo has mutated data:

```bash
maestro test flows/00_setup.yaml
```

This always clears state and re-seeds from scratch before the next run.

---

## Troubleshooting

| Problem | Fix |
|---|---|
| `adb devices` shows no device | Enable USB debugging on the device; accept the RSA prompt |
| Flow fails on "Seed Test Data" | Rebuild and reinstall — the debug button only exists in the debug APK |
| CSV scene fails at file picker | Re-run the `adb push` command from Step 2 |
| Maestro can't find an element | Increase the `sleepFor` before that step in the relevant flow file |
| API calls time out mid-flow | The `sleepFor: 6000` values assume a fast connection — increase if needed |
