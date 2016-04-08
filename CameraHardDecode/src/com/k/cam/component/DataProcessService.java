package com.k.cam.component;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;

import com.bbq.w.library.LogLib;
import com.k.cam.Configuration;

public class DataProcessService extends Service {

	private final static String TAG = "DataProcessService";
	private static final boolean DEBUG = Configuration.DEBUG;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (DEBUG) {
			LogLib.d(TAG, "DataProcessService:onCreate");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DEBUG) {
			LogLib.d(TAG, "DataProcessService:onDestroy");
		}

		Process.killProcess(Process.myPid());
	}
}
