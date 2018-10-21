package hu.divecity;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;

import java.util.ArrayList;

public class SocketHandler {
	Configuration configuration = new Configuration();
	private ArrayList<SocketIOClient> clients;

	public SocketHandler() {
		configuration.setHostname("localhost");
		configuration.setPort(3000);
		final SocketIOServer server = new SocketIOServer(configuration);

		server.addConnectListener(socketIOClient -> clients.add(socketIOClient));
		server.addDisconnectListener(socketIOClient -> clients.remove(socketIOClient));

		SocketIONamespace smarthome = server.addNamespace("smarthome");
	}

	public void SendToClient(SocketIOClient client, Integer deviceID, ActionID actionID, String detail) {
		String json = String.format("{\"target\" : %d, \"action\": %d, \"detail\": \"%s\"}", deviceID, actionID.ordinal(), detail);
		client.sendEvent("enentChange", json);
	}
}
