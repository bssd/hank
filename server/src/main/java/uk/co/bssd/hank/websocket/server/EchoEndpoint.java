package uk.co.bssd.hank.websocket.server;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/echo", configurator=SingletonEndpointConfigurator.class)
public class EchoEndpoint {
	
	@OnOpen
	public void onOpen(Session session) {
		System.out.println("Poo");
		System.out.println(session);
	}
	
    @OnMessage
    public String onMessage(String message, Session session) {
        return message;
    }
}