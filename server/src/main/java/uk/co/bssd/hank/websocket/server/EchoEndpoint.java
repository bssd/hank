package uk.co.bssd.hank.websocket.server;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import uk.co.bssd.hank.Announcer;
import uk.co.bssd.hank.SessionListener;

@ServerEndpoint(value = "/echo", configurator=SingletonEndpointConfigurator.class)
public class EchoEndpoint {
	
	private Announcer<SessionListener> sessionListeners;
	
	public EchoEndpoint(Announcer<SessionListener> sessionListeners) {
		this.sessionListeners = sessionListeners;
	}
	
	@OnOpen
	public void onOpen(Session session) {
		this.sessionListeners.announce().onOpen(session.getId());
	}
	
    @OnMessage
    public String onMessage(String message, Session session) {
        return message;
    }
}