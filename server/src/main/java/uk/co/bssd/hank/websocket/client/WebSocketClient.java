package uk.co.bssd.hank.websocket.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

import uk.co.bssd.hank.datetime.Time;

public class WebSocketClient {

	public static final String DEFAULT_HOST = "localhost";
	public static final int DEFAULT_PORT = 80;
	public static final String DEFAULT_CONTEXT = "websocket";

	private final URI uri;
	private final CountDownLatch messageLatch;
	private final AtomicReference<String> receivedMessage;

	private Session session;

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

	private WebSocketClient(String hostName, int port, String contextPath,
			String endpoint) {
		this.uri = uri(hostName, port, contextPath, endpoint);
		this.messageLatch = new CountDownLatch(1);
		this.receivedMessage = new AtomicReference<String>();
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

	public void connect(Time timeout) {
		ConnectFuture future = new ConnectFuture();
		initiateConnection(future);
		this.session = future.await(timeout);
		this.session.addMessageHandler(new MessageHandler.Whole<String>() {
			public void onMessage(String message) {
				receivedMessage.set(message);
				messageLatch.countDown();
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

	public String send(final String message) {
		try {
			session.getBasicRemote().sendText(message);
		} catch (IOException e) {
			throw new WebSocketClientException("Unable to send message", e);
		}
		return receive();
	}

	public String receive() {
		try {
			this.messageLatch.await(10, TimeUnit.SECONDS);
			return this.receivedMessage.get();
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
