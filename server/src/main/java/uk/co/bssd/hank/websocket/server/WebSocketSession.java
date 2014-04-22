package uk.co.bssd.hank.websocket.server;

import java.io.IOException;

import javax.websocket.Session;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

class WebSocketSession implements MessageSender {

	private final Session session;

	WebSocketSession(Session session) {
		this.session = session;
	}

	public void send(String message) {
		try {
			session.getBasicRemote().sendText(message);
		}
		catch (IOException e) {
			// TODO: log this
		}
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WebSocketSession) {
			WebSocketSession other = (WebSocketSession) obj;
			return new EqualsBuilder().append(this.session.getId(),
					other.session.getId()).isEquals();
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.session.getId()).toHashCode();
	}
}
