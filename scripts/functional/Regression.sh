#!/usr/bin/env bash

mvn clean verify -Pgrid -Dtest=Regression_Suite

TEST_BUILD_RESULT=$?

#Use the existing surefire test result output to generate a HTML report - Cannot be achieved within the same build
#(As it will make the Jenkins build always pass)
mvn verify -DskipTests

#If the Tests fail, fail the build - For DevOps pipelines
if [ $TEST_BUILD_RESULT -ne 0 ]; then
  echo "Errors occurred during Test phase, throwing error code 1"
  exit 1
fi