package com.finalized;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ObjectStore {
	private final String folderPath = "C://Users//Aravinth//Desktop//AGS//ProducerConsumer//files" ;
	private final File folder = new File(folderPath);

	private final AtomicLong size = new AtomicLong();
	private final Lock readLock = new ReentrantLock();

	public ObjectStore() {
		clearDirectory() ;
	}

	private void clearDirectory() {
		Arrays.stream(folder.listFiles())
				.filter(File::isFile)
				.forEach(File::delete) ;
	}

	public void writeQueue(MyArrayBlockingQueue queue) {
		size.incrementAndGet();

		final long timeStamp = System.currentTimeMillis();

		Path lockedFilePath = Paths.get(folderPath + File.separator + timeStamp + ".txt.locked");
		Path unlockedFilePath = Paths.get(folderPath + File.separator + timeStamp + ".txt") ;

		try (OutputStream fos = Files.newOutputStream(lockedFilePath);
			 ObjectOutputStream oos = new ObjectOutputStream(fos))
		{
			System.out.println("Writing to file: " + queue);
			oos.writeObject(queue);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}

		rename(lockedFilePath, unlockedFilePath) ;
	}

	public MyArrayBlockingQueue readQueue() {
		final File file = getMinFileInDirectory();
		size.getAndDecrement();

		try (InputStream is = new FileInputStream(file);
			 ObjectInputStream ois = new ObjectInputStream(is))
		{
			System.out.println("Reading occurs");
			MyArrayBlockingQueue queue = (MyArrayBlockingQueue) ois.readObject();
			System.out.println("Read from file: " + queue);
			return queue ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		} finally {
			file.delete();
			System.out.println("Reading over");
		}
	}

	private File getMinFileInDirectory() {
		final FileFilter unlockedFileFilter = f -> f.getName().endsWith("txt") ;

		Optional<File> min = Optional.empty() ;

		while(min.isEmpty()) {
			File[] unlockedFiles = folder.listFiles(unlockedFileFilter) ;
			min = Arrays.stream(unlockedFiles).min(File::compareTo);
		}

		return min.get();
	}

	private void rename(Path oldName, Path newName) {
		try {
			Files.move(oldName, newName) ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	public long size() {
		return size.get();
	}
}
