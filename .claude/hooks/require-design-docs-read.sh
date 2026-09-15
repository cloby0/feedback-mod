#!/usr/bin/env bash
# PreToolUse gate (Edit|Write|MultiEdit): implementation edits under src/main/
# require this session to have Read feedback_philosophy.md and
# feedback_mechanics.md first. CLAUDE.md says those two documents are
# authoritative and required before any implementation; nothing enforced
# that before this hook, so an agent could skip straight to code.
set -euo pipefail

input=$(cat)
file_path=$(jq -r '.tool_input.file_path // empty' <<<"$input")
transcript=$(jq -r '.transcript_path // empty' <<<"$input")

# Only gate hand-written implementation. Docs, datagen, build files, tests
# outside src/main, and edits to the design docs themselves are unaffected.
case "$file_path" in
  */src/main/*) ;;
  *) exit 0 ;;
esac

[[ -n "$transcript" && -f "$transcript" ]] || exit 0

was_read() {
  jq -e --arg pat "$1" '
    select(.type == "assistant")
    | .message.content[]?
    | select(.type == "tool_use" and .name == "Read")
    | select(.input.file_path | test($pat))
  ' "$transcript" >/dev/null 2>&1
}

missing=()
was_read 'feedback_philosophy\.md$' || missing+=("feedback_philosophy.md")
was_read 'feedback_mechanics\.md$' || missing+=("feedback_mechanics.md")

if [[ ${#missing[@]} -eq 0 ]]; then
  exit 0
fi

list=$(printf ', %s' "${missing[@]}")
list=${list#, }
reason="Blocked: this session hasn't Read ${list} yet. CLAUDE.md: these are authoritative and required before any implementation in this repo. Read them (Read tool, not a summary), then retry the edit."

jq -n --arg reason "$reason" '{
  hookSpecificOutput: {
    hookEventName: "PreToolUse",
    permissionDecision: "deny",
    permissionDecisionReason: $reason
  }
}'
