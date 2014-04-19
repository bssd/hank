package uk.co.bssd.hank.test.websocket;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.co.bssd.hank.datetime.TimeMeasure.milliseconds;
import static uk.co.bssd.hank.datetime.TimeMeasure.seconds;
import static uk.co.bssd.hank.websocket.server.WebSocketServer.aServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.websocket.DeploymentException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.verification.VerificationWithTimeout;

import uk.co.bssd.hank.SessionListener;
import uk.co.bssd.hank.datetime.TimeMeasure;
import uk.co.bssd.hank.websocket.client.WebSocketClient;
import uk.co.bssd.hank.websocket.server.BroadcastEndpoint;
import uk.co.bssd.hank.websocket.server.EchoEndpoint;
import uk.co.bssd.hank.websocket.server.SubscriptionEndpoint;
import uk.co.bssd.hank.websocket.server.SubscriptionEndpoint.SubscriptionListener;
import uk.co.bssd.hank.websocket.server.WebSocketServer;

@RunWith(value = MockitoJUnitRunner.class)
public class TyrusIntegrationTest {

	private static final int PORT = 8080;
	private static final TimeMeasure TIMEOUT_CONNECT = seconds(10);
	private static final TimeMeasure TIMEOUT_RECEIVE = seconds(10);
	private static final TimeMeasure TIMEOUT_RECEIVE_NONE = milliseconds(200);

	private WebSocketServer server;
	private EchoEndpoint echoEndpoint;
	private BroadcastEndpoint broadcastEndpoint;
	private SubscriptionEndpoint subscriptionEndpoint;

	@Mock
	private SubscriptionListener subscriptionListener;

	@Before
	public void before() throws DeploymentException {
		this.broadcastEndpoint = new BroadcastEndpoint();
		this.echoEndpoint = new EchoEndpoint();
		this.subscriptionEndpoint = new SubscriptionEndpoint();
		this.subscriptionEndpoint
				.addSubscriptionListener(this.subscriptionListener);

		this.server = aServer().withPort(PORT)
				.addEndpoint(this.broadcastEndpoint)
				.addEndpoint(this.echoEndpoint)
				.addEndpoint(this.subscriptionEndpoint).build();
		this.server.start();
	}

	@After
	public void after() {
		this.server.stop();
	}

	@Test
	public void testServerNotifiersListenersWhenClientOpensSubscription() {
		WebSocketClient client = clientConnectedToEndpoint("subscription");
		havingOpenedSubscriptionTo(client, "key1");
	}

	@Test
	public void testClientSubscribedToKeyReceivesMessageBroadcastForThatKey() {
		String key = "event1";
		String broadcastMessage = "this ain't a love song";

		WebSocketClient client = clientConnectedToEndpoint("subscription");
		havingOpenedSubscriptionTo(client, key);
		this.subscriptionEndpoint.broadcast(key, broadcastMessage);
		assertThat(receiveOne(client), is(broadcastMessage));
	}
	
	@Test
	public void testClientNotSubscribedToKeyDoesNotReceiveMessageBroadcastForThatKey() {
		WebSocketClient client = clientConnectedToEndpoint("subscription");
		this.subscriptionEndpoint.broadcast("event", "ignored");
		assertThat(receiveNone(client), is(true));
	}
	
	@Test
	public void testClientSubscribedToOneKeyDoesNotReceiveMessageBroadcastForOtherKey() {
		String broadcastMessage = "this ain't a love song";
		
		WebSocketClient client1 = clientConnectedToEndpoint("subscription");
		WebSocketClient client2 = clientConnectedToEndpoint("subscription");
		havingOpenedSubscriptionTo(client1, "event1");
		havingOpenedSubscriptionTo(client2, "event2");
		
		this.subscriptionEndpoint.broadcast("event2", broadcastMessage);
		assertThat(receiveNone(client1), is(true));
		assertThat(receiveOne(client2), is(broadcastMessage));
	}

	@Test
	public void testServerNotifiedWhenNewSessionConnects() {
		SessionListener mockSessionListener = mock(SessionListener.class);
		this.echoEndpoint.addSessionListener(mockSessionListener);

		clientConnectedToEndpoint("echo");
		verify(mockSessionListener, timeout(TIMEOUT_RECEIVE)).onOpen(
				anyString());
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

	private void havingOpenedSubscriptionTo(WebSocketClient client, String key) {
		client.send(key);
		verify(this.subscriptionListener, timeout(TIMEOUT_RECEIVE))
				.onSubscriptionOpened(key);
	}
	
	private boolean receiveNone(WebSocketClient client) {
		return client.receive(TIMEOUT_RECEIVE_NONE) == null;
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

	private VerificationWithTimeout timeout(TimeMeasure timeMeasure) {
		return Mockito.timeout((int) timeMeasure.convert(TimeUnit.MILLISECONDS)
				.quantity());
	}
}