package uk.co.bssd.hank.websocket.server;

import java.util.HashSet;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/broadcast", configurator=SingletonEndpointConfigurator.class)
public class BroadcastEndpoint {

	private final Set<Session> sessions;
	
	public BroadcastEndpoint() {
		this.sessions = new HashSet<Session>();
	}
	
	@OnOpen
	public void onOpen(Session session) {
		this.sessions.add(session);
		
	}
	
	@OnClose
	public void onClose(Session session) {
		this.sessions.remove(session);
	}

	public void broadcast(String message) {
		for (Session session : this.sessions) {
			session.getAsyncRemote().sendText(message);
		}
	}
}