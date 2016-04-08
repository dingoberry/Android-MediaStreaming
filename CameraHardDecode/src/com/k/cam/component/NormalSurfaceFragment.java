package com.k.cam.component;

import com.bbq.w.library.LogLib;
import com.k.cam.Configuration;
import com.k.cam.MediaHardDecoder;
import com.k.cam.MediaHardEncoder;
import com.k.cam.MediaHardEncoder.EncodeDataReceiver;
import com.k.cam.R;

import android.hardware.Camera;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

@SuppressWarnings("deprecation")
public class NormalSurfaceFragment extends CameraFragment implements Callback, EncodeDataReceiver {

	private final static String TAG = "NormalSurfaceFragment";

	private static final boolean DEBUG = Configuration.DEBUG;

//	private final static int CAMERA_HEIGHT = 600;
//	private final static int CAMERA_WIDTH = 800;
	
	private final static int CAMERA_HEIGHT = 720;
	private final static int CAMERA_WIDTH = 1280;

	private SurfaceView mSv;
	private SurfaceHolder mH;
	
	private MediaHardEncoder mEncoder;
	private MediaHardDecoder mDecoder;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFrameMode(FRAME_CALL_MODE.BUFFER);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View content = inflater.inflate(R.layout.normal_surface_fragment,
				container, false);
		mSv = (SurfaceView) content.findViewById(R.id.sv);
		SurfaceHolder holder = mSv.getHolder();
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		holder.addCallback(this);
		mH = holder;
		return content;
	}

	@Override
	public void onFrameArrival(byte[] data, Camera camera) {
		MediaHardEncoder encoder = mEncoder;
		if (encoder != null) {
			encoder.feedData(data);
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		int id = resolveCameraId();
		if (DEBUG) {
			LogLib.d(TAG, "surfaceCreated:" + id);
		}
		if (INVALID_VALUE != id) {
			openCam(id, CAMERA_WIDTH, CAMERA_HEIGHT);
			
			if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
				MediaHardEncoder encoder = new MediaHardEncoder();
				encoder.setEncodeDataReceiver(this);
				if (encoder.startEncode(CAMERA_WIDTH, CAMERA_HEIGHT)) {
					mEncoder = encoder;
					
					
					MediaHardDecoder decoder = new MediaHardDecoder();
					if (decoder.startDecode(CAMERA_WIDTH, CAMERA_HEIGHT, encoder.getColorFormat())) {
						mDecoder = decoder;
					}
				}
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if (DEBUG) {
			LogLib.d(TAG, "surfaceChanged");
		}
		if (isPreviewing()) {
			return;
		}
		startPreview(mH);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (DEBUG) {
			LogLib.d(TAG, "surfaceDestroyed");
		}
		releaseCam();

		MediaHardEncoder encoder = mEncoder;
		if (encoder != null) {
			encoder.stopEncode();
		}
		
		MediaHardDecoder decoder = mDecoder;
		if (decoder != null) {
			decoder.stopDecode();
		}
	}

	@Override
	public void receiveData(byte[] data) {
		LogLib.d(TAG, "encode size=" + data.length);
		
		MediaHardDecoder decoder = mDecoder;
		if (decoder != null) {
			decoder.enqueueData(data);
		}
	}
}
