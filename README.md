# Caitlin Daily

Caitlin Daily is an Android daily-planning app built with Kotlin, Jetpack Compose, Material 3, Room, and the Gemini API.

## Build

Requirements:

- JDK 17
- Gradle 9.3.1
- Android SDK 36

The repository includes a GitHub Actions build worker at `.github/workflows/build-apk.yml`. Every push, pull request, or manual workflow run:

1. Sets up JDK 17.
2. Uses Gradle 9.3.1.
3. Builds `assembleDebug`.
4. Runs `lintDebug`.
5. Publishes the debug APK as a workflow artifact.

Locally, if Gradle 9.3.1 is installed:

```bash
./gradlew assembleDebug
./gradlew lintDebug
```

The project intentionally does not depend on `google-services.json`, Firebase configuration, or an embedded Gemini key.

## Gemini

AI features use the Gemini Developer API through the user's own API key. Add the key from the app's Gemini settings screen.

For public testing, **do not put a Gemini API key in source control or in GitHub Actions**. Google recommends treating Gemini API keys like passwords and not hard-coding them into mobile production applications. Use a restricted/authentication key appropriate for your project. [Gemini API key security guidance](https://ai.google.dev/gemini-api/docs/api-key)

The app currently targets the stable `gemini-3.8-flash` model with `gemini-3.7-flash` as a fallback. [Gemini API models](https://ai.google.dev/gemini-api/docs/models)

## Permissions

The app declares and requests permissions for features that use them:

- Notifications on Android 13+
- Calendar read/write access for calendar synchronization
- Exact alarms for timetable reminders
- Vibration/haptic feedback
- Network access for Gemini API requests

Calendar events are inserted into the first visible writable calendar found on the device; no calendar ID is hard-coded.

## Application ID

```text
com.aswinas.caitlin.planner
```

The package/namespace and Android source tree use the same application ID.

## Project cleanup

The public-testing build removes the previous AI Studio/Firebase build configuration, hard-coded calendar ID, sample AI metrics, debug keystore dependency, unused networking/Firebase/camera/location dependencies, and example screenshot/unit-test scaffolding while preserving the existing app screens and feature set.
