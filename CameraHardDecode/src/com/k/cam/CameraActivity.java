package com.k.cam;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;

import com.bbq.w.library.CameraUtils;
import com.bbq.w.library.LogLib;

@SuppressWarnings("deprecation")
public abstract class CameraActivity extends Activity implements
		PreviewCallback {

	private Camera mCam;
	private AtomicBoolean mIsPreviewing = new AtomicBoolean();

	@Override
	protected void onPause() {
		super.onPause();
		Camera cam = mCam;
		if (mIsPreviewing.get() && cam != null) {
			cam.stopPreview();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Camera cam = mCam;
		if (mIsPreviewing.get() && cam != null) {
			cam.startPreview();
		}
	}

	protected void openCam(int id, int width, int height) {
		Camera cam = mCam;
		if (cam != null) {
			return;
		}

		cam = Camera.open(id);
		CameraUtils.fixCameraDegree(cam, id, this);
		CameraUtils.setPreviewSize(cam, width, height);
		mCam = cam;
	}

	protected boolean isPreviewing() {
		return mIsPreviewing.get();
	}

	protected void startPreview(SurfaceTexture surfaceTexture) {
		Camera cam = mCam;
		if (cam == null) {
			return;
		}
		cam.setPreviewCallback(this);
		try {
			cam.setPreviewCallback(this);
			cam.setPreviewTexture(surfaceTexture);
			cam.startPreview();
			mIsPreviewing.compareAndSet(false, true);
			mCam = cam;
			LogLib.d("startPreview");
		} catch (IOException e) {
			LogLib.w("IOException", e);
		}
	}

	protected void releaseCam() {
		Camera cam = mCam;
		if (cam == null) {
			return;
		}

		cam.setPreviewCallback(null);
		cam.stopPreview();
		mCam = null;
		mIsPreviewing.set(false);
	}
}
