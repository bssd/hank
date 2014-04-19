package uk.co.bssd.hank.websocket.server.time;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.bssd.hank.websocket.server.SubscriptionEndpoint;
import uk.co.bssd.hank.websocket.server.WebSocketServer;

public class Server {

	private static final boolean USER_THREAD = true;

	public static void main(String[] args) {
		final SubscriptionEndpoint endpoint = new SubscriptionEndpoint();
		WebSocketServer server = WebSocketServer.aServer().withPort(9001)
				.addEndpoint(endpoint).build();
		server.start();

		scheduleTimeBroadcast(endpoint);

		System.out.println("TimeServer started, press enter to stop...");
		waitForEnter();
	}

	private static void scheduleTimeBroadcast(
			final SubscriptionEndpoint endpoint) {
		Timer timer = new Timer(USER_THREAD);
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				String currentTime = new Date().toString();
				endpoint.broadcast("time", currentTime);
			}
		}, 1000, 1000);
	}

	private static void waitForEnter() {
		try {
			System.in.read();
		}
		catch (IOException e) {
		}
	}
}