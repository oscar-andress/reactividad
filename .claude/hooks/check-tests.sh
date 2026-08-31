#!/usr/bin/env bash
# PreToolUse hook: gates `git commit` on the full test suite passing.
# Only invoked for Bash calls matching "git commit*" (see .claude/settings.json's
# "if" filter) - every other Bash call never reaches this script.
set -uo pipefail

cd "$CLAUDE_PROJECT_DIR" || exit 0

JAVA17="/c/Users/oscar.vega/.jdks/jbr-17.0.14"
if [ -x "$JAVA17/bin/java" ]; then
  export JAVA_HOME="$JAVA17"
fi

LOG="$(mktemp)"
if ./mvnw test -q >"$LOG" 2>&1; then
  rm -f "$LOG"
  exit 0
fi

REASON="Blocked: './mvnw test' is failing - CLAUDE.md's Testing policy requires all tests to pass with zero errors before committing.

Last 40 log lines:
$(tail -n 40 "$LOG")"
rm -f "$LOG"

printf '%s' "$REASON" | node -e '
let input = "";
process.stdin.on("data", d => { input += d; });
process.stdin.on("end", () => {
  process.stdout.write(JSON.stringify({
    hookSpecificOutput: {
      hookEventName: "PreToolUse",
      permissionDecision: "deny",
      permissionDecisionReason: input
    }
  }));
});
'
exit 0
