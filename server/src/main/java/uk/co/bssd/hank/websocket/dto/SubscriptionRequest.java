package uk.co.bssd.hank.websocket.dto;

public final class SubscriptionRequest {

	public static SubscriptionRequest subscribe(String key) {
		return new SubscriptionRequest(Action.SUBSCRIBE, key);
	}
	
	public static SubscriptionRequest unsubscribe(String key) {
		return new SubscriptionRequest(Action.UNSUBSCRIBE, key);
	}
	
	public enum Action {
		SUBSCRIBE, UNSUBSCRIBE;
	}
	
	private final Action action;
	private final String key;
	
	public SubscriptionRequest(Action action, String key) {
		this.action = action;
		this.key = key;
	}
	
	public Action action() {
		return this.action;
	}
	
	public String key() {
		return this.key;
	}
}