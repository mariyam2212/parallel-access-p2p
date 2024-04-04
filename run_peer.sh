#!/bin/bash

jar_path="target/p2p-pa4-1.0-SNAPSHOT.jar"
c_path="com.aos/Peer"
# Add Java bin directory to the PATH
export PATH="$PATH:/usr/lib/jvm/java-1.8.0-openjdk-amd64/bin"

echo "#################"
echo "Running the Peer script..."
echo "#################"
java -cp "$jar_path":lib/javax.ws.rs-api-2.0.jar "$c_path"
