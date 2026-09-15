#!/usr/bin/env bash
# PostToolUse gate (Read): records that this session has read
# feedback_philosophy.md / feedback_mechanics.md, via a marker file keyed on
# transcript_path rather than by re-parsing the transcript JSONL. The
# transcript-parsing approach (see require-design-docs-read.sh's history)
# was unreliable for background/subagent sessions — the JSONL isn't
# guaranteed flushed to disk by the time the very next tool call's
# PreToolUse hook runs, so a subagent that had genuinely just Read both
# docs was still seeing the block. A marker file we write ourselves,
# synchronously, in the PostToolUse hook for the Read call itself, has no
# such race: it exists by the time the hook process exits.
set -euo pipefail

input=$(cat)
file_path=$(jq -r '.tool_input.file_path // empty' <<<"$input")
transcript=$(jq -r '.transcript_path // empty' <<<"$input")

[[ -n "$transcript" ]] || exit 0

markerdir="${CLAUDE_PROJECT_DIR:-.}/.claude/hooks/.read-markers"
mkdir -p "$markerdir"
key=$(printf '%s' "$transcript" | md5sum | cut -d' ' -f1)

case "$file_path" in
  */feedback_philosophy.md) touch "$markerdir/$key.philosophy" ;;
  */feedback_mechanics.md)  touch "$markerdir/$key.mechanics" ;;
esac

exit 0
