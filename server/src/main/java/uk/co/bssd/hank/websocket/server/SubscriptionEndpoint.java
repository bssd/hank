package uk.co.bssd.hank.websocket.server;

import java.io.IOException;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import uk.co.bssd.hank.Announcer;
import uk.co.bssd.hank.collection.MultiMap;

@ServerEndpoint(value = "/subscription")
public class SubscriptionEndpoint {

	public interface MessageSender {
		void send(String message);
	}

	public interface SubscriptionListener {
		void onSubscriptionOpened(String key);
	}
	
	private final Announcer<SubscriptionListener> subscriptionListeners;
	private final MultiMap<String, MessageSender> subscriptions;

	public SubscriptionEndpoint() {
		this.subscriptionListeners = Announcer.to(SubscriptionListener.class);
		this.subscriptions = new MultiMap<String, MessageSender>();
	}

	public void addSubscriptionListener(SubscriptionListener listener) {
		this.subscriptionListeners.addListener(listener);
	}

	public void broadcast(String key, String message) {
		announcer(key).announce().send(message);
	}

	@OnMessage
	public String onMessage(String key, final Session session) {
		this.subscriptionListeners.announce().onSubscriptionOpened(key);
		this.subscriptions.put(key, new MessageSender() {
			@Override
			public void send(String message) {
				try {
					session.getBasicRemote().sendText(message);
				}
				catch (IOException e) {
					// TODO: log this
				}
			}
		});
		return key;
	}
	
	private Announcer<MessageSender> announcer(String key) {
		return Announcer.to(MessageSender.class, this.subscriptions.get(key));
	}
}