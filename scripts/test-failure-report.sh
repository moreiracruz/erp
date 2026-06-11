#!/bin/bash
# =============================================================================
# Local Test Failure Report Generator
# Usage: ./scripts/test-failure-report.sh
# Run AFTER: ./mvnw verify -Pintegration (or ./mvnw test)
# Output: .test-report.txt (gitignored, paste contents to Kiro for diagnosis)
# =============================================================================

set -euo pipefail

REPORT=".test-report.txt"
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

{
  echo "=== LOCAL TEST FAILURE REPORT ==="
  echo "Date: $(date -Iseconds)"
  echo "Java: $(java -version 2>&1 | head -1)"
  echo "Docker: $(docker version --format '{{.Server.Version}}' 2>/dev/null || echo 'N/A')"
  echo ""

  # Find all surefire/failsafe reports with failures
  echo "--- FAILED TEST CLASSES ---"
  find "$PROJECT_ROOT" -path "*/target/*-reports/*.txt" -exec grep -l "Failures: [1-9]\|Errors: [1-9]" {} \; 2>/dev/null | \
    while read -r file; do
      echo ""
      echo "FILE: ${file#$PROJECT_ROOT/}"
      # Extract: test set name, failures count, and error messages
      grep -E "Tests run:|<<< FAILURE|<<< ERROR" "$file"
    done
  echo ""

  # Extract root causes
  echo "--- ROOT CAUSES ---"
  find "$PROJECT_ROOT" -path "*/target/*-reports/*.txt" -exec grep -l "Failures: [1-9]\|Errors: [1-9]" {} \; 2>/dev/null | \
    while read -r file; do
      grep -A2 "Caused by\|AssertionError\|AssertionFailedError\|expected:\|but was:" "$file" | \
        grep -v "^\s*at " | head -5
      echo "---"
    done
  echo ""

  echo "--- END OF REPORT ---"
} > "$REPORT"

LINES=$(wc -l < "$REPORT")
echo "Report saved to: $REPORT ($LINES lines)"
echo ""
echo "To share with Kiro, paste the contents:"
echo "  cat $REPORT"
