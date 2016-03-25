package com.bbq.w.library;

public class ThreadUtils {

	public static Thread runAloneThread(Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.start();
		return thread;
	}
}
