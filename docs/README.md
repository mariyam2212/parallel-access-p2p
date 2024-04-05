# Run the Indexing Server (port: 3788)
./run_server.sh

# Run a peer
./run_peer.sh

# generate sample data (directory -> subdirectory -> files)
# argument 1 - number of subdirectories required
./generate_shared_dir.sh <number-of-subdirectories>

# detailed steps mentioned in the design document 
# follow the instructions after executing above scripts