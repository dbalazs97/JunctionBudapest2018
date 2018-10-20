package hu.divecity;

import java.util.ArrayList;
import com.corundumstudio.socketio.*;

public class SocketHandler {
	Configuration configuration = new Configuration();
	private ArrayList<SocketIOClient> clients;

	public SocketHandler(Configuration configuration) {
		configuration.setHostname("localhost");
		configuration.setPort(3000);
		final SocketIOServer server = new SocketIOServer(configuration);

		server.addConnectListener(socketIOClient -> clients.add(socketIOClient));
		server.addDisconnectListener(socketIOClient -> clients.remove(socketIOClient));

		SocketIONamespace smarthome = server.addNamespace("smarthome");
	}
}
