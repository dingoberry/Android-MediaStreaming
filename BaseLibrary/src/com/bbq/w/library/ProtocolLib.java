package com.bbq.w.library;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ProtocolLib {

	public static void runConnectProtocol(Socket socket, int identifyType)
			throws IOException, MalProtocolException {
		int req = socket.getInputStream().read();
		if (req != ServerConstants.REQ_IDENTITY) {
			throw new MalProtocolException("bad req result :" + req + ", with right:" + ServerConstants.REQ_IDENTITY);
		}

		OutputStream outputStream = socket.getOutputStream();
		outputStream.write(identifyType);
		outputStream.flush();
		
		req = socket.getInputStream().read();
		if (req != ServerConstants.REQ_RECOGNIZED) {
			throw new MalProtocolException("bad req result :" + req + ", with right:" + ServerConstants.REQ_RECOGNIZED);
		}
	}
}
