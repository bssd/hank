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

import uk.co.bssd.hank.server.websocket.SimpleEchoEndpoint;
import uk.co.bssd.hank.server.websocket.SimpleEchoEndpointConfigurator;

public class TyrusIntegrationTest {

	private static final String HOST_NAME = "localhost";
	private static final int PORT = 8080;
	private static final String CONTEXT_PATH = "/websocket";

	private Server server;

	@Before
	public void before() throws DeploymentException {
		this.server = new Server(HOST_NAME, PORT, CONTEXT_PATH,
				Collections.<String, Object> emptyMap(),
				SimpleEchoEndpoint.class);
		this.server.start();
	}

	@After
	public void after() {
		this.server.stop();
	}

	@Test
	public void testSendAndReceiveMessage() {
		String message = "this ain't a love song";
		OneShotClient client = new OneShotClient(HOST_NAME, PORT, CONTEXT_PATH);
		String response = client.sendMessage(message);
		assertThat(response, is(equalTo(message + SimpleEchoEndpointConfigurator.responseSuffix)));
	}
}