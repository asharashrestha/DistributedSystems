package edu.uta.distributed.bonus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

//TODO 
//Maintain Files in dictionary with their last modified date time .. DONE
//Check The difference in modification
// Make a list
// Call server method to connect and synchronize

public class Helper {
	private final static String clientFolder = "ClientFileStorage_Test";
	private final static String serverFolder = "ServerFileStorage_Test";
	private static final Logger LOGGER = Logger.getLogger(Helper.class.getName());
	File cfolder, sFolder;
	Map<String, Long> fileLastModifiedinServer, oldClientFileMap, newClientFileMap;
	public final static int PORT = 8080;
	public final static String IPADD = "localhost";
	private final Client helperClient;

	public Helper(String clientFolder, String serverFolder) {
		cfolder = new File(clientFolder);
		sFolder = new File(serverFolder);
		helperClient = new Client(IPADD, PORT);

		fileLastModifiedinServer = new HashMap<String, Long>();
		oldClientFileMap = new HashMap<String, Long>();
		newClientFileMap = new HashMap<String, Long>();

		syncInitial();
	}

	public void syncInitial() {
		File[] listOfFilesinClient = cfolder.listFiles();
		File[] listOfFilesinServer = sFolder.listFiles();
		List<String> clientFileNames = new ArrayList<String>();
		List<String> serverFilesNames = new ArrayList<String>();

		for (File f : listOfFilesinClient) {
			clientFileNames.add(f.getName());
		}
		for (File f : listOfFilesinServer) {
			serverFilesNames.add(f.getName());
		}
		System.out.println("=====SYNCING =====");
		for (String fileName : clientFileNames) {
			if (!serverFilesNames.contains(fileName)) {
				LOGGER.info("File being Uploaded to Server : " + fileName);
				System.out.println("File being Uploaded to Server : " + fileName);
				helperClient.uploadFile(fileName);
			}
		}
		for (String fileName : serverFilesNames) {
			if (!clientFileNames.contains(fileName)) {
				LOGGER.info("File being Downloaded From Server : " + fileName);
				System.out.println("File being Downloaded From Server : " + fileName);
				helperClient.downloadFile(fileName);
			}
		}
		System.out.println("=====SYNCING COMPLETED=====");

		listOfFilesinClient = cfolder.listFiles();
		for (int i = 0; i < listOfFilesinClient.length; i++) {
			oldClientFileMap.put(listOfFilesinClient[i].getName(), listOfFilesinClient[i].lastModified());
			LOGGER.info("File Name and Last Modified " + listOfFilesinClient[i].getName() + " "
					+ listOfFilesinClient[i].lastModified());
		}
		LOGGER.info("HashMapCreated of size : " + oldClientFileMap.size());

	}

	public void sync() {
		File[] listOfFilesinClient = cfolder.listFiles();
		System.out.println("=====SYNCING======");
		for (int i = 0; i < listOfFilesinClient.length; i++) {
			LOGGER.info("Value: " + oldClientFileMap.get(listOfFilesinClient[i].getName()) + " Key: "
					+ listOfFilesinClient[i].getName() + ":"
					+ !oldClientFileMap.containsKey(listOfFilesinClient[i].getName()));
			if (!oldClientFileMap.containsKey(listOfFilesinClient[i].getName())) {
				System.out.println(listOfFilesinClient[i].getName() + " is being uploaded");
				helperClient.uploadFile(listOfFilesinClient[i].getName());
				System.out.println(listOfFilesinClient[i].getName() + " Upload Complete\n");
				newClientFileMap.put(listOfFilesinClient[i].getName(), listOfFilesinClient[i].lastModified());

				LOGGER.info("New File Name and Last Modified (Create) " + listOfFilesinClient[i].getName() + " "
						+ listOfFilesinClient[i].lastModified());
			} else if (oldClientFileMap.containsKey(listOfFilesinClient[i].getName())) {
				if (oldClientFileMap.get(listOfFilesinClient[i].getName()) < listOfFilesinClient[i].lastModified()) {
					System.out.println(listOfFilesinClient[i].getName() + " is updating");
					helperClient.uploadFile(listOfFilesinClient[i].getName());
					System.out.println(listOfFilesinClient[i].getName() + " Update complete\n");
					LOGGER.info("File Name and Last Modified UPDATED last modified(Update)"
							+ listOfFilesinClient[i].getName() + " " + listOfFilesinClient[i].lastModified());
				}
				newClientFileMap.put(listOfFilesinClient[i].getName(), listOfFilesinClient[i].lastModified());
			}
		}
		Iterator<Entry<String, Long>> it = oldClientFileMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Long> pair = (Map.Entry<String, Long>) it.next();
			if (!newClientFileMap.containsKey(pair.getKey())) {
				System.out.println("Deleting " + pair.getKey());
				helperClient.deleteFile(pair.getKey());
				System.out.println(pair.getKey() + "Deleted\n");
			}
			it.remove(); // to avoid ConcurrentModificationException
		}
		Map tmp = new HashMap(newClientFileMap);
		tmp.keySet().removeAll(oldClientFileMap.keySet());
		oldClientFileMap.putAll(tmp);
		System.out.println("=====SYNCING COMPLETE======");
	}

	public static void main(String[] args) {
		Helper h = new Helper(clientFolder, serverFolder);
		while (true) {
			h.sync();
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
