package uk.co.bssd.hank.websocket.server;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import uk.co.bssd.hank.Announcer;
import uk.co.bssd.hank.collection.MultiMap;
import uk.co.bssd.hank.websocket.dto.SubscriptionRequest;
import uk.co.bssd.hank.websocket.dto.SubscriptionRequest.Action;

import com.google.gson.Gson;

@ServerEndpoint(value = "/subscription")
public class SubscriptionEndpoint {

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
	public void onMessage(String message, Session session) {
		Gson gson = new Gson();
		SubscriptionRequest request = gson.fromJson(message, SubscriptionRequest.class);
		
		Action action = request.action();
		String key = request.key();
		
		if (action == Action.SUBSCRIBE) {
			this.subscriptionListeners.announce().onSubscriptionOpened(key);
			this.subscriptions.put(key, new WebSocketSession(session));
		} else if (action == Action.UNSUBSCRIBE) {
			this.subscriptionListeners.announce().onSubscriptionClosed(key);
			this.subscriptions.remove(key, new WebSocketSession(session));
		}
	}
	
	private Announcer<MessageSender> announcer(String key) {
		return Announcer.to(MessageSender.class, this.subscriptions.get(key));
	}
}