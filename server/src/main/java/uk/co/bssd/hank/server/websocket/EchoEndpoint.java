package uk.co.bssd.hank.server.websocket;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/echo", configurator=SingletonEndpointConfigurator.class)
public class EchoEndpoint {
	
    @OnMessage
    public String onMessage(String message, Session session) {
        return message;
    }
}