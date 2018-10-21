package hu.divecity;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import static hu.divecity.Main.*;

public class SocketHandler extends WebSocketServer {

	public SocketHandler(int address) {
		super(new InetSocketAddress(address));
	}

	@Override
	public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
		try {
			printSocket(webSocket, " connected.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void printSocket(WebSocket s, String s2) {
		System.out.println(s.getRemoteSocketAddress().toString() + s2);
	}

	@Override
	public void onClose(WebSocket webSocket, int code, String reason, boolean remote) {
		try {
			printSocket(webSocket, " disconnected.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(WebSocket webSocket, String message) {
		try {
			if (message.equals(caller16.phoneNumber))
				caller16.socket = webSocket;
			else
				caller17.socket = webSocket;
			printSocket(webSocket, " sent: " + message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void handleCall(Caller caller, Integer target, String detail) {
		System.out.println("WEBSOCKET RESPONSE TO "+caller.phoneNumber+": {\"target\": " + target.toString() + ", \"action\": " + caller.action.ordinal() + ", \"detail\": \"" + detail + "\"}");
		((caller.equals(caller16)) ? caller16.socket : caller17.socket).send("{\"target\": " + target.toString() + ", \"action\": " + caller.action.ordinal() + ", \"detail\": \"" + detail + "\"}");
	}

	@Override
	public void onError(WebSocket webSocket, Exception e) {
		try {
			if (e != null && webSocket != null)
				printSocket(webSocket, " got an error " + e.toString());
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	@Override
	public void onStart() {
		System.out.println("Websocket started opened at " + this.getAddress().toString());
	}

	public void close() {
		caller16.socket.close();
		caller17.socket.close();
	}
}
