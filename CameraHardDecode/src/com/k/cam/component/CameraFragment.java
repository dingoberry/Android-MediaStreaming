package com.k.cam.component;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.support.v4.app.Fragment;
import android.view.SurfaceHolder;

import com.bbq.w.library.CameraUtils;
import com.bbq.w.library.LogLib;
import com.k.cam.Configuration;

@SuppressWarnings("deprecation")
public abstract class CameraFragment extends Fragment implements
		PreviewCallback {

	protected final static int INVALID_VALUE = -1;

	private Camera mCam;
	private FRAME_CALL_MODE mMode = FRAME_CALL_MODE.NONE;
	private AtomicBoolean mIsPreviewing = new AtomicBoolean();

	private int mWidth, mHeight;
	private byte[] mBuffer;
	
	protected abstract void onFrameArrival(byte[] data, Camera camera);

	@Override
	public final void onPreviewFrame(byte[] data, Camera camera) {
		onFrameArrival(data, camera);

		if (mMode == FRAME_CALL_MODE.BUFFER) {
			camera.addCallbackBuffer(mBuffer);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		LogLib.d("CameraFragment:onStart");
		Camera cam = mCam;
		if (mIsPreviewing.get() && cam != null) {
			cam.startPreview();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		LogLib.d("CameraFragment:onStop");
		Camera cam = mCam;
		if (mIsPreviewing.get() && cam != null) {
			cam.stopPreview();
		}
	}

	protected void setFrameMode(FRAME_CALL_MODE mode) {
		mMode = mode;
	}

	protected int resolveCameraId() {
		int cameraId = INVALID_VALUE;
		for (int id : CameraUtils.getCameraIds()) {
			if (INVALID_VALUE == cameraId) {
				cameraId = id;
			}

			if (Configuration.CURRENT_CAMERA_ID == id) {
				cameraId = id;
				break;
			}
		}
		return cameraId;
	}

	protected void openCam(int id, int width, int height) {
		Camera cam = mCam;
		if (cam != null) {
			return;
		}

		cam = Camera.open(id);
		CameraUtils.fixCameraDegree(cam, id, getActivity());
		CameraUtils.setPreviewSize(cam, width, height);
		mCam = cam;
		mWidth = width;
		mHeight = height;
	}

	protected boolean isPreviewing() {
		return mIsPreviewing.get();
	}

	private void startPreview(Object observer) {
		Camera cam = mCam;
		if (cam == null) {
			return;
		}

		try {
			if (observer instanceof SurfaceTexture) {
				cam.setPreviewTexture((SurfaceTexture) observer);
			} else {
				cam.setPreviewDisplay((SurfaceHolder) observer);
			}

			switch (mMode) {
			case BUFFER:
				mBuffer = new byte[mWidth * mHeight * 3 / 2];
				cam.addCallbackBuffer(mBuffer);
				cam.setPreviewCallbackWithBuffer(this);
				break;
			case NORMAL:
				cam.setPreviewCallback(this);
				break;

			default:
				break;
			}
			
			cam.startPreview();
			mIsPreviewing.compareAndSet(false, true);
			mCam = cam;
			LogLib.d("startPreview");
		} catch (IOException e) {
			LogLib.w("IOException", e);
		}
	}

	protected void startPreview(SurfaceTexture surfaceTexture) {
		startPreview((Object) surfaceTexture);
	}

	protected void startPreview(SurfaceHolder surfaceHolder) {
		startPreview((Object) surfaceHolder);
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

	protected enum FRAME_CALL_MODE {
		NORMAL, BUFFER, NONE
	}
}
