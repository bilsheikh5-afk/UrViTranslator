#!/usr/bin/env sh

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")

# Add default JVM options here
DEFAULT_JVM_OPTS=""

# Locate Java
if [ -n "$JAVA_HOME" ]; then
    JAVA_EXE="$JAVA_HOME/bin/java"
else
    JAVA_EXE=$(which java)
fi

if [ ! -x "$JAVA_EXE" ]; then
    echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2
    exit 1
fi

# Locate the gradle-wrapper jar
DIR=$(dirname "$0")
GRADLE_WRAPPER_JAR="$DIR/gradle/wrapper/gradle-wrapper.jar"

# Execute Gradle
exec "$JAVA_EXE" $DEFAULT_JVM_OPTS -jar "$GRADLE_WRAPPER_JAR" "$@"
