The objective is to enhance the existing file transfer mechanism to incorporate parallelism by splitting files into fixed-size chunks. Instead of downloading a file from a single node, peer nodes will concurrently download these chunks from multiple peers, reconstructing the original file. The integrity of each chunk will be verified to ensure that the downloaded file matches the original.

### Run the Indexing Server (port: 3788)
./run_server.sh

### Run a peer
./run_peer.sh

### generate sample data (directory -> subdirectory -> files)
./generate_shared_dir.sh _number-of-subdirectories_

#### Detailed steps mentioned in the [design document](https://github.com/mariyam2212/parallel-access-p2p/blob/master/docs/Design_Document.pdf)
#### Follow the instructions after executing above scripts
