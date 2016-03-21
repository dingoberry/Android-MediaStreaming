package com.bbq.w.library;

import android.util.Log;

public class LogLib {

	private final static String TAG = "yymm";

	public static void v(String msg) {
		Log.v(TAG, msg);
	}

	public static void d(String msg) {
		Log.d(TAG, msg);
	}

	public static void w(String msg) {
		Log.w(TAG, msg);
	}

	public static void w(String msg, Exception e) {
		Log.w(TAG, msg, e);
	}

	public static void w(Exception e) {
		w("", e);
	}

	public static void e(String msg, Exception e) {
		Log.e(TAG, msg, e);
	}

	public static void e(Exception e) {
		e("", e);
	}
}
