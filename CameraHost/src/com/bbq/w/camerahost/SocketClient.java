package com.bbq.w.camerahost;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.bbq.w.library.LogLib;
import com.bbq.w.library.MalProtocolException;
import com.bbq.w.library.ProtocolLib;
import com.bbq.w.library.ServerConstants;
import com.bbq.w.library.ThreadUtils;

public class SocketClient {

	private final static int INVALID_VALUE = -1;
	
	private Socket mSocket;
	private String mHostAddress;
	private int mPort;
	private int mByteSize;

	public void setPort(int port) {
		this.mPort = port;
	}

	public void setHostAddress(String hostAddress) {
		this.mHostAddress = hostAddress;
	}

	public boolean buildConnect() {
		boolean shouldAbort = false;
		Socket socket = null;
		try {
			socket = new Socket(mHostAddress, mPort);
			ProtocolLib.runConnectProtocol(socket,
					ServerConstants.IDENTIFY_HOST);
			mByteSize = INVALID_VALUE;
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
		return !shouldAbort;
	}

	public void disconnect() {
		LogLib.d("disconnect.");
		Socket socket = mSocket;
		mByteSize = INVALID_VALUE;
		if (socket == null) {
			return;
		}

		try {
			socket.close();
			LogLib.d("disconnect ok.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LogLib.w(e);
		}
	}
	
	private void writeBufferSize(Socket socket, int size) throws IOException {
		DataOutputStream ds = new DataOutputStream(socket.getOutputStream());
		ds.writeInt(size);
		ds.flush();
	}

	public void flushDataByte(byte[] data) {
		Socket socket = mSocket;
		if (socket == null || !socket.isConnected()) {
			return;
		}
		
		boolean error = false;
		try {
			if (mByteSize == INVALID_VALUE) {
				mByteSize = data.length;
				writeBufferSize(socket, mByteSize);
			}

			OutputStream os = socket.getOutputStream();
			os.write(data);
			os.flush();
		} catch (IOException e) {
			LogLib.w(e);
			error = true;
		} finally {
			if (error) {
				try {
					socket.close();
				} catch (IOException e) {
					LogLib.w(e);
				}
				mSocket = null;
			}
		}
	}
}
