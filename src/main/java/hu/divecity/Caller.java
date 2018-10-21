package hu.divecity;

import org.java_websocket.WebSocket;

public class Caller {
	String phoneNumber = "";
	String correlator = "";

	ActionID action;

	WebSocket socket;

	public Caller(String phoneNumber, String correlator, ActionID actionID) {
		this.phoneNumber = phoneNumber;
		this.correlator = correlator;
		this.action = actionID;
	}
}
