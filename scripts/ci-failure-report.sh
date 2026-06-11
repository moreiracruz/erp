#!/bin/bash
# =============================================================================
# CI Failure Report Generator
# Usage: ./scripts/ci-failure-report.sh [RUN_ID]
#   If RUN_ID is omitted, uses the latest failed run.
# Output: .ci-report.txt (gitignored, paste contents to Kiro for diagnosis)
# =============================================================================

set -euo pipefail

REPORT=".ci-report.txt"

# Get run ID
if [ -n "${1:-}" ]; then
  RUN_ID="$1"
else
  RUN_ID=$(gh run list --status failure --limit 1 --json databaseId -q '.[0].databaseId')
  if [ -z "$RUN_ID" ]; then
    echo "No failed runs found."
    exit 0
  fi
fi

echo "Generating report for run: $RUN_ID"

{
  echo "=== CI FAILURE REPORT ==="
  echo "Run ID: $RUN_ID"
  echo "Date: $(date -Iseconds)"
  echo ""

  # Run metadata
  echo "--- RUN INFO ---"
  gh run view "$RUN_ID" --json name,status,conclusion,event,headBranch,createdAt \
    --jq '"Workflow: \(.name)\nStatus: \(.conclusion)\nEvent: \(.event)\nBranch: \(.headBranch)\nCreated: \(.createdAt)"'
  echo ""

  # Failed jobs
  echo "--- FAILED JOBS ---"
  gh run view "$RUN_ID" --json jobs \
    --jq '.jobs[] | select(.conclusion == "failure") | "❌ \(.name) (\(.conclusion))"'
  echo ""

  # Error logs (last 80 lines of each failed job)
  echo "--- ERROR LOGS (summarized) ---"
  gh run view "$RUN_ID" --log-failed 2>/dev/null | \
    grep -E "ERROR|FAILURE|Exception|Caused by|AssertionError|expected:|but was:" | \
    grep -v "Downloading\|Downloaded\|INFO" | \
    head -60
  echo ""

  echo "--- END OF REPORT ---"
} > "$REPORT"

echo "Report saved to: $REPORT ($(wc -l < "$REPORT") lines)"
echo ""
echo "To share with Kiro, paste the contents:"
echo "  cat $REPORT"
