package hu.divecity;

import com.corundumstudio.socketio.SocketIOClient;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Objects;

import static spark.Spark.*;

public class Main {

	private static final String API_KEY = "5a8b14c1a353b4000197972f863d73874d4d4ffdbf3387b88a834439";
	private static final String PHONE = "sip:+358480786517@ims8.wirelessfuture.com";
	private static final String OTHERPHONE = "sip:+358480786516@ims8.wirelessfuture.com";
	private static final String SERVER = "http://692b8ba4.ngrok.io";
	private static SocketHandler socketHandler = new SocketHandler();
	private static final HashMap<String, SocketIOClient> phoneToClient = new HashMap<>();

	public static void main(String[] args) {

		Runtime.getRuntime().addShutdownHook(new Thread(Main::UnSubsrcribeToNokia));
		SubsrcribeToNokia();
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

			post("/", (req, res) -> {
				res.header("content-type", "application/json");
				res.header("authorization", "5a8b14c1a353b4000197972f863d73874d4d4ffdbf3387b88a834439");
				res.body(RequestDialedNumbers());
				return "";//RequestDialedNumbers();
			});

			post("/dial", (req, res) -> {
				System.out.println("Dialed numbers: ");
				System.out.println(req.body());
				return "{\n" +
						"   \"action\": {\n" +
						"      \"actionToPerform\": \"Continue\",\n" +
						"      \"displayAddress\": \""+OTHERPHONE+"\"\n" +
						"   }\n" +
						"}";
			});

			get("/please.wav", (request, response) -> {
				//response.header("Content-Type", "audio/wav");
				byte[] bytes = null;
				try {
					String a = Objects.requireNonNull(Main.class.getClassLoader().getResource("please.wav")).getFile();
					File path = new File(a);
					bytes = Files.readAllBytes(path.toPath());
				} catch (Exception e) {
					e.printStackTrace();
				}
					return bytes;
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void SubsrcribeToNokia() {
		String json = "{\n" +
				"  \"callDirectionSubscription\": {\n" +
				"    \"callbackReference\": {\n" +
				"      \"notifyURL\": \"" + SERVER + "\"\n" +
				"    },\n" +
				"    \"filter\": {\n" +
				"      \"address\": [\n" +
				"        \"" + PHONE + "\"\n" +
				"      ],\n" +
				"      \"criteria\": [\n" +
				"        \"CalledNumber\"\n" +
				"      ],\n" +
				"      \"addressDirection\": \"Called\"\n" +
				"    },\n" +
				"    \"clientCorrelator\": \"cc12345\"\n" +
				"  }\n" +
				"}";

		try {
			HttpResponse<JsonNode> js = Unirest.post("https://mn.developer.nokia.com/tasseeAPI/callnotification/v1/subscriptions/callDirection")
					.header("content-type", "application/json")
					.header("authorization", "5a8b14c1a353b4000197972f863d73874d4d4ffdbf3387b88a834439")
					.body(json)
					.asJson();

			if (js.getStatus() == 200) {
				//System.out.println(js.getBody().toString());
				System.out.println("Successfully subscribed to Nokia TAS.");
			} else {
				System.out.println("There was an error while subscribing with code " + js.getStatus());
				//System.exit(100);
			}
		} catch (UnirestException e) {
			e.printStackTrace();
		}

	}

	private static void UnSubsrcribeToNokia() {
		Unirest
				.delete("https://mn.developer.nokia.com/tasseeAPI/callnotification/v1/subscriptions/callDirection/subs?Id=cc12345&addr=sip%3A%2B358480786517%40ims8.wirelessfuture.com")
				.header("authorization", "5a8b14c1a353b4000197972f863d73874d4d4ffdbf3387b88a834439");
		System.out.println("Successfully unsubscribed to Nokia TAS.");
	}

	private static String RequestDialedNumbers() {
		String url = SERVER+"/please.wav";
		//String url = "http://10.95.86.118/?target=" + encoded;
		return "{" +
				"   \"action\": {" +
				"      \"actionToPerform\": \"Continue\"," +
				"      \"displayAddress\": \"" + OTHERPHONE + "\"," +
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
				"            \"" + OTHERPHONE + "\"" +
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
