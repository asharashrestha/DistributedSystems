package edu.uta.distributed.multithreaded;

import java.io.File;

public class Client {

	/**
	 * @param args
	 */

	public final static String IPADD = "localhost";
	private final static String clientFolder = "ClientFileStorage";

	public static void main(String[] args) {
		if (new File(clientFolder).mkdirs())
			System.out.println("Client Folder Created ");
		else
			System.out.println("Folder already created");
		for (int i = 0; i < 30; i++) {
			Thread clientThread = new Thread(new ClientThread());
			clientThread.start();
			try {
				clientThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}