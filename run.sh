#!/bin/sh
# Build and run. Usage: sh run.sh web      (website on http://localhost:8080)
#                       sh run.sh          (student desktop app)
#                       sh run.sh admin    (problem/test-case admin console)
#                       sh run.sh test     (all self-tests)
set -e
cd "$(dirname "$0")"
rm -rf bin && mkdir -p bin
javac --release 17 -encoding UTF-8 -d bin -cp "lib/*" $(find src -name '*.java')
java -cp "bin:lib/*" com.bcorps.app.Main "$@"
