package uk.co.bssd.hank.websocket.server;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.DeploymentException;

import org.glassfish.tyrus.server.Server;

public class WebSocketServer {

	public static final String DEFAULT_HOST = "localhost";
	public static final int DEFAULT_PORT = 80;
	public static final String DEFAULT_CONTEXT = "/websocket";

	public static WebSocketServerBuilder aServer() {
		return new WebSocketServerBuilder().withHost(DEFAULT_HOST)
				.withPort(DEFAULT_PORT).withContext(DEFAULT_CONTEXT);
	}

	public static class WebSocketServerBuilder {

		private String host;
		private int port;
		private String context;
		private final Set<Class<?>> endpoints;

		protected WebSocketServerBuilder() {
			this.endpoints = new HashSet<Class<?>>();
		}

		public WebSocketServerBuilder withHost(String host) {
			this.host = host;
			return this;
		}

		public WebSocketServerBuilder withPort(int port) {
			this.port = port;
			return this;
		}

		public WebSocketServerBuilder withContext(String context) {
			this.context = context;
			return this;
		}

		public WebSocketServerBuilder addEndpoint(Object instance) {
			Class<? extends Object> clazz = instance.getClass();
			DefaultServerEndpointConfigurator.register(clazz, instance);
			this.endpoints.add(clazz);
			return this;
		}

		public WebSocketServer build() {
			return new WebSocketServer(this.host, this.port, this.context,
					this.endpoints);
		}
	}

	private final Server server;

	private WebSocketServer(String host, int port, String context,
			Set<Class<?>> endpoints) {
		this.server = new Server(host, port, context,
				Collections.<String, Object> emptyMap(), endpoints);
	}

	public void start() {
		try {
			this.server.start();
		} catch (DeploymentException e) {
			throw new WebSocketServerException("Unable to start server", e);
		}
	}
	
	public void stop() {
		this.server.stop();
	}
}