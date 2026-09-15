#!/usr/bin/env bash
# PreToolUse gate (Edit|Write|MultiEdit): implementation edits under src/main/
# require this session to have Read feedback_philosophy.md and
# feedback_mechanics.md first. CLAUDE.md says those two documents are
# authoritative and required before any implementation; nothing enforced
# that before this hook, so an agent could skip straight to code.
#
# Checks marker files (written by mark-design-doc-read.sh's PostToolUse hook
# on Read) rather than re-parsing the transcript JSONL. The transcript
# approach was unreliable for background/subagent sessions — the JSONL isn't
# guaranteed flushed to disk by the time this hook runs for the very next
# tool call, so a subagent that had genuinely just Read both docs could
# still get blocked. The marker file is written synchronously by the Read
# call's own PostToolUse hook, so it has no such race.
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

[[ -n "$transcript" ]] || exit 0

markerdir="${CLAUDE_PROJECT_DIR:-.}/.claude/hooks/.read-markers"
key=$(printf '%s' "$transcript" | md5sum | cut -d' ' -f1)

missing=()
[[ -f "$markerdir/$key.philosophy" ]] || missing+=("feedback_philosophy.md")
[[ -f "$markerdir/$key.mechanics" ]] || missing+=("feedback_mechanics.md")

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
