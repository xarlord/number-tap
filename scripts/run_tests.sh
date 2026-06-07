#!/usr/bin/env bash
#
# Number Tap — Automated Testing Script
# Runs unit tests, instrumented tests (on connected device), and generates coverage.
#
# Usage:
#   ./scripts/run_tests.sh              # Unit tests + coverage only
#   ./scripts/run_tests.sh --full       # Unit + instrumented + coverage
#   ./scripts/run_tests.sh --device     # Instrumented tests only
#
# Prerequisites:
#   - JDK 17
#   - Android SDK (ANDROID_HOME set)
#   - Android emulator running (for --full/--device)
#   - ANDROID_HOME environment variable
#
# Issue #13: Formalized automated testing on virtual device.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log()  { echo -e "${GREEN}[TEST]${NC} $*"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $*"; }
err()  { echo -e "${RED}[FAIL]${NC} $*"; }

cd "$PROJECT_DIR"

# Ensure gradlew exists
if [ ! -f "./gradlew" ]; then
    err "gradlew not found. Run from project root."
    exit 1
fi
chmod +x ./gradlew

MODE="${1:---unit}"

# ───────────────────────────────────────────
#  Unit Tests
# ───────────────────────────────────────────
run_unit_tests() {
    log "Running unit tests..."
    ./gradlew testDebugUnitTest --console=plain

    # Count results
    local total=$(find app/build/test-results/testDebugUnitTest -name "*.xml" -exec grep -c '<testcase' {} \; | paste -sd+ | bc 2>/dev/null || echo "0")
    local failed=$(find app/build/test-results/testDebugUnitTest -name "*.xml" -exec grep -c 'failures="[1-9]' {} \; 2>/dev/null | paste -sd+ | bc || echo "0")

    if [ "$failed" = "0" ]; then
        log "Unit tests: ${total} passed ✓"
    else
        err "Unit tests: ${failed} failures!"
        return 1
    fi
}

# ───────────────────────────────────────────
#  Coverage Verification
# ───────────────────────────────────────────
run_coverage() {
    log "Verifying coverage ≥ 90%..."
    if ./gradlew koverVerify --console=plain; then
        log "Coverage gate passed ✓"
    else
        err "Coverage below 90% — check kover report"
        ./gradlew koverHtmlReport --console=plain 2>/dev/null || true
        return 1
    fi
}

# ───────────────────────────────────────────
#  Instrumented Tests (requires device/emulator)
# ───────────────────────────────────────────
run_instrumented_tests() {
    # Check for connected device
    if [ -z "${ANDROID_HOME:-}" ]; then
        warn "ANDROID_HOME not set — skipping instrumented tests"
        return 0
    fi

    local devices=$("$ANDROID_HOME/platform-tools/adb" devices 2>/dev/null | grep -v "List\|^$" | wc -l)
    if [ "$devices" -eq 0 ]; then
        warn "No Android device/emulator connected — skipping instrumented tests"
        warn "Start an emulator with: \$ANDROID_HOME/emulator/emulator -avd <name> -no-window"
        return 0
    fi

    log "Running instrumented tests on connected device..."
    ./gradlew connectedDebugAndroidTest --console=plain

    log "Instrumented tests completed ✓"
}

# ───────────────────────────────────────────
#  Lint
# ───────────────────────────────────────────
run_lint() {
    log "Running Android Lint..."
    ./gradlew lintDebug --console=plain
    log "Lint passed ✓"
}

# ───────────────────────────────────────────
#  Main
# ───────────────────────────────────────────
case "$MODE" in
    --full)
        run_lint
        run_unit_tests
        run_coverage
        run_instrumented_tests
        ;;
    --device)
        run_instrumented_tests
        ;;
    --unit|--default)
        run_unit_tests
        run_coverage
        ;;
    *)
        echo "Usage: $0 [--full|--device|--unit]"
        exit 1
        ;;
esac

log "All requested tests completed successfully! 🎉"
