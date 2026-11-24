#!/usr/bin/env bash
set -euo pipefail

./gradlew :nexus-desktop:installDist --no-daemon
rm -rf nexus-desktop/build/jpackage

jpackage \
  --type app-image \
  --name NexusAI \
  --input nexus-desktop/build/install/nexus-desktop/lib \
  --main-jar nexus-desktop-0.1.0-SNAPSHOT.jar \
  --main-class ai.nexus.desktop.NexusDesktopApplication \
  --dest nexus-desktop/build/jpackage \
  --app-version 1.0.0 \
  --java-options '--module-path $APPDIR --add-modules javafx.controls,javafx.graphics'

echo "Готово: nexus-desktop/build/jpackage/NexusAI.app"
