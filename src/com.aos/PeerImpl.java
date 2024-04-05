package com.aos;

import java.io.*;
import java.nio.file.Files;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

public class PeerImpl implements PeerDownloadInterface {
    String portNo = null; // Port no. of the peer
    String dirName = null; //Directory where the files are to be stored.
    String fileName = null; //the file to be searched.
    String remotePeer = null; //Peer from whom file has to be downloaded.
    Collection<ArrayList<String>> colArr;
    private static final Logger logger = Logger.getLogger(PeerImpl.class.getName());
    private static final int CHUNK_SIZE = 64 * 1024; // Chunk size in bytes

    PeerImpl(String portNo, String dirName) {
        this.portNo = portNo;
        this.dirName = dirName;

        // Remove console handler
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }

        FileHandler fileHandler = null;
        try {
            fileHandler = new FileHandler("logfile.log");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SimpleFormatter formatter = new SimpleFormatter();
        fileHandler.setFormatter(formatter);
        logger.addHandler(fileHandler);
    }

    // this method is used for Registering new entry, Searching , Deleting a file and Downloading a file.
    public void doWork() throws IOException {
        String peerID = null; //peerID
        try {
            // Locating Registry of Indexing Server and obtains target address
            Registry regis = LocateRegistry.getRegistry("localhost", 3788);
            IndexServerInterface isInter = (IndexServerInterface) regis.lookup("Indexing");
            Scanner sc = new Scanner(System.in);
            System.out.println("Give a Peer ID to this Peer");
            peerID = sc.nextLine();

            //obtain directory name where file is located
            File dirList = new File(dirName);
            //list of all records in the directory
            String[] record = dirList.list();

            // Registering Files in Index Server
            for (int c = 0; c < record.length; c++) {
                File currentFile = new File(dirList, record[c]);
                System.out.println("Registering file " + currentFile.getName() + " in Indexing Server" + "of size = " + currentFile.length());
                isInter.registryFiles("new", currentFile.getName(), peerID, portNo, dirName, currentFile.length());
            }
            System.out.println("Please make a choice");
            System.out.println("1. Query File\n2. Un-register File\n3. Exit");
            String sd = sc.nextLine();
            while (!sd.equalsIgnoreCase("3")) {
                if (sd.equalsIgnoreCase("2")) {
                    //Deleting a File from local peer's Directory
                    String wantToDel = "";
                    while (!wantToDel.equalsIgnoreCase("No")) {
                        System.out.println("Enter the file name which you want to delete");
                        String fname = sc.nextLine();
                        if (fname != null) {
                            File fileToDel = new File(dirName + File.separator + fname);
                            // Delete the specified file from local peer's directory
                            if (fileToDel.delete()) {
                                System.out.println("File deleted Successfully.");
                                // Updating the index server about the deleted file
                                isInter.registryFiles("del", fname, peerID, portNo, dirName, fileToDel.length());
                            } else {
                                System.out.println("Failed to delete the File");
                            }
                        } else {
                            System.out.println("Please Enter a Filename");
                        }
                        System.out.println("Do you want to delete more files? (Yes/No)");
                        wantToDel = sc.nextLine();
                    }
                } else if (sd.equalsIgnoreCase("1")) {
                    //Searching and downloading a File Code
                    String ans = "";
                    while (!ans.equalsIgnoreCase("No")) {
                        // Searching the file in Indexing server
                        System.out.println("Enter name of the file you want to look for at indexing server:");
                        fileName = sc.nextLine();
                        if (fileName != null) {
                            colArr = isInter.searchFile(fileName);
                        } else {
                            System.out.println("Please Enter a Filename");
                        }

                        if (!colArr.isEmpty()) {
                            // Displaying Peers List which can provide the requested file
                            System.out.println("List of peer IDs holding the requested file: ");
                            for (ArrayList<String> als : colArr) {
                                System.out.println("Peer-ID: " + als.get(1));
                            }

                            // Choosing one of the returned Peer
                            System.out.println("Choose the peer for file download");
                            remotePeer = sc.nextLine();

                            // Downloading the file from Specified Peer
                            int co = colArr.size();
                            if (remotePeer != null) {
                                for (ArrayList<String> als : colArr) {
                                    if (als.get(1).equalsIgnoreCase(remotePeer)) {
                                        // Looking up from the Registry for Selected Peer
                                        Registry regis2 = LocateRegistry.getRegistry("localhost", Integer.parseInt(als.get(2)));
                                        PeerDownloadInterface pdInter = (PeerDownloadInterface) regis2.lookup("root://PeerTest/" + als.get(2) + "/FS");
                                        // Calling Remote File Download method of Selected Peer
                                        byte[] output = pdInter.fileDownload(als);
                                        System.out.println("Size of file requested: " + output.length / 1024 + "KB / " + output.length / 1024 / 1024 + "MB");
                                        logger.log(Level.INFO, "Size of file requested: " + output.length / 1024 + "KB / " + output.length / 1024 / 1024 + "MB");
                                        // Converting Downloaded byte array into file
                                        if (output.length != 0) {
                                            FileOutputStream ostream = null;
                                            try {
                                                ostream = new FileOutputStream(dirName + File.separator + fileName);
                                                ostream.write(output);
                                                System.out.println("File Downloaded Successfully.");
                                                //Updating the IndexServer Indexes after downloading the file.
                                                isInter.registryFiles("new", fileName, peerID, portNo, dirName, output.length);
                                            } catch (Exception e) {
                                                logger.log(Level.SEVERE, "Exception in bytearray to file conversion. " + e.getMessage());
                                            } finally {
                                                ostream.close();
                                            }
                                        } else {
                                            System.out.println("File not found at the peer location");
                                        }
                                        break;
                                    } else {
                                        if (co == 1)
                                            System.out.println("Peer with that ID " + remotePeer + " does not exist. Please choose correct PeerId.");
                                    }
                                    co--;
                                }
                            } else {
                                System.out.println("Please enter the correct Peer ID");
                            }
                        } else {
                            System.out.println("Queried file not found with the Indexing Server");
                        }
                        System.out.println("Query again? Yes/No");
                        ans = sc.nextLine();
                    }
                } else {
                    System.out.println("Please make a choice");
                }
                System.out.println("1. Query File\n2. Un-register File\n3. Exit");
                sd = sc.nextLine();
            }
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Exception at Client Interface: " + e.getMessage());
            logger.log(Level.SEVERE, "Exception at Client Interface: " + e);
        }
    }

    public void doParallelWork() throws IOException {
        String peerID = null; //peerID
        try {
            // Locating Registry of Indexing Server and obtains target address
            Registry regis = LocateRegistry.getRegistry("localhost", 3788);
            IndexServerInterface isInter = (IndexServerInterface) regis.lookup("Indexing");
            Scanner sc = new Scanner(System.in);
            System.out.println("Give a Peer ID to this Peer");
            peerID = sc.nextLine();

            //obtain directory name where file is located
            File dirList = new File(dirName);
            //list of all records in the directory
            String[] record = dirList.list();

            // Registering Files in Index Server
            for (int c = 0; c < record.length; c++) {
                File currentFile = new File(dirList, record[c]);
                System.out.println("Registering file " + currentFile.getName() + " in Indexing Server" + "of size = " + currentFile.length());
                isInter.registryFiles("new", currentFile.getName(), peerID, portNo, dirName, currentFile.length());
            }
            System.out.println("Please make a choice");
            System.out.println("1. Query File\n2. Un-register File\n3. Exit");
            String sd = sc.nextLine();
            while (!sd.equalsIgnoreCase("3")) {
                if (sd.equalsIgnoreCase("2")) {
                    //Deleting a File from local peer's Directory
                    String wantToDel = "";
                    while (!wantToDel.equalsIgnoreCase("No")) {
                        System.out.println("Enter the file name which you want to delete");
                        String fname = sc.nextLine();
                        if (fname != null) {
                            File fileToDel = new File(dirName + File.separator + fname);
                            // Delete the specified file from local peer's directory
                            if (fileToDel.delete()) {
                                System.out.println("File deleted Successfully.");
                                // Updating the index server about the deleted file
                                isInter.registryFiles("del", fname, peerID, portNo, dirName, fileToDel.length());
                            } else {
                                System.out.println("Failed to delete the File");
                            }
                        } else {
                            System.out.println("Please Enter a Filename");
                        }
                        System.out.println("Do you want to delete more files? (Yes/No)");
                        wantToDel = sc.nextLine();
                    }
                } else if (sd.equalsIgnoreCase("1")) {
                    //Searching and downloading a File Code
                    String ans = "";
                    while (!ans.equalsIgnoreCase("No")) {
                        // Searching the file in Indexing server
                        System.out.println("Enter name of the file you want to look for at indexing server:");
                        fileName = sc.nextLine();
                        if (fileName != null) {
                            colArr = isInter.searchFile(fileName);
                        } else {
                            System.out.println("Please Enter a Filename");
                        }

                        if (!colArr.isEmpty()) {
                            // Displaying Peers List which can provide the requested file
                            System.out.println("List of peer IDs holding the requested file: ");
                            for (ArrayList<String> als : colArr) {
                                System.out.println("Peer-ID: " + als.get(1));
                            }

//                            // No Choosing one Peer for parallel download
//                            System.out.println("Choose the peer for file download");
//                            remotePeer = sc.nextLine();

                            // Parallel Logic: Downloading the file from all Peers holding the file
                            List<ArrayList<String>> peerList = new ArrayList<>(colArr);
                            fileDownloadInParallelWrapper(peerList, fileName, isInter);
                            break;
                        } else {
                            System.out.println("Queried file not found with the Indexing Server");
                        }
                        System.out.println("Query again? Yes/No");
                        ans = sc.nextLine();
                    }
                } else {
                    System.out.println("Please make a choice");
                }
                System.out.println("1. Query File\n2. Un-register File\n3. Exit");
                sd = sc.nextLine();
            }
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Exception at Client Interface: " + e.getMessage());
            logger.log(Level.SEVERE, "Exception at Client Interface: " + e.getMessage());
        }
    }

    // remote File download method is defined
    public byte[] fileDownload(ArrayList<String> searchedDir) throws RemoteException {
        //0 filename, 1 peerid, 2 port_num, 3 direct
        String fname = searchedDir.get(0);
        String remoteDir = searchedDir.get(3);
        try {
            File file = new File(remoteDir + File.separator + fname);
            logger.log(Level.INFO, "fileDownload: file " + fname + " exists at " + remoteDir + " = " + file.exists());
            if (file.exists()) {
                byte buffer[] = Files.readAllBytes(file.toPath());
                return buffer;
            }
        } catch (Exception e) {
            System.out.println("Error in File download part " + e.getMessage());
            logger.log(Level.SEVERE, "Error in File download part " + e.getMessage());
            logger.log(Level.SEVERE, "stack-trace", e);
            e.printStackTrace();
            return new byte[0];
        }
        return new byte[0];
    }


    public void fileDownloadInParallelWrapper(List<ArrayList<String>> peerList, String fileName, IndexServerInterface isInter) {
        logger.log(Level.INFO, "peer list size=" + peerList.size());
        // Fetch one of the peers(say first)
        ArrayList<String> als = peerList.stream().findFirst().orElse(new ArrayList<>());
        logger.log(Level.INFO, "first peer info" + als.get(0) + " " + als.get(1) + " " + als.get(2) + " " + als.get(3) + " " + als.get(4));
        // als.get(4) gives filesize
        long fileSize = Long.valueOf(als.get(4));
        logger.log(Level.INFO, "size of file in first peer in bytes : " + fileSize);
        System.out.println("Size of file requested: " + fileSize / 1024L + " KB / " + fileSize / 1024L / 1024L + " MB");
        logger.log(Level.INFO, "Size of file requested: " + fileSize / 1024L + " KB / " + fileSize / 1024L / 1024L + " MB");

        try {
            fileDownloadInParallel(peerList, fileName, fileSize, isInter);
        } catch (RemoteException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void fileDownloadInParallel(List<ArrayList<String>> peerNodes, String fileName, long fileSize, IndexServerInterface isInter) throws RemoteException {
        int numChunks = (int) Math.ceil((double) fileSize / CHUNK_SIZE); // Calculate total number of chunks
        System.out.println("Number of chunks = " + numChunks);
        logger.info("Number of chunks = " + numChunks);
        ExecutorService executor = Executors.newFixedThreadPool(peerNodes.size()); // Create thread pool
        // Create a map to store downloaded chunks from each peer
        ConcurrentHashMap<Integer, byte[]> chunkMap = new ConcurrentHashMap<>();

        //##########################################################################
        // Download chunks from each peer in parallel
        for (int chunkNumber = 0; chunkNumber < numChunks; chunkNumber++) {
            final int chunkIndex = chunkNumber; // Current chunk index
            executor.execute(() -> {
                System.out.println("File download in process...");
                try {
                    // Get reference to the peer node registry
                    ArrayList<String> peerNode = peerNodes.get(chunkIndex % peerNodes.size()); // Select peer node in round-robin fashion
                    //0 filename, 1 peerid, 2 port_num, 3 direct
                    Registry registry = LocateRegistry.getRegistry("localhost", Integer.parseInt(peerNode.get(2)));
                    PeerDownloadInterface pdInter = (PeerDownloadInterface) registry.lookup("root://PeerTest/" + peerNode.get(2) + "/FS");

                    // Calculate start offset and end offset for this chunk
                    long startOffset = chunkIndex * (long) CHUNK_SIZE;
                    long endOffset = Math.min(startOffset + CHUNK_SIZE, fileSize);
                    int chunkSize = (int) (endOffset - startOffset);

                    // Download chunk from peer
                    byte[] chunkData = pdInter.downloadChunk(fileName, startOffset, chunkSize, peerNode.get(3));
                    System.out.println("Chunk " + chunkIndex + " downloaded successfully");
                    logger.info(chunkData.length + " downloaded from peer " + peerNode.get(1));
                    chunkMap.put(chunkIndex, chunkData); // Store downloaded chunk in map
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error downloading chunk", e);
                }
            });
        }
        executor.shutdown(); // Shutdown executor after all tasks are completed
        try {
            // Wait for all tasks to complete
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

            // Assemble downloaded chunks into a single file
            try (FileOutputStream outputStream = new FileOutputStream(dirName + File.separator + fileName)) {
                for (int chunkIndex = 0; chunkIndex < numChunks; chunkIndex++) {
                    byte[] chunkData = chunkMap.get(chunkIndex);
                    if (chunkData != null) {
                        outputStream.write(chunkData);
                        System.out.println("Chunk " + chunkIndex + " downloaded successfully");
                    } else {
                        System.out.println("Chunk " + chunkIndex + " not found");
                    }
                }
            }

            System.out.println("File downloaded successfully from multiple peers.");
            logger.info("File downloaded successfully from multiple peers.");
            // Update IndexServer indexes after downloading the file
            isInter.registryFiles("new", fileName, peerNodes.get(0).get(1), peerNodes.get(0).get(2), dirName, fileSize);
            // Check integrity of downloaded file
            boolean integrityCheck = checkIntegrity(dirName + File.separator + fileName, peerNodes.get(0));
            if (integrityCheck) {
                System.out.println("Integrity check passed: Downloaded file matches the original file.");
            } else {
                System.out.println("Integrity check failed: Downloaded file does not match the original file.");
                // Handle integrity check failure...
            }
        } catch (InterruptedException | IOException e) {
            logger.log(Level.SEVERE, "Error assembling file", e);
        }
    }

    // Implementation of the downloadChunk method
    @Override
    public byte[] downloadChunk(String fileName, long startOffset, int chunkSize, String dirName) throws RemoteException {
        try {
            String fname = fileName;
            String remoteDir = dirName;

            // Open the file
            File file = new File(remoteDir + File.separator + fname);
            logger.log(Level.INFO, "downloadChunk: file " + fname + " exists at " + remoteDir + " = " + file.exists());
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);

                // Set the file pointer to the startOffset
                fis.skip(startOffset);

                // Read the chunk of data
                byte[] chunkData = new byte[chunkSize];
                int bytesRead = bis.read(chunkData, 0, chunkSize);

                // Close the input streams
                bis.close();
                fis.close();

                // If no bytes were read, return null
                if (bytesRead == -1) {
                    return null;
                }

                // If fewer bytes were read than expected, create a new array with correct size
                if (bytesRead < chunkSize) {
                    byte[] trimmedData = new byte[bytesRead];
                    System.arraycopy(chunkData, 0, trimmedData, 0, bytesRead);
                    return trimmedData;
                }

                // Otherwise, return the chunkData
                return chunkData;
            }
        } catch (Exception e) {
            // Handle any exceptions
            logger.log(Level.SEVERE, "Error downloading chunk", e);
            throw new RemoteException("Error downloading chunk: " + e.getMessage());
        }
        return new byte[0];
    }

    private boolean checkIntegrity(String downloadedFilePath, ArrayList<String> peerNode) {
        try {
            // Calculate checksum of the downloaded file
            FileInputStream fis = new FileInputStream(downloadedFilePath);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            fis.close();
            byte[] downloadedChecksum = md.digest();

            // Calculate checksum of the original file
            Registry registry = LocateRegistry.getRegistry("localhost", Integer.parseInt(peerNode.get(2)));
            PeerDownloadInterface pdInter = (PeerDownloadInterface) registry.lookup("root://PeerTest/" + peerNode.get(2) + "/FS");
            byte[] output = pdInter.fileDownload(peerNode);
            MessageDigest originalMd = MessageDigest.getInstance("MD5");
            originalMd.update(output);
            byte[] originalChecksum = originalMd.digest();

            // Compare checksums
            return MessageDigest.isEqual(downloadedChecksum, originalChecksum);
        } catch (IOException | NoSuchAlgorithmException e) {
            Logger.getLogger(PeerImpl.class.getName()).log(Level.SEVERE, "Error checking integrity", e);
            return false; // Integrity check failed due to exception
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

    // Download task class for downloading a chunk from a peer node
   /* private static class DownloadTask implements Runnable {
        private ArrayList<String> peerNode;
        private String fileName;
        private long startOffset;
        private int chunkSize;
        private final IndexServerInterface isInter;
        private String peerDir;

        public DownloadTask(ArrayList<String> peerNode, String fileName, long startOffset, int chunkSize, IndexServerInterface isInter, String peerDir) {
            this.peerNode = peerNode;   // where from to download
            this.fileName = fileName;
            this.startOffset = startOffset;
            this.chunkSize = chunkSize;
            this.isInter = isInter;
            this.peerDir = peerDir;
        }

        @Override
        public void run() {
            try {
                // Get reference to the peer node registry
                // als.get(2) gives port number
                Registry regis2 = LocateRegistry.getRegistry("localhost", Integer.parseInt(peerNode.get(2)));
                PeerDownloadInterface pdInter = (PeerDownloadInterface) regis2.lookup("root://PeerTest/" + peerNode.get(2) + "/FS");

                // Call the method on the peer node to download the chunk
                byte[] chunkData = pdInter.downloadChunk(fileName, startOffset, chunkSize, peerNode.get(3));

                // Process the downloaded chunk data (e.g., save to file, etc.)
                // Converting Downloaded byte array into file
                if (chunkData.length != 0) {
                    FileOutputStream ostream = null;
                    try {
                        ostream = new FileOutputStream(peerDir + File.separator + fileName);
                        ostream.write(chunkData);
                        logger.info(chunkData.length + " bytes downloaded from " + peerNode.get(3));
                        //Updating the IndexServer Indexes after downloading the file.
                        isInter.registryFiles("new", fileName, peerNode.get(1), peerNode.get(2), peerDir, chunkData.length);
                    } catch (Exception e) {
                        System.out.println("Exception in bytearray to file conversion. " + e.getMessage());
                        logger.log(Level.SEVERE, "Exception in bytearray to file conversion. " + e.getMessage());
                    } finally {
                        ostream.close();
                        return;
                    }
                } else {
                    System.out.println("File not found at the peer location");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "download - run()", e);
                //e.printStackTrace();
            }
        }
    }*/
}
