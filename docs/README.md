The objective of this assignment is to enhance the existing file transfer mechanism to incorporate parallelism by splitting files into fixed-size chunks. Instead of downloading a file from a single node, peer nodes will concurrently download these chunks from multiple peers, reconstructing the original file. The integrity of each chunk will be verified to ensure the downloaded file matches the original.

### Run the Indexing Server (port: 3788)
./run_server.sh

### Run a peer
./run_peer.sh

### generate sample data (directory -> subdirectory -> files)
#### argument 1 - number of subdirectories required
./generate_shared_dir.sh <number-of-subdirectories>

### detailed steps mentioned in the design document 
### follow the instructions after executing above scripts
