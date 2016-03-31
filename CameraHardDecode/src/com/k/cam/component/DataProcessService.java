package com.k.cam.component;

import com.bbq.w.library.LogLib;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;

public class DataProcessService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		LogLib.d("DataProcessService:onCreate");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		LogLib.d("DataProcessService:onDestroy");

		Process.killProcess(Process.myPid());
	}
}
