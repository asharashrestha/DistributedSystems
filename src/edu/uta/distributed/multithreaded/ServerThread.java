package edu.uta.distributed.multithreaded;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.Socket;

public class ServerThread implements Runnable {
	// private static final Logger LOGGER =
	// Logger.getLogger(Server.class.getName());
	private Socket clientsocket = null;// to communicate with client
	// Input From Client
	private DataInputStream inputFromClient = null;
	// Output Stream to client
	private DataOutputStream OutputFromServer = null;
	private int clientNum;
	public final static int PORT = 6666;
	private final static String serverFolder = "ServerFileStorage";
	private static ReadWriteLock lock = new ReadWriteLock();

	public ServerThread(Socket socketClient, int i) {
		try {
			clientsocket = socketClient;
			clientNum = i;
			System.out
					.println("Connection Successful .. Client  " + clientNum + "accepted.. Waiting from Client Input");
		} catch (Exception e) {
			System.out.println("Server ShutDown Unexpectedly");
		}
	}

	public void run() {
		processClientrequest();
	}

	private void processClientrequest() {
		String task;
		try {
			OutputFromServer = new DataOutputStream(clientsocket.getOutputStream());
			inputFromClient = new DataInputStream(clientsocket.getInputStream());

			task = inputFromClient.readUTF().trim();
			// LOGGER.info("Task : " + task);

			if (task.compareTo("upload") == 0) {

				String fileName = inputFromClient.readUTF().trim();
				try {
					lock.WriteLock(fileName, clientNum, "upload");
					// LOGGER.info("FileName : " + fileName);
					long size = inputFromClient.readLong();
					// LOGGER.info("File Size sent by Client " + size);
					OutputFromServer.writeUTF("ACK");
					inputFromClient = new DataInputStream(clientsocket.getInputStream());

					getFileFromClient(fileName, size);

					OutputFromServer.flush();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						lock.WriteUnlock(fileName);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			} else if (task.compareTo("download") == 0) {
				String fileName = inputFromClient.readUTF().trim();
				// TODO Check if file requested is in the directory
				try {
					lock.ReadLock(fileName, clientNum, "download");
					sendFileToClient(fileName);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					lock.Readunlock(fileName);
				}

			} else if (task.compareTo("delete") == 0) {
				String fileName = inputFromClient.readUTF().trim();
				try {
					lock.ReadLock(fileName, clientNum, "delete");
					deleteFile(fileName);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					lock.Readunlock(fileName);
				}
			} else if (task.compareTo("rename") == 0) {

				String oldfileName = inputFromClient.readUTF().trim();
				String newfileName = inputFromClient.readUTF().trim();
				try {
					lock.WriteLock(oldfileName, clientNum, "rename");
					rename(oldfileName, newfileName);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						lock.WriteUnlock(oldfileName);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		} catch (IOException i) {
			i.printStackTrace();
		} catch (NullPointerException n) {
			n.printStackTrace();

		}

	}

	private void rename(String oldfileName, String newfileName) {
		// TODO Auto-generated method stub
		try {
			File oldFile = new File(serverFolder + "/" + oldfileName);
			File newFile = new File(serverFolder + "/" + newfileName);

			// LOGGER.info("File Exists :" + oldFile.exists());
			// LOGGER.info("File: " + oldFile.getName() + " renamed to " +
			// newFile.getName());
			if (oldFile.renameTo(newFile)) {
				OutputFromServer.writeUTF("ACK");
				OutputFromServer.writeUTF("File Renamed requested by Client " + clientNum);
			} else {
				OutputFromServer.writeUTF("ACK");
				OutputFromServer.writeUTF("File Doesnot Exist requested by Client " + clientNum);
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

			// LOGGER.info("File Exists :" + file.exists());
			if (file.delete()) {
				OutputFromServer.writeUTF("ACK");
				OutputFromServer.writeUTF("File Deleted requested by Client " + clientNum);
			} else {
				OutputFromServer.writeUTF("ACK");
				OutputFromServer.writeUTF("File Doesnot Exist requested by Client " + clientNum);
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

			// LOGGER.info("File Exists :" + file.exists());
			FileOutputStream fileOutputStream = new FileOutputStream(file);

			byte[] buffer = new byte[2048];
			System.out.println("Reading file from Client " + clientNum + " and saving it server...");
			int bytesRead;
			while (fileSize > 0
					&& (bytesRead = inputFromClient.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
				fileOutputStream.write(buffer);
				fileSize -= bytesRead;
				// LOGGER.info("This is loop: " + bytesRead);
			}
			System.out.println("File is uploaded to server of  Client " + clientNum);
			fileOutputStream.close();
			inputFromClient.close();
		} catch (IOException i) {
			i.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Writing file complete...Client" + clientNum);
		}
	}

	public void sendFileToClient(String fileName) {
		File file = new File(serverFolder + "/" + fileName);
		// LOGGER.info("FILE exists" + file.exists());
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
			// LOGGER.info("Size of File to Be Sent : " + file.length());
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
			System.out.println("File " + fileName + " Sent to Client " + clientNum);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
