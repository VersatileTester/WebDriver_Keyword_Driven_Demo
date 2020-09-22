#!/usr/bin/env bash
#2 arguments passed in when running through DevOps
localID=$1
browserstackUrl=$2
currentDateTime="`date +%Y%m%d%H%M%S`";

mvn clean verify -Pbstack \
-Dtest=BrowserStack_Suite \
-Dbrowser=osxSafari \
-Dbrowserstack.url=$browserstackUrl \
-Dlocal.id=$localID \
-Dbuild=ipadSafari$currentDateTime

TEST_BUILD_RESULT=$?

#Use the existing surefire test result output to generate a HTML report - Cannot be achieved within the same build
#(As it will make the Jenkins build always pass)
mvn verify -DskipTests

#If the Tests fail, fail the build - For DevOps pipelines
if [ $TEST_BUILD_RESULT -ne 0 ]; then
  echo "Errors occurred during Test phase, throwing error code 1"
  exit 1
fi