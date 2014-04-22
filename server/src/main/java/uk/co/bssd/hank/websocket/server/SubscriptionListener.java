package uk.co.bssd.hank.websocket.server;

public interface SubscriptionListener {

	void onSubscriptionOpened(String key);

	void onSubscriptionClosed(String key);
}