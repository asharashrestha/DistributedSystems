package edu.uta.distributed.multithreaded;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ReadWriteLock {
	private static final Logger LOGGER = Logger.getLogger(ReadWriteLock.class.getName());
	private int readers = 0;
	private int writeRequests = 0;
	private int writers = 0;

	private List<String> locks;

	public ReadWriteLock() {
		locks = new ArrayList<String>();
	}

	public synchronized void ReadLock(String Filename, int clientNum, String task) throws InterruptedException {
		while (locks.contains(Filename) & (writers > 0 || writeRequests > 0)) {
			wait();
			System.out.println("\nClient" + clientNum + " is waiting to " + task + " " + Filename+"\n");
		}
		readers++;
	}

	public synchronized void Readunlock(String Filename) {
		readers--;
		notifyAll();
	}

	public synchronized void WriteLock(String Filename, int clientNum, String task) throws InterruptedException {
		writeRequests++;
		
		LOGGER.info("Client" + clientNum +" "+Filename + ":" + locks.contains(Filename));
		LOGGER.info("Bool: "+(locks.contains(Filename) & (readers > 0 || writers > 0)));
		while (locks.contains(Filename) & (readers > 0 || writers > 0)) {
			wait();
			System.out.println("\nClient" + clientNum + " is waiting to " + task + " " + Filename+"\n");
		}
		locks.add(Filename);

		writeRequests--;
		writers++;
	}

	public synchronized void WriteUnlock(String Filename) throws InterruptedException {
		locks.remove(Filename);
		writers--;
		notifyAll();
	}
}
