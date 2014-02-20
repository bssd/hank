package uk.co.bssd.hank.websocket.server;

public class WebSocketServerException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public WebSocketServerException(String message) {
		super(message);
	}
	
	public WebSocketServerException(String message, Exception exception) {
		super(message, exception);
	}
}