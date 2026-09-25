#!/usr/bin/env sh
set -eu
BUILD_DIR="${TMPDIR:-/tmp}/learning-logistics-test-classes"
mkdir -p "$BUILD_DIR"
javac -d "$BUILD_DIR" $(find src/main/java src/test/java -name '*.java')
java -cp "$BUILD_DIR" dev.learninglogistics.logistics.ShipmentDecisionTest
