package com.bbq.w.library;

public class ThreadUtils {

	public static void runAloneThread(Runnable runnable) {
		new Thread(runnable).start();
	}
}
