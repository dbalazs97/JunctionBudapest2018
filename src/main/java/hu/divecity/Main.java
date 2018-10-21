package hu.divecity;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.UUID;

import static spark.Spark.*;

public class Main {
	// from 16, to 17

	private static final String API_KEY = "5a8b14c1a353b4000197972f863d73874d4d4ffdbf3387b88a834439";
	public static final String PHONE17 = "sip:+358480786517@ims8.wirelessfuture.com";
	private static final String PHONE16 = "sip:+358480786516@ims8.wirelessfuture.com";
	private static final String SERVER = "http://943ede25.ngrok.io";
	private static SocketHandler socketHandler;
	private static String correlator;

	public static final Caller caller16 = new Caller(PHONE16, UUID.randomUUID().toString(), ActionID.ACTION_ON);
	public static final Caller caller17 = new Caller(PHONE17, UUID.randomUUID().toString(), ActionID.ACTION_OFF);

	private static final HashMap<String, Integer> phoneToClient = new HashMap<>();

	public static void main(String[] args) {
		CloseConnections(false, caller16);
		CloseConnections(false, caller17);

		correlator = String.valueOf(UUID.randomUUID());
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			Main.CloseConnections(true, caller16);
			Main.CloseConnections(true, caller17);
		}));
		try {
			socketHandler = new SocketHandler(4321);
			socketHandler.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

		SubsrcribeToNokia(caller16);
		SubsrcribeToNokia(caller17);

		try {
			port(1234);
			before("/*", (request, response) -> {
				try {
					System.out.println("REQUEST FROM: " + request.url() + "::" + request.requestMethod() + "\n[" + request.body() + "]\n[" + response.body() + "]\n");
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			get("/", (request, response) -> "Hello");
			post("/dial", (request, response) -> "");

			post("/", (req, res) -> {
				res.header("content-type", "application/json");
				res.header("authorization", "5a8b14c1a353b4000197972f863d73874d4d4ffdbf3387b88a834439");

				JSONObject jso = new JSONObject(req.body());

				Caller caller = (jso.getJSONObject("callEventNotification").getString("callingParticipant").equals(caller16.phoneNumber))? caller16 : caller17;

				socketHandler.handleCall(caller, 0, "");
				return RequestDialedNumbers(caller);
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void SubsrcribeToNokia(Caller who) {
		String json = "{\n" +
				"  \"callDirectionSubscription\": {\n" +
				"    \"callbackReference\": {\n" +
				"      \"notifyURL\": \"" + SERVER + "\"\n" +
				"    },\n" +
				"    \"filter\": {\n" +
				"      \"address\": [\n" +
				"        \"" + who.phoneNumber + "\"\n" +
				"      ],\n" +
				"      \"criteria\": [\n" +
				"        \"CalledNumber\"\n" +
				"      ],\n" +
				"      \"addressDirection\": \"Called\"\n" +
				"    },\n" +
				"    \"clientCorrelator\": \"" + who.correlator + "\"\n" +
				"  }\n" +
				"}";

		try {
			HttpResponse<JsonNode> js = Unirest.post("https://mn.developer.nokia.com/tasseeAPI/callnotification/v1/subscriptions/callDirection")
					.header("content-type", "application/json")
					.header("authorization", "5a8b14c1a353b4000197972f863d73874d4d4ffdbf3387b88a834439")
					.body(json)
					.asJson();

			if (js.getStatus() == 200 || js.getStatus() == 201) {
				//System.out.println(js.getBody().toString());
				System.out.println("Successfully subscribed to Nokia TAS.");
			} else {
				System.out.println("There was an error while subscribing with code " + js.getStatus() + " - " + js.getBody());
				//System.exit(100);
			}
		} catch (UnirestException e) {
			e.printStackTrace();
		}

	}

	private static void CloseConnections(Boolean deleteSocket, Caller who) {
		Unirest
				.delete("https://mn.developer.nokia.com/tasseeAPI/callnotification/v1/subscriptions/callDirection/subs?Id=" + who.correlator + "&addr=" + URLEncoder.encode(who.phoneNumber))
				.header("authorization", "5a8b14c1a353b4000197972f863d73874d4d4ffdbf3387b88a834439");
		System.out.println("Successfully unsubscribed to Nokia TAS.");
		if (deleteSocket)
			socketHandler.close();
	}

	private static String RequestDialedNumbers(Caller who) {
		String url = SERVER + "/please.wav";
		//String url = "http://10.95.86.118/?target=" + encoded;
		return "{" +
				"   \"action\": {" +
				"      \"actionToPerform\": \"Continue\"," +
				"      \"digitCapture\": {" +
				"         \"digitConfiguration\": {" +
				"            \"maxDigits\": 10," +
				"            \"minDigits\": 3," +
				"            \"endChar\": \"#\"" +
				"         }," +
				"         \"playingConfiguration\": {" +
				"            \"playFileLocation\": \"" + url + "\"" +
				"         }," +
				"         \"callParticipant\": [" +
				"            \"" + who.phoneNumber + "\"" +
				"         ]" +
				"      }," +
				"      \"playAndCollectInteractionSubscription\": {" +
				"         \"callbackReference\": {" +
				"            \"notifyURL\": \"" + SERVER + "/dial\"" +
				"         }" +
				"      }" +
				"   }" +
				"}";
	}
}
