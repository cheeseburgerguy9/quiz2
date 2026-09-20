@echo off
setlocal
set "GRADLE_VERSION=9.3.1"
set "DIST_NAME=gradle-%GRADLE_VERSION%-bin"
set "DIST_URL=https://services.gradle.org/distributions/%DIST_NAME%.zip"
set "CACHE_DIR=%USERPROFILE%\.gradle\wrapper\dists\%DIST_NAME%"
set "DIST_DIR=%CACHE_DIR%\gradle-%GRADLE_VERSION%"
if not exist "%DIST_DIR%\bin\gradle.bat" (
  if not exist "%CACHE_DIR%" mkdir "%CACHE_DIR%"
  if not exist "%CACHE_DIR%\%DIST_NAME%.zip" powershell -NoProfile -Command "Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%CACHE_DIR%\%DIST_NAME%.zip'"
  powershell -NoProfile -Command "Expand-Archive -Force '%CACHE_DIR%\%DIST_NAME%.zip' '%CACHE_DIR%\tmp'"
  move /Y "%CACHE_DIR%\tmp\gradle-%GRADLE_VERSION%" "%DIST_DIR%" >nul
)
call "%DIST_DIR%\bin\gradle.bat" %*
