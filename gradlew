#!/bin/sh
APP_HOME="$(cd "$(dirname "$0")" && pwd)"
exec java -jar "${APP_HOME}/gradle/wrapper/gradle-wrapper.jar" \
    -Dgradle.user.home="${APP_HOME}/.gradle" \
    "$@" 2>/dev/null || \
  exec /opt/gradle-8.10.2/bin/gradle "$@"
