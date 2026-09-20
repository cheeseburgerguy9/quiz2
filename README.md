# Caitlin Daily

Offline-first Android daily planner with optional Gemini-powered verification, study-time extraction, timetable organization, and insights.

## GitHub Actions build

The repository is structured for the project's `.github/workflows/build-apk.yml` workflow:

- Android SDK platform 35
- Android build-tools 35.0.0
- JDK 17
- Gradle 9.3.1 launched through the checked-in `gradlew` launcher
- `testDebugUnitTest`
- `assembleDebug`
- APK artifact: `app/build/outputs/apk/debug/*.apk`

No API key or signing secret is required to build the debug APK. Gemini API keys are entered by the user inside the app and are never bundled into the source.

## Local build

```bash
chmod +x ./gradlew
./gradlew testDebugUnitTest --no-daemon --stacktrace
./gradlew assembleDebug --no-daemon --stacktrace
```
