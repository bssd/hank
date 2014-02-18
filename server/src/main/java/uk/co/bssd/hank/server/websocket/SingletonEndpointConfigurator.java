package uk.co.bssd.hank.server.websocket;

import java.util.HashMap;
import java.util.Map;

import javax.websocket.server.ServerEndpointConfig.Configurator;

public class SingletonEndpointConfigurator extends Configurator {

	private static final Map<Class<?>, Object> singletons = new HashMap<Class<?>, Object>();

	public static void register(Class<?> clazz, Object singleton) {
		singletons.put(clazz, singleton);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getEndpointInstance(Class<T> clazz)
			throws InstantiationException {
		return (T) singletons.get(clazz);
	}

	public static void clear() {
		singletons.clear();
	}
}