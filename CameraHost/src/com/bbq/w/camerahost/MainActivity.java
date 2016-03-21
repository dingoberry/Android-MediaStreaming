package com.bbq.w.camerahost;

import java.io.IOException;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.bbq.w.library.LogLib;
import com.bbq.w.library.SpUtils;
import com.bbq.w.library.ThreadUtils;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity implements OnClickListener,
		Runnable, PreviewCallback {

	private final static String IP_RESERVER = "ip_reserver";
	private final static String PORT_RESERVER = "port_reserver";
	private final static int INVALID_VALUE = -1;

	private View mConnect;
	private SocketClient mCilent;

	private EditText mIp;
	private EditText mPort;
	private SurfaceView mSv;

	private Camera mCamera;
	private SurfaceHolder mHolder;

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
	public void onClick(View v) {
		if (v == mConnect) {
			openConnection();
		}
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
		if (camera == null) {
			camera = Camera.open();
			mCamera = camera;
		}

		try {
			int degrees;
			switch (getWindowManager().getDefaultDisplay().getOrientation()) {
			case Surface.ROTATION_90:
				degrees = 90;
				break;

			case Surface.ROTATION_180:
				degrees = 180;
				break;

			case Surface.ROTATION_270:
				degrees = 270;
				break;

			default:
				degrees = 0;
				break;
			}
			CameraInfo cameraInfo = new CameraInfo();
			Camera.getCameraInfo(CameraInfo.CAMERA_FACING_BACK, cameraInfo);
			degrees = (cameraInfo.orientation - degrees + 360) % 360;
			camera.setDisplayOrientation(degrees);
			camera.setPreviewCallback(this);
			Parameters parameters = camera.getParameters();
			camera.setPreviewDisplay(mHolder);
			LogLib.d("parm=" + parameters.getSupportedPreviewFormats());
			camera.startPreview();
		} catch (IOException e) {
			LogLib.w(e);
			mCamera = null;
		}
	}

	@Override
	public void run() {
		mSv.setVisibility(View.VISIBLE);
		mConnect.setVisibility(View.GONE);

		ThreadUtils.runAloneThread(new Runnable() {
			@Override
			public void run() {
				openCamera();
			}
		});
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {

	}
}
