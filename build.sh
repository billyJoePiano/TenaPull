#!/bin/bash

echo

if [ -z "$JAVA_HOME" ]
then
    export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-17.0.3.0.6-2.el8_5.x86_64/
    echo "Note: \$JAVA_HOME environmental variable has been set to the following directory:"

else
    echo "Note: \$JAVA_HOME environmental variable was already set to the following value, so will not be changed:"
fi
echo "$JAVA_HOME"
echo "If there are problems building the application, you may need to change this variable to the directory which contains a recent JDK version"

function interruptMsg() {
  echo
  echo "build.sh interrupted... exiting"
  exit 1
}

trap interruptMsg SIGINT SIGKILL SIGTERM SIGHUP SIGSTOP

echo "Press any key to continue..."
read -n 1 -s -r # -n 1 = 1 character.  -s = silent.  -r = raw (don't escape backslashes, etc.)
read -t 0.1     # near-immediate timeout, but consumes excess characters (e.g. for arrow or ctrl keys)

echo -n "Starting build with Maven in 3..."
sleep 1
echo -n "2... "
sleep 1
echo -n "1... "
sleep 1
echo

mvn clean install -X

