package uk.co.bssd.hank.websocket.server;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import uk.co.bssd.hank.Announcer;

@ServerEndpoint(value = "/subscription")
public class SubscriptionEndpoint {

	private final Announcer<SubscriptionListener> subscriptionListeners;
	
	public interface SubscriptionListener {
		void onSubscriptionOpened(String key);
	}
	
	public SubscriptionEndpoint() {
		this.subscriptionListeners = Announcer.to(SubscriptionListener.class);
	}
	
	public void addSubscriptionListener(SubscriptionListener listener) {
		this.subscriptionListeners.addListener(listener);
	}
	
	@OnMessage
    public String onMessage(String key, Session session) {
		this.subscriptionListeners.announce().onSubscriptionOpened(key);
        return key;
    }
}