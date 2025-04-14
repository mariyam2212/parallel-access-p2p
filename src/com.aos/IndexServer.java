package com.aos;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class IndexServer {

	public static void main(String[] args) throws RemoteException {
	     // Registering the Index Server on hard-coded port and setting up the remote object 
 			 Registry registry = LocateRegistry.createRegistry(3788);
			 IndexServerImpl isImpl = new IndexServerImpl();
			 IndexServerInterface isInter = (IndexServerInterface)UnicastRemoteObject.exportObject(isImpl, 0);
			 registry.rebind("Indexing", isInter);
			 System.out.println("********** INDEXING SERVER STARTED **********");
	}
}
