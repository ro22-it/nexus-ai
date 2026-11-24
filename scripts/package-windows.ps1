$ErrorActionPreference = "Stop"

./gradlew.bat :nexus-desktop:installDist --no-daemon
Remove-Item -Recurse -Force "nexus-desktop/build/jpackage" -ErrorAction SilentlyContinue

jpackage `
  --type app-image `
  --name NexusAI `
  --input nexus-desktop/build/install/nexus-desktop/lib `
  --main-jar nexus-desktop-0.1.0-SNAPSHOT.jar `
  --main-class ai.nexus.desktop.NexusDesktopApplication `
  --dest nexus-desktop/build/jpackage `
  --app-version 1.0.0 `
  --java-options '--module-path $APPDIR --add-modules javafx.controls,javafx.graphics'

Write-Host "Готово: nexus-desktop/build/jpackage/NexusAI.exe"
