#!/bin/sh
# Build and run. Usage: ./run.sh          (student app)
#                       ./run.sh admin    (problem/test-case admin console)
#                       ./run.sh test     (all self-tests)
set -e
cd "$(dirname "$0")"
rm -rf bin && mkdir -p bin
javac --release 17 -encoding UTF-8 -d bin -cp lib/h2-2.5.252.jar $(find src -name '*.java')
java -cp "bin:lib/h2-2.5.252.jar" com.bcorps.app.Main "$@"
