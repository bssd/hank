package uk.co.bssd.hank.test.server.websocket;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;

import javax.websocket.DeploymentException;

import org.glassfish.tyrus.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.co.bssd.hank.server.websocket.BroadcastEndpoint;
import uk.co.bssd.hank.server.websocket.EchoEndpoint;
import uk.co.bssd.hank.server.websocket.SingletonEndpointConfigurator;

public class TyrusIntegrationTest {

	private static final String HOST_NAME = "localhost";
	private static final int PORT = 8080;
	private static final String CONTEXT_PATH = "/websocket";

	private Server server;
	private BroadcastEndpoint broadcastEndpoint;

	@Before
	public void before() throws DeploymentException {
		this.broadcastEndpoint = new BroadcastEndpoint();

		SingletonEndpointConfigurator.register(EchoEndpoint.class,
				new EchoEndpoint());
		SingletonEndpointConfigurator.register(BroadcastEndpoint.class,
				this.broadcastEndpoint);

		this.server = new Server(HOST_NAME, PORT, CONTEXT_PATH,
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
		OneShotSendingClient client = new OneShotSendingClient(HOST_NAME, PORT,
				CONTEXT_PATH);
		String response = client.sendMessage(message);
		assertThat(response, is(equalTo(message)));
	}

	@Test
	public void testBroadcastMessageToClient() {
		String message = "I'm on my way, home sweet home";
		OneShotReceivingClient client = new OneShotReceivingClient(HOST_NAME,
				PORT, CONTEXT_PATH, "broadcast");
		client.connect();
		this.broadcastEndpoint.broadcast(message);
		assertThat(client.receive(), is(equalTo(message)));
	}
}