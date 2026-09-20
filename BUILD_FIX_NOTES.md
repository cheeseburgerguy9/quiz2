# Build fixes

This revision fixes the Kotlin compilation errors reported by GitHub Actions:

- `SettingsScreen.kt`: opted `SettingsScreen` into `ExperimentalMaterial3Api` for `TopAppBarDefaults.topAppBarColors`.
- `TasksScreen.kt`: restored the missing `MetricMiniCard` composable.
- `TasksScreen.kt`: restored the missing `EmptyTasksView` composable.

No API keys are added to the project.

GitHub Actions can build with:

    gradle :app:assembleDebug --stacktrace
