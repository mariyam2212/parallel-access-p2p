package com.aos;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

public class AvgRespFileSearch implements Runnable, PeerDownloadInterface {
    String portNo = null;
    String dirName = null;
    String peerID = null;
    String fileName = "Process Flow.txt";
    Collection<ArrayList<String>> colArr;
    long start = 0;
    long end = 0;
    long responseTime = 0;
    int seqReq = 500;

    @Override
    public void run() {
        // TODO Auto-generated method stub
        doWork();
    }

    public void doWork() {
        try {
            // Locating Registry of Indexing Server and obtains target address
            Registry regis = LocateRegistry.getRegistry("localhost", 3788);
            IndexServerInterface isInter = (IndexServerInterface) regis.lookup("Indexing");

            Scanner sc = new Scanner(System.in);

            File dirList = new File(dirName);
            //List of all files in the directory
            String[] record = dirList.list();

            // Registering Files in Index Server
            for (int c = 0; c < record.length; c++) {
                File currentFile = new File(dirList, record[c]);
                System.out.println("Registering file " + currentFile.getName() + " in Indexing Server" + "of size = " + currentFile.length());
                isInter.registryFiles("new", currentFile.getName(), peerID, portNo, dirName, currentFile.length());
            }
            // Running Sequential 500 search requests to Index server
            start = System.nanoTime();
            System.out.println("start is: " + start);
            for (int i = 0; i < seqReq; i++) {
                colArr = isInter.searchFile(fileName);
            }
            end = System.nanoTime();
            System.out.println("end is: " + end);
            responseTime = (end - start) / 1000000; // Milliseconds
            //System.out.println("ResponseTime for PeerID " +peerID +" is "+ responseTime + " ms");
            float avgRespTime = (float) responseTime / seqReq;
            System.out.println("Avg Response time of PeerID " + peerID + " is " + avgRespTime + "ms");

        } catch (Exception e) {
            System.out.println("MultiClientFileSearch exception: " + e);
        }

    }

    @Override
    public byte[] fileDownload(ArrayList<String> searchedDir) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] downloadChunk(String fileName, long startOffset, int chunkSize, String dirName) throws RemoteException {
        return new byte[0];
    }

    // Initializing the variables with the help of constructors
    AvgRespFileSearch(String portNo, String dirName, String peerId) {
        this.portNo = portNo;
        this.dirName = dirName;
        this.peerID = peerId;
    }


}