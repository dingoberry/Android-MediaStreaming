package com.bbq.w.camerahost;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.bbq.w.library.LogLib;

public class MainActivity extends Activity implements OnClickListener {

	private View mConnect;
	private SocketClient mCilent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mConnect = findViewById(R.id.base_connect);
		mConnect.setOnClickListener(this);
		mCilent = new SocketClient();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == mConnect) {
			openConnection();
		}
	}

	private void openConnection() {
		// TODO Auto-generated method stub
		new Thread(){
			public void run() {
				try {
					EditText editText = (EditText) findViewById(R.id.base_ip);
					String ip = editText.getText().toString();
					editText = (EditText) findViewById(R.id.base_port);
					int port = Integer.parseInt(editText.getText().toString());
					mCilent.setPort(port);
					mCilent.setHostAddress(ip);
					mCilent.buildConnect();
				} catch (NumberFormatException e) {
					LogLib.w(e);
				}
			};
		}.start();
	}
}
