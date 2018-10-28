package edu.uta.distributed.multithreaded;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;


public class ClientThread implements Runnable {

	/**
	 * @param args
	 */

	//private static final //LOGGER //LOGGER = //LOGGER.get//LOGGER(Client.class.getName());
	private Socket clientSocket = null;
	private DataInputStream input = null;
	private DataOutputStream output = null;

	private final static String IPADD = "localhost";
	private final static int PORT = 8080;
	private final static String clientFolder = "ClientFileStorage";

	public void connect() {
		try {
			clientSocket = new Socket(IPADD, PORT);
			System.out.println("Connected to Server");
			output = new DataOutputStream(clientSocket.getOutputStream());
			input = new DataInputStream(clientSocket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();

		}
	}

	public void uploadFile(String FileName) {
		File file = new File(clientFolder + "/" + FileName);
		//LOGGER.info(FileName + "file Exist:" + file.exists());
		if (!file.exists()) {
			System.out.println("File Doesnot Exists");
			return;
		}

		try {
			connect();
			output.writeUTF("upload");
			output.writeUTF(file.getName() + "\n");
			output.writeLong(file.length());
			long size = file.length();
			// output.writeLong(file.length());
			// Wait for Server to Acknowledge
			while (true) {
				if (input.readUTF().compareTo("ACK") == 0) {
					System.out.println("Acknowledge Received from Server");
					break;
				}
			}
			output.flush();

			input = new DataInputStream(new FileInputStream(file));
			//LOGGER.info("Input Stream Size" + input.available());
			byte[] b = new byte[4096];
			int bytesRead;
			while (size > 0 && (bytesRead = input.read(b, 0, (int) Math.min(b.length, size))) != -1) {
				output.write(b);
				size -= bytesRead;
				// //LOGGER.info("Size Left : " + size);
			}
			input.close();
			// Important to Flush after writing to it
			output.flush();
			output.close();
			System.out.println("File " + FileName + " Sent to Server");
			closeConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void downloadFile(String FileName) {
		File file = new File(clientFolder + "/" + FileName);
		long sizeOfFile = 0;
		//LOGGER.info("FileName Downloaded : " + FileName);
		try {
			connect();
			output.writeUTF("download\n");
			output.writeUTF(FileName + "\n");
			output.writeLong(file.length());
			// Wait for Server to Acknowledge and Size
			while (true) {

				try {
					String serverResponse = input.readUTF();
					if (serverResponse.compareTo("ACK") == 0) {
						sizeOfFile = input.readLong();
						break;
					} else if (serverResponse.compareTo("NACK") == 0) {
						System.out.println("SERVER REPLY : " + input.readUTF());
						output.flush();
						closeConnection();
						return;
					}
				} catch (Exception e) {
					System.out.println("SERVER REPLY : " + input.readUTF());
					output.flush();
					e.printStackTrace();
				}
			}
			System.out.println("ACK Received from Server");
			output.flush();
			//LOGGER.info("File Size to be received : " + sizeOfFile);
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			// Download the File
			int bytesRead;
			byte[] buffer = new byte[2048];
			while (sizeOfFile > 0
					&& (bytesRead = input.read(buffer, 0, (int) Math.min(buffer.length, sizeOfFile))) != -1) {
				fileOutputStream.write(buffer);
				sizeOfFile -= bytesRead;
				//LOGGER.info("Downloading File | Reading File from the stream");

			}

			fileOutputStream.flush();
			fileOutputStream.close();
			System.out.println("File " + FileName + " Received From Server");
			closeConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String loadMenuOption() {
		Scanner s = new Scanner(System.in);
		System.out.println(
				"Press 1 to upload Separated by Filename(Example : 1 client.txt): \n Press 2 to download Separated by Filename(Example : 2 server.txt)\n "
						+ "Press 3 to delete Separated by Filename(Example : 3 server.txt): \n Press 4 to rename Separated by Filename(Example : 4 server.txt newserver.txt)"
						+ "\n Press 5 to quit");
		return s.nextLine();
	}

	
	private void renameFile(String oldFileName, String newFileName) {
		// TODO Auto-generated method stub
		try {
			connect();
			output.writeUTF("rename ");
			output.writeUTF(oldFileName + "\n");
			output.writeUTF(newFileName + "\n");
			String serverMessage;
			while (true) {
				if (input.readUTF().compareTo("ACK") == 0) {
					serverMessage = input.readUTF();
					break;
				}
			}
			System.out.println("Server Reply : " + serverMessage);
			//LOGGER.info("Server Reply : " + serverMessage);
			output.flush();
			output.close();
			input.close();
			closeConnection();
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	public void deleteFile(String fileName) {
		// TODO Auto-generated method stub
		try {
			connect();
			output.writeUTF("delete\n");
			output.writeUTF(fileName + "\n");
			String serverMessage;
			while (true) {
				if (input.readUTF().compareTo("ACK") == 0) {
					serverMessage = input.readUTF();
					break;
				}
			}
			System.out.println("Server Reply : " + serverMessage);
			//LOGGER.info("Server Reply : " + serverMessage);
			output.flush();
			output.close();
			input.close();
			closeConnection();
		} catch (IOException i) {
			i.printStackTrace();

		}
	}

	public void closeConnection() {
		try {
			System.out.println("Disconnected from Server");
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();

		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Random rand = new Random();

		// Generate random integers in range 0 to 999
		int rand_int1 = rand.nextInt(2);
		switch (rand_int1) {
		case 0:
			uploadFile("LargeClient.txt");
			break;
		case 2:
			downloadFile("LargeClient.txt");
			break;
		case 3:
			deleteFile("LargeClient.txt");
			break;
		case 1:
			downloadFile("LargeClient.txt");
			break;
		}
	}

}
