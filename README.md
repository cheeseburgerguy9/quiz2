# Caitlin Daily

Caitlin Daily is an offline-first Android task, timetable, AI verification, study-time and insights app. The current source is configured to build a debug APK on GitHub Actions without shipping a Gemini API key or requiring Google Sign-In.

## Features

- Offline account creation with name, age and optional profile photo.
- Tasks grouped by Work, Study, Health and Personal with High/Medium/Low priority.
- High-priority tasks require Gemini verification before they can be completed.
- AI verification accepts a screenshot plus written evidence.
- Optional Study Time Beta: submit a screenshot and an app name for Gemini to extract study time.
- Gemini 3.6 Flash for verification, study-time extraction, insights and timetable organization.
- User-provided Gemini API key only; no API key is bundled with the app.
- API key is stored locally using Android Keystore encryption and is never included in the in-app backup.
- AI-dependent navigation and actions remain disabled until the key is successfully verified.
- Task date/time pickers and optional device-calendar insertion with runtime permission.
- Offline JSON export/restore from Settings, including account, app settings, tasks, study records and timetable data.
- Material You dynamic colors and the existing Caitlin Daily visual design.

## Gemini API key

The app does not contain a developer Gemini API key. On first use, open **Settings → Gemini AI**, choose **Get API key**, create a key in Google AI Studio, paste it into the app, then select **Verify Key**.

The key is encrypted locally with Android Keystore. Exported backups intentionally omit the key.

## Build on GitHub Actions

1. Push the repository to GitHub.
2. Open **Actions**.
3. Run **Build Android APK** (or push to the default branch).
4. Download the `caitlin-daily-debug-apk` artifact from the workflow run.

The workflow installs the required Gradle version itself, so a developer Gradle installation or a repository-shipped `debug.keystore` is not required.

## Local Android Studio

Open the project directory in Android Studio and allow Gradle to sync. No `.env` file, `google-services.json`, Firebase account, or Google Sign-In configuration is required.

## Privacy

Normal task/account data is kept locally. Gemini requests are made only after the user supplies and verifies their own API key; screenshots and text submitted to AI features are sent to Google's Gemini API for processing.
