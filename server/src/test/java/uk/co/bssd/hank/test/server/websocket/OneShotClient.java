package uk.co.bssd.hank.test.server.websocket;

import java.io.IOException;
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

public class OneShotClient {

	private final URI uri;

	public OneShotClient(String hostName, int port, String contextPath) {
		this.uri = uri(hostName, port, contextPath);
	}

	private URI uri(String hostName, int port, String contextPath) {
		try {
			return new URI(String.format("ws://%s:%d%s/echo", hostName, port,
					contextPath));
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public String sendMessage(final String messageToSend) {
		try {
			final CountDownLatch messageLatch = new CountDownLatch(1);
			final ClientEndpointConfig cec = ClientEndpointConfig.Builder
					.create().build();
			final AtomicReference<String> receivedMessage = new AtomicReference<String>();

			ClientManager client = ClientManager.createClient();
			client.connectToServer(new Endpoint() {
				@Override
				public void onOpen(Session session, EndpointConfig config) {
					try {
						session.addMessageHandler(new MessageHandler.Whole<String>() {
							public void onMessage(String messageReceived) {
								receivedMessage.set(messageReceived);
								messageLatch.countDown();
							}
						});
						session.getBasicRemote().sendText(messageToSend);
					} catch (IOException e) {
						throw new IllegalStateException(e);
					}
				}
			}, cec, uri);
			messageLatch.await(100, TimeUnit.SECONDS);
			return receivedMessage.get();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
