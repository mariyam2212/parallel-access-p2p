#!/bin/bash

# Check if argument is provided
if [ $# -ne 2 ]; then
    echo "Usage: $0 <number_of_directories> <file_size_K_or_M>"
    exit 1
fi
#How many directories you want to create
number_of_directories=$1

#Size of files to be created - M or K
file_size=$2

# Directory where shared directory will be created
SHARED_DIR="SHARED_DIR"

# Create shared directory if it doesn't exist
if [ ! -d "$SHARED_DIR" ]; then
    mkdir "$SHARED_DIR"
fi

for ((n = 1; n <= number_of_directories; n++)); do
  # Create client directory
  client_dir="${SHARED_DIR}/folder${n}"
  mkdir -p "$client_dir"

  # Generate random number of files
  num_files=$((1 + RANDOM % 20))  # Adjust the range as needed
  for ((i = 1; i <= num_files; i++)); do
      # Generate a random filename
      filename="file${n}_$i"
      # Generate random file size in MB (between 1 MB and 100 MB)
      #random_size=$((RANDOM % 100 + 1))
      # Generate random file size in KB (between 1 MB and 100 MB)
          random_size=$((RANDOM % 200 + 64))
      # Create a file with random size
      dd if=/dev/zero of="${client_dir}/${filename}" bs=1${file_size} count=$random_size &>/dev/null
  done
done

echo "Shared directory and client directory created successfully."
