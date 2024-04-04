package com.aos;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;


//   This contains the main method for setting up the Peer.
public interface Peer {
    public static void main(String[] args) throws RemoteException {
        Scanner sc = new Scanner(System.in);
        String portno = null;
        System.out.println("Enter the port number for this peer ");
        portno = sc.nextLine();

        System.out.println("Enter the directory path to register with the Indexing Server");
        String directoryName = sc.nextLine();

        // Registering the peer on specified port & setting up the remote object
        Registry registry = LocateRegistry.createRegistry(Integer.parseInt(portno));
        ClientInterface ciImpl = new ClientInterface(portno, directoryName);
        PeerDownloadInterface pdInter = (PeerDownloadInterface) UnicastRemoteObject.exportObject(ciImpl, 0);
        registry.rebind("root://PeerTest/" + portno + "/FS", pdInter);
        System.out.println("********** PEER CLIENT STARTED **********");
        try {
            System.out.println("1. Work\n2. Work Parallel");
            int work = sc.nextInt();
            switch (work) {
                case 1:
                    ciImpl.doWork();
                    break;
                case 2:
                    ciImpl.doParallelWork();
                    break;
                default:
                    System.out.println("Invalid work choice");
            }
            ciImpl.doWork();
        } catch (IOException e) {
            System.out.println("IO Exception at Peer Main" + e.getMessage());
            e.printStackTrace();
        }
    }
}
