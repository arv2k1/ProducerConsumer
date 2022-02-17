package com.finalized;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class MyArrayBlockingQueue implements Serializable {
	private static final long serialVersionUID = 1L;
	private final Integer[] queue;
	private final int capacity;
	// left and right never points to the same position ever.
	private AtomicInteger left = new AtomicInteger(-1);
	private AtomicInteger right = new AtomicInteger(0);
	private final AtomicInteger size = new AtomicInteger();

	public MyArrayBlockingQueue(int capacity) {
		this.capacity = capacity;
		this.queue = new Integer[capacity];
	}

	private void addToLast(Integer e) {
		queue[right.get()] = e;
		right.set((right.get() + 1) % capacity);
		size.incrementAndGet();
	}

	private Integer removeFromFront() {
		left.set((left.get() + 1) % capacity);
		Integer polled = queue[left.get()];
		size.decrementAndGet();
		return polled;
	}

	public void put(Integer e) {
		if (!queueFull())
			addToLast(e);
	}

	public Integer take() {
		if (queueEmpty())
			return null;
		Integer polled = removeFromFront();
		return polled;
	}

	public boolean queueFull() {
		return (size.get() == capacity);
	}

	public boolean queueEmpty() {
		return (size.get() == 0);
	}

	public int size() {
		return size.get();
	}
}
