package uk.co.bssd.hank.websocket.client;

public class WebSocketClientException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public WebSocketClientException(String message) {
		super(message);
	}
	
	public WebSocketClientException(String message, Exception exception) {
		super(message, exception);
	}
}