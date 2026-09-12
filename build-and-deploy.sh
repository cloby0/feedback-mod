#!/usr/bin/env bash
# Build the mod and copy the jar into the Feedback PrismLauncher instance.
# NeoForge 1.21.1 wants JDK 21, which is the machine default -- no JAVA_HOME override needed.
#
# No packwiz step: Feedback is a standalone mod, not a modpack coremod.
set -euo pipefail

cd "$(dirname "$0")"

instance_dest="/home/colby/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances/feedback/minecraft/mods"

if [ ! -d "$instance_dest" ]; then
    echo "Instance mods folder not found: $instance_dest" >&2
    echo "Create the 'feedback' PrismLauncher instance first (NeoForge 1.21.1)." >&2
    exit 1
fi

./gradlew build --no-daemon

mod_id=$(grep '^mod_id=' gradle.properties | cut -d= -f2)
mod_version=$(grep '^mod_version=' gradle.properties | cut -d= -f2)
jar="build/libs/${mod_id}-${mod_version}.jar"

if [ ! -f "$jar" ]; then
    echo "Expected jar not found: $jar" >&2
    echo "Contents of build/libs:" >&2
    ls -1 build/libs >&2 || true
    exit 1
fi

# A version bump leaves the previous jar behind, and two copies of the same mod id
# is a hard crash on launch. Clear our own jars only -- never anything else in mods/.
shopt -s nullglob
for stale in "$instance_dest/${mod_id}-"*.jar; do
    echo "Removing stale $(basename "$stale")"
    rm -f "$stale"
done
shopt -u nullglob

cp "$jar" "$instance_dest/"
echo "Deployed $(basename "$jar") -> $instance_dest"
