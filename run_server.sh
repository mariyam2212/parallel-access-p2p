#!/bin/bash

# Path to the JAR file
jar_path="target/p2p-pa4-1.0-SNAPSHOT.jar"
c_path="com.aos/IndexServer"

# Add Java JDK bin directory to PATH
export PATH="$PATH:/path/to/java/jdk1.6.0_14/bin"

echo "#################"
echo "Running the Server script..."
echo "#################"
# Execute the Java class using the JAR file and classpath
java -cp "$jar_path":lib/javax.ws.rs-api-2.0.jar "$c_path"
