# Number Tap — Test Device Specification
# Issue #13: Automated testing on virtual device

## CI Device Configuration

| Parameter       | Value          |
|----------------|----------------|
| Device          | Pixel 7        |
| API Level       | 34             |
| Target          | android 34     |
| ABI             | x86_64         |
| RAM             | 2048 MB        |
| SD Card         | 512 MB         |
| Screen          | 1080x2400, 420dpi |

## Creating the AVD

```bash
# Install system image
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "system-images;android-34;google_apis;x86_64"

# Create AVD
echo "no" | $ANDROID_HOME/cmdline-tools/latest/bin/avdmanager create avd \
  -n "numbertap_test" \
  -k "system-images;android-34;google_apis;x86_64" \
  -d "pixel_7"

# Start headless
$ANDROID_HOME/emulator/emulator \
  -avd numbertap_test \
  -no-window \
  -no-audio \
  -gpu swiftshader_indirect \
  -no-snapshot \
  -no-metrics &
```

## GitHub Actions (reactivecircus/android-emulator-runner)

```yaml
- name: Run instrumented tests
  uses: reactivecircus/android-emulator-runner@v2
  with:
    api-level: 34
    target: google_apis
    arch: x86_64
    profile: pixel_7
    heap-size: 512M
    ram-size: 2048M
    script: ./gradlew connectedDebugAndroidTest --console=plain
```

## Running Tests Locally

```bash
# Full suite (unit + instrumented + coverage)
./scripts/run_tests.sh --full

# Unit tests only
./scripts/run_tests.sh

# Instrumented tests only (requires running emulator)
./scripts/run_tests.sh --device
```
