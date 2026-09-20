# Caitlin Daily feature integration

The existing UI/components were kept in place; the changes are additive to the existing design.

- Offline account creation: name, optional age, optional profile photo.
- Google/Firebase sign-in/auth dependencies removed.
- Gemini is BYO-key only. No API key is bundled. The local key is encrypted with Android Keystore.
- Gemini model endpoint: `gemini-3.6-flash`.
- Settings: Verify key, Get API key, short setup guide, clear key.
- Gemini-dependent Schedule/AI Verify/Insights tabs are disabled when no verified key is configured.
- AI task verification requires both written evidence and a screenshot.
- High-priority tasks cannot be manually marked complete; only successful AI verification can mark them complete.
- Study Time Beta accepts a screenshot plus an app name and asks Gemini to read the visible duration.
- Insights combines task names/completion state and study sessions into a Gemini-generated report and suggestions.
- Task creation now has Android calendar date/time pickers and an optional direct Google Calendar insertion with runtime permission.
- Settings backup/restore exports account data, settings, tasks, study sessions, timetable and app timestamp. The Gemini API key is deliberately excluded.
- Room database migrated from version 1 to 2 for scheduled task timestamps and study sessions.
- GitHub Actions uses Gradle 9.3.1 + JDK 17, matching AGP 9.1.1 requirements.
