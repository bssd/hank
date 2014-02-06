package uk.co.bssd.hank.server.websocket;

import javax.websocket.server.ServerEndpointConfig.Configurator;

public class SimpleEchoEndpointConfigurator extends Configurator {

	public static String responseSuffix = " response";

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getEndpointInstance(Class<T> endpointClass)
			throws InstantiationException {
		return (T)new SimpleEchoEndpoint(responseSuffix);
	}
}