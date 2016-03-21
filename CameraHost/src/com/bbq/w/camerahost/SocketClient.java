package com.bbq.w.camerahost;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.bbq.w.library.LogLib;
import com.bbq.w.library.MalProtocolException;
import com.bbq.w.library.ProtocolLib;
import com.bbq.w.library.ServerConstants;

public class SocketClient {

	private Socket mSocket;
	private String mHostAddress;
	private int mPort;

	public void setPort(int port) {
		this.mPort = port;
	}

	public void setHostAddress(String hostAddress) {
		this.mHostAddress = hostAddress;
	}

	public void buildConnect() {
		boolean shouldAbort = false;
		Socket socket = null;
		try {
			socket = new Socket(mHostAddress, mPort);
			ProtocolLib.runConnectProtocol(socket,
					ServerConstants.IDENTIFY_HOST);
			mSocket = socket;
			LogLib.d("connect successfully.");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			LogLib.w(e);
			shouldAbort = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LogLib.w(e);
			shouldAbort = true;
		} catch (MalProtocolException e) {
			// TODO Auto-generated catch block
			LogLib.w(e);
			shouldAbort = true;
		} finally {
			if (shouldAbort && socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LogLib.w(e);
				}
				mSocket = null;
			}
		}
	}

	public void flushDataByte(byte[] data) {
		Socket socket = mSocket;
		if (socket == null || !socket.isConnected()) {
			return;
		}

		boolean error = false;
		try {
			OutputStream os = socket.getOutputStream();
			os.write(data);
			os.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LogLib.w(e);
			error = true;
		} finally {
			if (error) {
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mSocket = null;
			}
		}
	}

	public void writeDataSize() {

	}

}
