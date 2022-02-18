package com.finalized;

import java.io.Serializable;
import java.util.Arrays;
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
		while(queueFull())
			;
		addToLast(e);
	}

	public Integer take() {
		while(queueEmpty())
			;
		return removeFromFront();
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

	@Override
	public String toString() {
		return "MyArrayBlockingQueue{" +
				"queue=" + Arrays.toString(queue) +
				'}';
	}
}
