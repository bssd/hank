package uk.co.bssd.hank.websocket.server;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.tyrus.server.TyrusServerEndpointConfigurator;

public class DefaultServerEndpointConfigurator extends TyrusServerEndpointConfigurator {

	private static final Map<Class<?>, Object> endpoints = new HashMap<Class<?>, Object>();

	public static void register(Class<?> clazz, Object endpoint) {
		endpoints.put(clazz, endpoint);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getEndpointInstance(Class<T> clazz)
			throws InstantiationException {
		return (T) endpoints.get(clazz);
	}

	public static void clear() {
		endpoints.clear();
	}
}