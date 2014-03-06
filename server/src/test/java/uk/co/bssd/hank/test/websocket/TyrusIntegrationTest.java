package uk.co.bssd.hank.test.websocket;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static uk.co.bssd.hank.datetime.Time.seconds;
import static uk.co.bssd.hank.websocket.server.WebSocketServer.aServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.websocket.DeploymentException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.co.bssd.hank.Announcer;
import uk.co.bssd.hank.SessionListener;
import uk.co.bssd.hank.datetime.Time;
import uk.co.bssd.hank.websocket.client.WebSocketClient;
import uk.co.bssd.hank.websocket.server.BroadcastEndpoint;
import uk.co.bssd.hank.websocket.server.EchoEndpoint;
import uk.co.bssd.hank.websocket.server.WebSocketServer;

public class TyrusIntegrationTest {

	private static final int PORT = 8080;
	private static final Time TIMEOUT_CONNECT = seconds(10);
	private static final Time TIMEOUT_RECEIVE = seconds(10);

	private WebSocketServer server;
	private BroadcastEndpoint broadcastEndpoint;

	@Before
	public void before() throws DeploymentException {
		this.broadcastEndpoint = new BroadcastEndpoint();

		this.server = aServer().withPort(PORT)
				.addEndpoint(BroadcastEndpoint.class, this.broadcastEndpoint)
				.build();
		this.server.start();
	}

	@After
	public void after() {
		this.server.stop();
	}

	@Test
	public void testServerNotifiedWhenNewSessionConnects() {
		SessionListener mockSessionListener = mock(SessionListener.class);
		this.server.addSessionListener(mockSessionListener);

		clientConnectedToEndpoint("echo");
		verify(mockSessionListener, timeout(1000)).onOpen(anyString());
	}

	@Test
	public void testSendAndReceiveMessage() {
		String message = "this ain't a love song";
		WebSocketClient client = clientConnectedToEndpoint("echo");
		String response = client.send(message);
		assertThat(response, is(equalTo(message)));
	}

	@Test
	public void testBroadcastMessageToClient() {
		String message = "I'm on my way, home sweet home";
		WebSocketClient client = clientConnectedToEndpoint("broadcast");
		this.broadcastEndpoint.broadcast(message);
		assertThat(receiveOne(client), is(equalTo(message)));
	}

	@Test
	public void testBroadcastMultipleMessagesToClient() {
		List<String> messagesToSend = Arrays.asList(
				"She packed my bags last night, preflight",
				"Zero hour, nine a.m.");

		WebSocketClient client = clientConnectedToEndpoint("broadcast");
		broadcast(messagesToSend);
		List<String> messagesReceived = receive(client, messagesToSend.size());

		assertThat(messagesReceived, hasItems(toArray(messagesToSend)));
	}

	private String receiveOne(WebSocketClient client) {
		return receive(client, 1).get(0);
	}

	private List<String> receive(WebSocketClient client, int numberExpected) {
		List<String> messagesReceived = new ArrayList<String>();

		for (int i = 0; i < numberExpected; i++) {
			messagesReceived.add(client.receive(TIMEOUT_RECEIVE));
		}
		return messagesReceived;
	}

	private void broadcast(List<String> messages) {
		for (String message : messages) {
			this.broadcastEndpoint.broadcast(message);
		}
	}

	private String[] toArray(List<String> messagesToSend) {
		return messagesToSend.toArray(new String[messagesToSend.size()]);
	}

	private WebSocketClient clientConnectedToEndpoint(String endpoint) {
		WebSocketClient client = WebSocketClient.aClient().withPort(PORT)
				.withEndpoint(endpoint).build();
		client.connect(TIMEOUT_CONNECT);
		return client;
	}
}