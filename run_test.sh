#!/bin/bash

jar_path="target/p2p-pa4-1.0-SNAPSHOT.jar"
c_path="com.aos/MultiClient"
# Add Java bin directory to the PATH
export PATH="$PATH:/path/to/java/bin"  # Replace "/path/to/java/bin" with the actual path to your Java bin directory

echo "Setting the Class path"

echo "For Evaluation of Peer to Peer File transferring system, we are running multiple clients at once..."
echo "Starting to set up all configurations."
echo "#############################"
echo "LOAD TESTING of P2P started"
echo "#############################"

# Run the Java application with required classpath
java -cp "$jar_path":lib/javax.ws.rs-api-2.0.jar "$c_path"

read -p "Press Enter to exit"
