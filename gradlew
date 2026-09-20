#!/usr/bin/env sh
set -eu

GRADLE_VERSION="9.3.1"
DIST_NAME="gradle-${GRADLE_VERSION}-bin"
DIST_URL="https://services.gradle.org/distributions/${DIST_NAME}.zip"
CACHE_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper/dists/${DIST_NAME}"
DIST_DIR="${CACHE_DIR}/gradle-${GRADLE_VERSION}"
ZIP_PATH="${CACHE_DIR}/${DIST_NAME}.zip"

if [ ! -x "${DIST_DIR}/bin/gradle" ]; then
  mkdir -p "${CACHE_DIR}"
  if [ ! -f "${ZIP_PATH}" ]; then
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    if command -v curl >/dev/null 2>&1; then
      curl -fL --retry 3 --connect-timeout 20 -o "${ZIP_PATH}.tmp" "${DIST_URL}"
    elif command -v wget >/dev/null 2>&1; then
      wget -O "${ZIP_PATH}.tmp" "${DIST_URL}"
    else
      echo "Neither curl nor wget is available." >&2
      exit 1
    fi
    mv "${ZIP_PATH}.tmp" "${ZIP_PATH}"
  fi
  rm -rf "${DIST_DIR}.tmp"
  mkdir -p "${DIST_DIR}.tmp"
  unzip -q -o "${ZIP_PATH}" -d "${DIST_DIR}.tmp"
  mv "${DIST_DIR}.tmp/gradle-${GRADLE_VERSION}" "${DIST_DIR}"
  rm -rf "${DIST_DIR}.tmp"
fi

exec "${DIST_DIR}/bin/gradle" "$@"
