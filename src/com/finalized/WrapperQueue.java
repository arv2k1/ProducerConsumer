package com.finalized;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WrapperQueue {
	private AtomicReference<MyArrayBlockingQueue> producerQueue;
	private AtomicReference<MyArrayBlockingQueue> consumerQueue;
	private final ObjectStore fileQueue;
	private final int Capacity;
	private AtomicInteger filePointer = new AtomicInteger(0);
	private Lock putLock = new ReentrantLock();
	ExecutorService pool = Executors.newCachedThreadPool();

	public WrapperQueue(int capacity) {

		producerQueue = new AtomicReference<MyArrayBlockingQueue>(new MyArrayBlockingQueue(capacity));
		// Create some Extra Space for consumer to consume values from queue after
		// reading occurs from file
		consumerQueue = new AtomicReference<MyArrayBlockingQueue>(new MyArrayBlockingQueue(capacity));
		fileQueue = new ObjectStore();
		this.Capacity = capacity;

		// Tries to keep the consumerQueue full at all times
		Thread moveFromFileDaemon = new Thread(() -> {
			while (true)
				try {
					moveFromFile();
				} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
				}
		});
		moveFromFileDaemon.setDaemon(true);
		moveFromFileDaemon.start();
	}

	// 2.take values from the producer queue and place it in the file at a time.
	private void moveToFile(MyArrayBlockingQueue producerCopy) throws IOException {

		fileQueue.writeQueue(producerCopy);
	}

	// 3.take values from file and put it in consumer queue and always make it full
	// using service Threads
	private void moveFromFile() throws ClassNotFoundException, IOException {
		if (filePointer.get() > 0) {
			MyArrayBlockingQueue obj = fileQueue.readQueue();
			filePointer.getAndDecrement();
			while (consumerQueue.get().size() > 0)
				;
			if (obj != null)
				consumerQueue.set(obj);
		}
	}

	// 1.Producer put values in the producer queue
	public void put(Integer e) {
		putLock.lock();
		try {
			MyArrayBlockingQueue obj = producerQueue.get();
			if (obj.queueFull()) {

				MyArrayBlockingQueue producerCopy = obj;

				producerQueue.set(new MyArrayBlockingQueue(Capacity));

				pool.execute(() -> {
					try {
						moveToFile(producerCopy);
						filePointer.getAndIncrement();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				});
			}
			producerQueue.get().put(e);
		} finally {
			putLock.unlock();
		}
	}

	// 4.Consumer consumes it from Consumer queue if any else consumes from producer
	// queue
	public Integer take() throws ClassNotFoundException, IOException {
		MyArrayBlockingQueue consumer = consumerQueue.get();
		MyArrayBlockingQueue producer = producerQueue.get();
		while (consumerQueue.get().queueEmpty())
			if (fileQueue.size() == 0) {
				System.out.println("Consumes from producer");
				return producer.take();
			}
		return consumer.take();
	}

}
