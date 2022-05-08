#!/bin/bash

if [ -z "$JAVA_HOME" ]
then
    export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-17.0.3.0.6-2.el8_5.x86_64/
    echo "Note: \$JAVA_HOME environmental variable has been set to the following directory:"

else
    echo "Note: \$JAVA_HOME environmental variable was already set to the following value, so will not be changed:"
fi
echo "$JAVA_HOME"
echo "If there are problems building the application, you may need to change this variable to the directory which contains a recent JDK version"

sleep 2
echo "Starting build with Maven..."
sleep 1

mvn clean install -X
