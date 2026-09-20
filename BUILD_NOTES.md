# Build / cleanup notes

- Removed the AI Studio generated Secrets, Google Services, Firebase, Google Sign-In, and KSP build plugins/dependencies.
- Room uses KAPT instead of KSP to avoid the IntelliJ/AWT KSP crash seen in GitHub Actions.
- Debug builds use the standard Android debug signing configuration; no repository `debug.keystore` is required.
- GitHub Actions installs Gradle 9.3.1 directly and runs `assembleDebug`.
- AGP 9.1.1, Gradle 9.3.1, JDK 17 and Kotlin 2.2.10 are intentionally aligned.
- Gemini uses the user-provided `gemini-3.6-flash` API model; no API key is bundled.
- API keys are stored with Android Keystore encryption and excluded from explicit backups.
- Removed generated demo account values, demo timetable entries, greeting sample screen, and AI Studio project metadata.
- High-priority task completion is guarded both in the UI and ViewModel; screenshot + written evidence are required for AI verification.
- Explicit in-app backup includes account/app settings, tasks, study records, timetable, app creation date, and profile photo data.
