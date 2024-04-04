package com.aos;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

// Creating Remote Interface of Peer
public interface PeerDownloadInterface extends Remote {
    public byte[] fileDownload(ArrayList<String> searchedDir) throws RemoteException;

    // Implementation of the downloadChunk method
    byte[] downloadChunk(String fileName, long startOffset, int chunkSize, String dirName) throws RemoteException;
}