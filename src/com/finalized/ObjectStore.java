package com.finalized;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ObjectStore {
	private String path = "C:\\Users\\gnana-pt4726\\Desktop\\New\\files";
	private File file = new File(path);
	private AtomicLong size = new AtomicLong();
	Lock readLock = new ReentrantLock();

	public ObjectStore() {
		for (File subfile : file.listFiles()) {
			if (subfile != null)
				subfile.delete();
			else
				break;
		}

	}

	public Long size() {
		return size.get();
	}

	public void writeQueue(MyArrayBlockingQueue producerCopy) throws IOException {
		size.getAndIncrement();
		Path p = Paths.get(path + "/" + System.currentTimeMillis() + ".writing.txt");
		File file = p.toFile();
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
			System.out.println("Writing to file: " + producerCopy);
			oos.writeObject(producerCopy);
		}
		file.renameTo(new File(path + "/" + System.currentTimeMillis() + ".txt"));

	}

	public MyArrayBlockingQueue readQueue() throws IOException, ClassNotFoundException, NumberFormatException {
		size.getAndDecrement();
		MyArrayBlockingQueue consumer = null;
		readLock.lock();
		File[] files = file.listFiles();
		Arrays.sort(files);
		if (files.length > 0) {

			Path p = files[0].toPath();
			if (!p.toString().contains("writing")) {
				System.out.println(p);
				try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(p.toFile()))) {
					System.out.println("Reading occurs");
					consumer = (MyArrayBlockingQueue) ois.readObject();
				} finally {
					p.toFile().delete();
					System.out.println("Reading over");
					readLock.unlock();
				}
			}
		}

		return consumer;
	}

}
