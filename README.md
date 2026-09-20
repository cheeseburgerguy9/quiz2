# Caitlin Daily — offline-first Android app

Caitlin Daily is an offline-first Android planner with optional Gemini AI.

## What changed

- Removed Google Sign-In / Google account dependency.
- Account creation is fully offline.
- Setup asks for **name**, optional **age**, and optional **profile photo**.
- Profile photo is stored locally in the app's private storage.
- Gemini is **Bring Your Own Key**:
  - No API key is included in source, Gradle, BuildConfig, or the APK.
  - The user enters their own key in **Settings → Gemini AI**.
  - The key is encrypted with Android Keystore.
  - The manual backup deliberately excludes the Gemini key.
- Settings contains a direct **Get a Gemini API key** button and step-by-step instructions.
- Added offline JSON **Export / Import** backup.
  - Includes profile, profile photo, app date, settings, tasks, verification metadata and timetable.
  - Import replaces the current local planner data.
- Uses Material 3 dynamic color / Material You styling on Android 12+ and follows the selected System/Light/Dark mode.
- Removed the old Google account UI.
- Added a GitHub Actions workflow that builds a debug APK without requiring a signing key.

## Getting a Gemini API key

1. Open Google AI Studio from the button in the app or visit `https://aistudio.google.com/app/apikey`.
2. Sign in to Google AI Studio.
3. Choose **Create API key** and select/create a Google Cloud project if prompted.
4. Copy the generated key.
5. In Caitlin Daily, open **Settings → Gemini AI**, paste the key and tap **Save key**.
6. Tap **Test Gemini connection**.

The key is stored locally using Android Keystore. It is not part of this repository.

## Build locally

Open the project in Android Studio and run the `app` debug configuration.

Or, with Gradle 9.3.1 and JDK 17 installed:

```bash
gradle :app:assembleDebug
```

The APK is produced at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Build with GitHub

The repository includes `.github/workflows/build-apk.yml`.

Push the repository to GitHub, then open **Actions → Build Android APK**. The workflow builds the debug APK and uploads it as an artifact.

No `.env`, Google Services JSON, Gemini key, or custom keystore is required.

## Important backup note

Manual backups are JSON files intended for the user. They contain personal planner data and the profile photo, so treat them as private files. The Gemini API key is never exported.

## Package

Application ID:

```text
com.aistudio.plancraft.vxyzt
```


## UI refresh

The main task dashboard now follows the supplied Android 16/17 Material You-inspired reference: expressive rounded cards, pill filters, a compact profile pill, floating New Task action, rounded navigation surface, and a subtle time-of-day header illustration. Dynamic Material 3 colors remain enabled, so the palette follows the device wallpaper/theme on Android 12+.

The header artifact changes with local device time (morning sun, afternoon cloud, evening glow, night moon) and remains intentionally low-contrast so it does not compete with task content.
