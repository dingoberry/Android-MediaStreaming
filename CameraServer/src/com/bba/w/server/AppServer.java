package com.bba.w.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppServer implements Runnable {

	private final static int REQ_IDENTIFY = 0x1;
	private final static int REQ_RECOGNIZED = 0x2;

	private final static int IDENTIFY_HOST = 0x10;
	private final static int IDENTIFY_AUDIENCE = 0x20;

	private ServerSocket mServer;
	private Socket mHostClient;
	private List<Socket> mAudienceClients = new ArrayList<>();

	public void start() {
		try {
			ServerSocket server = new ServerSocket(0);
			System.out.println("binder port:" + server.getLocalPort());
			mServer = server;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		while (true) {
			boolean isNoUse = false;
			Socket socket = null;
			try {
				System.out.println("Wait for connect");
				socket = mServer.accept();
				System.out.println("Client connected");
				OutputStream outputStream = socket.getOutputStream();
				outputStream.write(REQ_IDENTIFY);
				outputStream.flush();

				switch (socket.getInputStream().read()) {
				case IDENTIFY_HOST:
					System.out.println("Connect type host.");
					if (mHostClient == null) {
						mHostClient = socket;
						outputStream.write(REQ_RECOGNIZED);
						outputStream.flush();
						new Thread(this).start();
					} else {
						System.out.println("host is active");
						isNoUse = true;
					}
					break;

				case IDENTIFY_AUDIENCE:
					System.out.println("Connect type audience.");
					mAudienceClients.add(socket);
					outputStream.write(REQ_RECOGNIZED);
					outputStream.flush();
					break;
				default:
					break;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				isNoUse = true;
			} finally {
				if (isNoUse && socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Socket hostClient = mHostClient;
		if (hostClient == null) {
			return;
		}
		
		try {
			DataInputStream inputStream = new DataInputStream(hostClient.getInputStream());
			int size = inputStream.readInt();
			byte[] buffer = new byte[size];
			int length;

			while((length = inputStream.read(buffer)) != -1) {
				send(Arrays.copyOf(buffer, length));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				hostClient.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void send(byte[] data) {
		// TODO Auto-generated method stub
		System.out.println("send :" + data.length);
	}
}
