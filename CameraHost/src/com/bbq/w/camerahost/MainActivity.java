package com.bbq.w.camerahost;

import java.io.IOException;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.bbq.w.library.CameraUtils;
import com.bbq.w.library.LogLib;
import com.bbq.w.library.SpUtils;
import com.bbq.w.library.ThreadUtils;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity implements OnClickListener,
		Runnable, PreviewCallback {

	private final static String IP_RESERVER = "ip_reserver";
	private final static String PORT_RESERVER = "port_reserver";
	private final static int INVALID_VALUE = -1;
	private final static int CAMERA_HEIGHT = 600;
	private final static int CAMERA_WIDTH = 800;

	private View mConnect;
	private SocketClient mCilent;

	private EditText mIp;
	private EditText mPort;
	private View mContainer;
	private View mSwitcher;
	private SurfaceView mSv;

	private Camera mCamera;
	private SurfaceHolder mHolder;

	private int[] mCameraIds;
	private int mIdIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mConnect = findViewById(R.id.base_connect);
		mConnect.setOnClickListener(this);
		mCilent = new SocketClient();

		mIp = (EditText) findViewById(R.id.base_ip);
		mPort = (EditText) findViewById(R.id.base_port);
		mSv = (SurfaceView) findViewById(R.id.main_sv);
		mContainer = findViewById(R.id.main_holder);
		mSwitcher = findViewById(R.id.main_switcher);
		mSwitcher.setOnClickListener(this);
		mHolder = mSv.getHolder();

		String ip = SpUtils.getString(this, IP_RESERVER);
		if (!TextUtils.isEmpty(ip)) {
			mIp.setText(ip);
		}
		int port = SpUtils.getInt(this, PORT_RESERVER, INVALID_VALUE);
		if (port != INVALID_VALUE) {
			mPort.setText(String.valueOf(port));
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mCilent.disconnect();
	}

	@Override
	public void onClick(View v) {
		if (v == mConnect) {
			openConnection();
		} else if (v == mSwitcher) {
			switchCamera();
		}
	}

	private void switchCamera() {
		Camera camera = mCamera;
		if (camera != null) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			mCamera = null;
			LogLib.d("switching, release camera.");
		}

		ThreadUtils.runAloneThread(new Runnable() {

			@Override
			public void run() {
				mIdIndex = ((mIdIndex + 1) % mCameraIds.length);
				openCamera();
			}
		});
	}

	private void openConnection() {
		ThreadUtils.runAloneThread(new Runnable() {
			@Override
			public void run() {
				try {
					String ip = mIp.getText().toString();
					int port = Integer.parseInt(mPort.getText().toString());
					mCilent.setPort(port);
					mCilent.setHostAddress(ip);
					boolean state = mCilent.buildConnect();
					SpUtils.saveString(MainActivity.this, IP_RESERVER, ip);
					SpUtils.saveInt(MainActivity.this, PORT_RESERVER, port);

					LogLib.d("Connect state: " + state);
					if (state == true) {
						mIp.post(MainActivity.this);
					}
				} catch (NumberFormatException e) {
					LogLib.w(e);
				}
			}
		});
	}

	private void openCamera() {
		Camera camera = mCamera;
		int cameraId = mCameraIds[mIdIndex];
		if (camera == null) {
			LogLib.d("open new camera");
			camera = Camera.open(cameraId);
			mCamera = camera;
		}

		try {
			CameraUtils.fixCameraDegree(camera, cameraId, this);
			CameraUtils.setPreviewSize(camera, CAMERA_WIDTH, CAMERA_HEIGHT);
			camera.setPreviewCallback(this);
			camera.setPreviewDisplay(mHolder);
			camera.startPreview();
		} catch (IOException e) {
			LogLib.w(e);
			mCamera = null;
		}
	}

	@Override
	public void run() {
		mContainer.setVisibility(View.VISIBLE);
		mConnect.setVisibility(View.GONE);
		int[] cameraIds = CameraUtils.getCameraIds();
		if (cameraIds == null || cameraIds.length == 0) {
			LogLib.d("not suppored camera.");
			return;
		}
		mCameraIds = cameraIds;
		mIdIndex = 0;

		ThreadUtils.runAloneThread(new Runnable() {
			@Override
			public void run() {
				openCamera();
			}
		});
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		mCilent.flushDataByte(data);
	}
}
