package edu.uta.distributed.singlethread;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class Server {
	private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
	private Socket clientsocket = null;// to communicate with client
	private ServerSocket server = null; // it listens to port for request
	// Input From Client
	private DataInputStream inputFromClient = null;
	// Output Stream to client
	private DataOutputStream OutputFromServer = null;

	private BufferedInputStream buff = null;
	public final static int PORT = 6666;
	private final static String serverFolder = "ServerFileStorage";

	public Server(int port) {
		try {
			// Creates a Folder for Storing Files for Server
			if (new File(serverFolder).mkdirs())
				System.out.println("Server Folder Created ");
			else
				System.out.println("Folder already exist");
			server = new ServerSocket(port);
		} catch (Exception e) {
			System.out.println("Server ShutDown Unexpectedly");
			e.printStackTrace();
		}
	}

	public void start() {
		try {
			System.out.println("Server Started");
			while (true) {
				System.out.println("Server Ready for Connection.. Waiting for Client");
				// Listening to PORT by Server
				clientsocket = server.accept();
				System.out.println("Connection Successful .. Client accepted.. Waiting from Client Input");
				processClientrequest();

			}

		} catch (IOException i) {
			System.out.println("Connection Error");
			i.printStackTrace();
			System.exit(-1);
		}
	}

	private void processClientrequest() {
		String task;
		try {
			OutputFromServer = new DataOutputStream(clientsocket.getOutputStream());
			inputFromClient = new DataInputStream(clientsocket.getInputStream());

			task = inputFromClient.readUTF().trim();
			LOGGER.info("Task : " + task);
			if (task.compareTo("upload") == 0) {
				String fileName = inputFromClient.readUTF().trim();
				LOGGER.info("FileName : " + fileName);
				long size = inputFromClient.readLong();
				LOGGER.info("File Size sent by Client " + size);
				OutputFromServer.writeUTF("ACK");
				inputFromClient = new DataInputStream(clientsocket.getInputStream());

				getFileFromClient(fileName, size);

				OutputFromServer.flush();
			} else if (task.compareTo("download") == 0) {
				String fileName = inputFromClient.readUTF().trim();
				// TODO Check if file requested is in the directory

				sendFileToClient(fileName);
			} else if (task.compareTo("delete") == 0) {
				String fileName = inputFromClient.readUTF().trim();
				deleteFile(fileName);
			} else if (task.compareTo("rename") == 0) {

				String oldfileName = inputFromClient.readUTF().trim();
				String newfileName = inputFromClient.readUTF().trim();
				rename(oldfileName, newfileName);
			}

		} catch (IOException i) {
			i.printStackTrace();
		} catch (NullPointerException n) {
			n.printStackTrace();
			start();

		}

	}

	private void rename(String oldfileName, String newfileName) {
		// TODO Auto-generated method stub
		try {
			File oldFile = new File(serverFolder + "/" + oldfileName);
			File newFile = new File(serverFolder + "/" + newfileName);

			LOGGER.info("File Exists :" + oldFile.exists());
			LOGGER.info("File: " + oldFile.getName() + " renamed to " + newFile.getName());
			if (oldFile.renameTo(newFile)) {
				OutputFromServer.writeUTF("ACK");
				OutputFromServer.writeUTF("File Renamed");
			} else {
				OutputFromServer.writeUTF("ACK");
				OutputFromServer.writeUTF("File Doesnot Exist");
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			OutputFromServer.flush();
			OutputFromServer.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	private void deleteFile(String fileName) {
		// TODO Auto-generated method stub
		try {
			File file = new File(serverFolder + "/" + fileName);

			LOGGER.info("File Exists :" + file.exists());
			if (file.delete()) {
				OutputFromServer.writeUTF("ACK");
				OutputFromServer.writeUTF("File Deleted");
			} else {
				OutputFromServer.writeUTF("ACK");
				OutputFromServer.writeUTF("File Doesnot Exist");
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			OutputFromServer.flush();
			OutputFromServer.close();
		} catch (IOException i) {
			i.printStackTrace();
		}

	}

	private void getFileFromClient(String fileName, long fileSize) {
		try {
			File file = new File(serverFolder + "/" + fileName);

			LOGGER.info("File Exists :" + file.exists());
			FileOutputStream fileOutputStream = new FileOutputStream(file);

			byte[] buffer = new byte[2048];
			System.out.println("Reading file and saving it server...");
			int bytesRead;
			while (fileSize > 0
					&& (bytesRead = inputFromClient.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
				fileOutputStream.write(buffer);
				fileSize -= bytesRead;
				LOGGER.info("This is loop: " + bytesRead);
			}
			System.out.println("File is uploaded to server");
			LOGGER.info("File is uploaded to server");
			fileOutputStream.close();
			inputFromClient.close();
		} catch (IOException i) {
			i.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Writing file complete...");
		}

	}

	public void sendFileToClient(String fileName) {
		File file = new File(serverFolder + "/" + fileName);
		LOGGER.info("FILE exists" + file.exists());
		try {
			if (!file.exists()) {
				OutputFromServer.writeUTF("NACK");
				OutputFromServer.writeUTF("ERROR: Invalid File Requested");
				OutputFromServer.flush();
				// OutputFromServer.close();
				return;
			}
			OutputFromServer = new DataOutputStream(clientsocket.getOutputStream());
			OutputFromServer.writeUTF("ACK");
			OutputFromServer.writeLong(file.length());
			LOGGER.info("Size of File to Be Sent : " + file.length());
			OutputFromServer.flush();

			long size = file.length();
			DataInputStream serverFileStream = new DataInputStream(new FileInputStream(file));
			int bytesRead;
			byte[] b = new byte[4096];
			while (size > 0 && (bytesRead = serverFileStream.read(b, 0, (int) Math.min(b.length, size))) != -1) {
				OutputFromServer.write(b);
				size -= bytesRead;
				// //LOGGER.info("Size Left : " + size);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			OutputFromServer.flush();
			serverFileStream.close();
			OutputFromServer.close();
			System.out.println("File " + fileName + " Sent to Client");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Server server = new Server(PORT);
		server.start();
		server.close();
	}

	public void close() {
		// TODO Auto-generated method stub
		System.out.println("Closing Connnection");

		try {
			clientsocket.close();
			inputFromClient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
