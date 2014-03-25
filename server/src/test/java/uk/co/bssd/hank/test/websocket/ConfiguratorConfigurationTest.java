package uk.co.bssd.hank.test.websocket;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.ServiceLoader;

import javax.websocket.server.ServerEndpointConfig.Configurator;

import org.junit.Test;

import uk.co.bssd.hank.websocket.server.DefaultServerEndpointConfigurator;

public class ConfiguratorConfigurationTest {

	@Test
	public void testNumberOfRegisteredServerEndpointConfigurators() {
		Iterator<Configurator> configurators = configurators();
		assertThat(count(configurators), is(2));
	}
	
	@Test
	public void testDefaultServerEndpointConfiguratorIsFirstRegistered() {
		assertThat(configurators().next(), instanceOf(DefaultServerEndpointConfigurator.class));
	}

	private int count(Iterator<Configurator> configurators) {
		int count = 0;
		
		while(configurators.hasNext()) {
			configurators.next();
			count++;
		}
		return count;
	}

	private Iterator<Configurator> configurators() {
		return ServiceLoader.load(javax.websocket.server.ServerEndpointConfig.Configurator.class).iterator();
	}
}