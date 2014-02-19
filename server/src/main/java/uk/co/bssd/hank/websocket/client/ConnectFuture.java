package uk.co.bssd.hank.websocket.client;

import java.util.concurrent.CountDownLatch;

import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import uk.co.bssd.hank.datetime.Time;

/* default */class ConnectFuture extends Endpoint {
	private final CountDownLatch connectionLatch;
	private Session session;

	/* default */ConnectFuture() {
		this.connectionLatch = new CountDownLatch(1);
	}

	@Override
	public void onOpen(Session session, EndpointConfig config) {
		this.session = session;
		this.connectionLatch.countDown();
	}

	public Session await(Time timeout) {
		try {
			this.connectionLatch.await(timeout.quantity(), timeout.unit());
		} catch (InterruptedException e) {
			throw new WebSocketClientException(
					"Interrupted whilst waiting for connection", e);
		}

		if (this.session == null) {
			String message = String.format("Failed to connect after %s",
					timeout);
			throw new WebSocketClientException(message);
		}
		return this.session;
	}
}