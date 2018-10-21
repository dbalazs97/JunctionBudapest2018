package hu.divecity;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;

public class SocketHandler extends WebSocketServer {
	private ArrayList<WebSocket> clients = new ArrayList<>();
	private HashMap<String, WebSocket> map = new HashMap<>();

	public SocketHandler(int address) {
		super(new InetSocketAddress(address));
	}

	@Override
	public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
		try {
			clients.add(webSocket);
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
			clients.remove(webSocket);
			printSocket(webSocket, " disconnected.");
			webSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(WebSocket webSocket, String message) {
		try {
			printSocket(webSocket, " sent: " + message);
			map.put(message, webSocket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void handleCall(String webSocket, Integer target, ActionID action, String detail) {
		map.get(webSocket).send("{\"target\": " + target.toString() + ", \"action\": " + action.ordinal() + ", \"detail\": \"" + detail + "\"}");
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
		for (WebSocket client : clients) {
			client.close();
		}
	}
}
