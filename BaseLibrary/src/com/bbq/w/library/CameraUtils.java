package com.bbq.w.library;

import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.view.Surface;
import android.view.WindowManager;

@SuppressWarnings("deprecation")
public class CameraUtils {

	private final static String TAG = "CameraUtils";
	private final static boolean DEBUG = LibConstants.DEBUG;

	private static int getDisplayRotation(Context ctx) {
		WindowManager wm = (WindowManager) ctx
				.getSystemService(Context.WINDOW_SERVICE);

		int rotation;

		switch (wm.getDefaultDisplay().getOrientation()) {

		case Surface.ROTATION_90:
			rotation = 90;
			break;

		case Surface.ROTATION_180:
			rotation = 180;
			break;

		case Surface.ROTATION_270:
			rotation = 270;
			break;

		default:
			rotation = 0;
			break;
		}
		if (DEBUG) {
			LogLib.d(TAG, "getDisplayRotation : " + rotation);
		}
		return rotation;
	}

	public static void fixCameraDegree(Camera cam, int cameraId, Context ctx) {
		int rotation = getDisplayRotation(ctx);
		CameraInfo cameraInfo = new CameraInfo();
		Camera.getCameraInfo(cameraId, cameraInfo);
		int result;
		if (CameraInfo.CAMERA_FACING_FRONT == cameraInfo.facing) {
			result = (cameraInfo.orientation + rotation) % 360;
			result = (360 - result) % 360;
		} else {
			result = (cameraInfo.orientation - rotation + 360) % 360;
		}
		cam.setDisplayOrientation(result);

		if (DEBUG) {
			LogLib.d(TAG, "fixCameraDegree : " + result + "; cameraInfo="
					+ cameraInfo.orientation);
		}
	}

	public static void setPreviewSize(Camera cam, int width, int height) {
		Parameters parameters = cam.getParameters();
		List<Size> sizes = parameters.getSupportedPreviewSizes();

		float ratio = (float) width / height;
		boolean isCompared = false;
		for (Size size : sizes) {
			if (DEBUG) {
				LogLib.d(TAG, size.width + ";" + size.height);
				LogLib.d(TAG, "refR = " + (float) size.width / size.height);
			}
			if (ratio == (float) size.width / size.height) {
				isCompared = true;
				break;
			}
		}

		if (isCompared) {
			parameters.setPreviewSize(width, height);
			cam.setParameters(parameters);
		}
		if (DEBUG) {
			LogLib.d(TAG, "isCompared = " + isCompared + "; ratio=" + ratio);
		}
	}

	public static int[] getCameraIds() {
		int count = Camera.getNumberOfCameras();
		int[] idArray = new int[count];
		for (int i = 0; i < count; i++) {
			idArray[i] = i;
		}
		return idArray;
	}
}
