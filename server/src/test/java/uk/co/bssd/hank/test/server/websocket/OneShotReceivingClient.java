package uk.co.bssd.hank.test.server.websocket;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

public class OneShotReceivingClient {

	private final URI uri;
	private final CountDownLatch messageLatch;
	private final AtomicReference<String> receivedMessage;

	public OneShotReceivingClient(String hostName, int port,
			String contextPath, String endpoint) {
		this.uri = uri(hostName, port, contextPath, endpoint);
		this.messageLatch = new CountDownLatch(1);
		this.receivedMessage = new AtomicReference<String>();
	}

	private URI uri(String hostName, int port, String contextPath,
			String endpoint) {
		try {
			return new URI(String.format("ws://%s:%d%s/%s", hostName, port,
					contextPath, endpoint));
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public void connect() {
		try {
			client().connectToServer(new Endpoint() {
				@Override
				public void onOpen(Session session, EndpointConfig config) {
					session.addMessageHandler(new MessageHandler.Whole<String>() {
						public void onMessage(String messageReceived) {
							receivedMessage.set(messageReceived);
							messageLatch.countDown();
						}
					});
				}
			}, config(), this.uri);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to connect to server", e);
		}
	}

	public String receive() {
		try {
			this.messageLatch.await(10, TimeUnit.SECONDS);
			return this.receivedMessage.get();
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	private ClientManager client() {
		return ClientManager.createClient();
	}

	private ClientEndpointConfig config() {
		return ClientEndpointConfig.Builder.create().build();
	}
}