package com.k.cam.component;

import com.bbq.w.library.LogLib;
import com.k.cam.R;

import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

@SuppressWarnings("deprecation")
public class NormalSurfaceFragment extends CameraFragment implements Callback {
	
	private final static int CAMERA_HEIGHT = 600;
	private final static int CAMERA_WIDTH = 800;

	private SurfaceView mSv;
	private SurfaceHolder mH;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View content = inflater.inflate(R.layout.normal_surface_fragment, container, false);
		mSv = (SurfaceView) content.findViewById(R.id.sv);
		SurfaceHolder holder = mSv.getHolder();
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		holder.addCallback(this);
		mH = holder;
		return content;
	}

	@Override
	public void onFrameArrival(byte[] data, Camera camera) {
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		LogLib.d("surfaceCreated");
		int id = resolveCameraId();
		if (INVALID_VALUE != id) {
			openCam(id, CAMERA_WIDTH, CAMERA_HEIGHT);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		LogLib.d("surfaceChanged");
		if (isPreviewing()) {
			return;
		}
		startPreview(mH);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		LogLib.d("surfaceDestroyed");
		releaseCam();
	}
}
