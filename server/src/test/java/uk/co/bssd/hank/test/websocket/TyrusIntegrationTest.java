package uk.co.bssd.hank.test.websocket;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.bssd.hank.datetime.Time.seconds;

import java.util.Collections;

import javax.websocket.DeploymentException;

import org.glassfish.tyrus.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.co.bssd.hank.datetime.Time;
import uk.co.bssd.hank.websocket.client.WebSocketClient;
import uk.co.bssd.hank.websocket.server.BroadcastEndpoint;
import uk.co.bssd.hank.websocket.server.EchoEndpoint;
import uk.co.bssd.hank.websocket.server.SingletonEndpointConfigurator;

public class TyrusIntegrationTest {

	private static final int PORT = 8080;
	
	private static final Time TIMEOUT_CONNECT = seconds(10);

	private Server server;
	private BroadcastEndpoint broadcastEndpoint;

	@Before
	public void before() throws DeploymentException {
		this.broadcastEndpoint = new BroadcastEndpoint();

		SingletonEndpointConfigurator.register(EchoEndpoint.class,
				new EchoEndpoint());
		SingletonEndpointConfigurator.register(BroadcastEndpoint.class,
				this.broadcastEndpoint);

		this.server = new Server("localhost", PORT, "/websocket",
				Collections.<String, Object> emptyMap(), EchoEndpoint.class,
				BroadcastEndpoint.class);
		this.server.start();
	}

	@After
	public void after() {
		this.server.stop();
		SingletonEndpointConfigurator.clear();
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
		assertThat(client.receive(), is(equalTo(message)));
	}

	private WebSocketClient clientConnectedToEndpoint(String endpoint) {
		WebSocketClient client = WebSocketClient.aClient().withPort(PORT)
				.withEndpoint(endpoint).build();
		client.connect(TIMEOUT_CONNECT);
		return client;
	}
}