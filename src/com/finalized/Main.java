package com.finalized;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

	public static void main(String[] args) {
		WrapperQueue q = new WrapperQueue(5);

		final AtomicInteger nextNum = new AtomicInteger() ;

		final Runnable producer = () -> {
			while (true) {
				try {
					int r = nextNum.getAndIncrement() ;
					System.out.println("Producing :" + r);
					q.put(r);
					TimeUnit.SECONDS.sleep(1);
				} catch (Exception e) {
					throw new RuntimeException(e) ;
				}
			}
		};

		final Runnable consumer = () -> {
			while (true) {
				try {
					Integer val = q.take();
					System.out.println("consuming :" + val);
					TimeUnit.SECONDS.sleep(1);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		for (int i = 0; i < 5; i++) {
			new Thread(producer).start();
			new Thread(producer).start();
			new Thread(consumer).start();
		}

//		while (true) {
//			try {
//				new Thread(producer).start();
//				TimeUnit.SECONDS.sleep(1);
//				new Thread(consumer).start();
//				TimeUnit.SECONDS.sleep(1);
//			} catch (Exception e) {
//				throw new RuntimeException(e) ;
//			}
//		}

	}
}
