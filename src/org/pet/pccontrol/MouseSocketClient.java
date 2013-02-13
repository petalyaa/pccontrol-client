package org.pet.pccontrol;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

public class MouseSocketClient {
	
	private static final String TAG = "Client";

	public static enum ButtonAction {
		LEFT_CLICK, RIGHT_CLICK, DOUBLE_CLICK_LEFT
	};

	private String host;

	private int port;
	
	private int timeout;

	private Socket socket;

	private DataOutputStream outputStream;

	public MouseSocketClient(String host, int port, int timeout) {
		this.host = host;
		this.port = port;
		this.timeout = timeout;
	}

	public void connect() throws UnknownHostException, IOException {
		InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
		socket = new Socket();
		socket.connect(inetSocketAddress, timeout);
		outputStream = new DataOutputStream(socket.getOutputStream());
	}

	public void moveMouse(MouseCoordinate coordinate) throws IOException {
		if (outputStream != null) {
			outputStream.writeBytes(coordinate.getX() + "," + coordinate.getY());
			outputStream.writeBytes("\n");
		}
	}

	public void buttonClick(ButtonAction button) throws IOException {
		String action = null;
		switch (button) {
		case DOUBLE_CLICK_LEFT:
			action = "3";
			break;
		case LEFT_CLICK:
			action = "1";
			break;
		case RIGHT_CLICK:
			action = "2";
			break;
		}
		if (outputStream != null) {
			outputStream.writeBytes("button=" + action);
			outputStream.writeBytes("\n");
		}
	}

	public void disconnect() throws IOException {
		if (outputStream != null)
			outputStream.close();
		if (socket != null)
			socket.close();
	}

}
