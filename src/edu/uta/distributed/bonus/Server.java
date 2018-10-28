package edu.uta.distributed.bonus;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class Server {
	private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
	private static Socket clientsocket = null;// to communicate with client
	private static ServerSocket server = null; // it listens to port for request

	public final static int PORT = 8080;
	private final static String serverFolder = "ServerFileStorage_Test";

	public static void main(String[] args) {
		try {
			// Creates a Folder for Storing Files for Server
			if (new File(serverFolder).mkdirs())
				System.out.println("Server Folder Created ");
			else
				System.out.println("Folder already exist");
			server = new ServerSocket(PORT);

		} catch (Exception e) {
			System.out.println("Server ShutDown Unexpectedly");
			e.printStackTrace();
		}
		while (true) {
			try {
				LOGGER.info("Listing to Port : " + PORT);
				System.out.println("Listing to Port : " + PORT);
				clientsocket = server.accept();
				System.out.println("Connection Accepted ");
				Thread serverThread = new Thread(new ServerThread(clientsocket));
				serverThread.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		// server.close();
	}

}