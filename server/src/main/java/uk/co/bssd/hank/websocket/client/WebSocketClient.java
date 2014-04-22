package uk.co.bssd.hank.websocket.client;

import static uk.co.bssd.hank.datetime.TimeMeasure.seconds;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

import uk.co.bssd.hank.datetime.TimeMeasure;
import uk.co.bssd.hank.websocket.dto.SubscriptionRequest;

import com.google.gson.Gson;

public class WebSocketClient {

	public static final String DEFAULT_HOST = "localhost";
	public static final int DEFAULT_PORT = 80;
	public static final String DEFAULT_CONTEXT = "websocket";

	public static WebSocketClientBuilder aClient() {
		return new WebSocketClientBuilder().withHost(DEFAULT_HOST)
				.withPort(DEFAULT_PORT).withContext(DEFAULT_CONTEXT);
	}

	public static class WebSocketClientBuilder {

		private String host;
		private int port;
		private String context;
		private String endpoint;

		protected WebSocketClientBuilder() {
			super();
		}

		public WebSocketClientBuilder withHost(String host) {
			this.host = host;
			return this;
		}

		public WebSocketClientBuilder withPort(int port) {
			this.port = port;
			return this;
		}

		public WebSocketClientBuilder withContext(String context) {
			this.context = context;
			return this;
		}

		public WebSocketClientBuilder withEndpoint(String endpoint) {
			this.endpoint = endpoint;
			return this;
		}

		public WebSocketClient build() {
			return new WebSocketClient(this.host, this.port, this.context,
					this.endpoint);
		}
	}

	private static final TimeMeasure DEFAULT_TIMEOUT_RECEIVE = seconds(10);
	
	private final URI uri;
	private final BlockingQueue<String> messagesReceived;
	
	private Session session;
	
	private TimeMeasure timeoutReceive = DEFAULT_TIMEOUT_RECEIVE; 
	
	private WebSocketClient(String hostName, int port, String contextPath,
			String endpoint) {
		this.uri = uri(hostName, port, contextPath, endpoint);
		this.messagesReceived = new LinkedBlockingQueue<String>();
	}

	private URI uri(String hostName, int port, String contextPath,
			String endpoint) {
		try {
			return new URI(String.format("ws://%s:%d/%s/%s", hostName, port,
					contextPath, endpoint));
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public void connect(TimeMeasure timeout) {
		ConnectFuture future = new ConnectFuture();
		initiateConnection(future);
		this.session = future.await(timeout);
		this.session.addMessageHandler(new MessageHandler.Whole<String>() {
			public void onMessage(String message) {
				messagesReceived.add(message);
			}
		});
	}

	private void initiateConnection(ConnectFuture future) {
		try {
			client().connectToServer(future, config(), this.uri);
		} catch (Exception e) {
			throw new WebSocketClientException("Unable to connect to server", e);
		}
	}
	
	public void subscribe(String key) {
		SubscriptionRequest request = SubscriptionRequest.subscribe(key);
		Gson gson = new Gson();
		String json = gson.toJson(request);
		send(json);
	}
	
	public void unsubscribe(String key) {
		SubscriptionRequest request = SubscriptionRequest.unsubscribe(key);
		Gson gson = new Gson();
		String json = gson.toJson(request);
		send(json);
	}

	public String send(String message) {
		try {
			session.getBasicRemote().sendText(message);
		} catch (IOException e) {
			throw new WebSocketClientException("Unable to send message", e);
		}
		return receive(this.timeoutReceive);
	}

	public String receive(TimeMeasure timeout) {
		try {
			return this.messagesReceived.poll(timeout.quantity(), timeout.unit());
		} catch (InterruptedException e) {
			throw new WebSocketClientException(
					"Interrupted waiting on receive", e);
		}
	}

	private ClientManager client() {
		return ClientManager.createClient();
	}

	private ClientEndpointConfig config() {
		return ClientEndpointConfig.Builder.create().build();
	}
}
