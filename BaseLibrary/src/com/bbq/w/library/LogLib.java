package com.bbq.w.library;

import android.util.Log;

public class LogLib {

	private final static String TAG = "yymm";

	private static String getLogMsg(String tag, String msg) {
		return "(" + tag + ") " + msg;
	}

	public static void v(String tag, String msg) {
		Log.v(TAG, getLogMsg(tag, msg));
	}

	public static void d(String tag, String msg) {
		Log.d(TAG, getLogMsg(tag, msg));
	}

	public static void w(String tag, String msg) {
		Log.w(TAG, getLogMsg(tag, msg));
	}

	public static void w(String tag, String msg, Exception e) {
		Log.w(TAG, getLogMsg(tag, msg), e);
	}

	public static void w(String tag, Exception e) {
		w(tag, "", e);
	}

	public static void e(String tag, String msg, Exception e) {
		Log.e(TAG, getLogMsg(tag, msg), e);
	}

	public static void e(String tag, Exception e) {
		e(tag, "", e);
	}
}
