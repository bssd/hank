package uk.co.bssd.hank.server.websocket;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/echo", configurator=SimpleEchoEndpointConfigurator.class)
public class SimpleEchoEndpoint {
	
	private final String responseSuffix;
	
	public SimpleEchoEndpoint(String responseSuffix) {
		this.responseSuffix = responseSuffix;
	}
	
    @OnMessage
    public String onMessage(String message, Session session) {
        return message + this.responseSuffix;
    }
}