package org.chronotics.talaria;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.chronotics.talaria.thrift.ThriftServerProperties;
import org.chronotics.talaria.websocket.jetty.JettyWebSocketServerProperties;
import org.chronotics.talaria.websocket.jetty.JettyWebSocketServlet;
import org.chronotics.talaria.websocket.springstompserver.SpringStompServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author SG Lee
 * @since 3/20/2015
 * General properties
 */

@PropertySources({
	@PropertySource(
			value = "classpath:spring/properties/missing.properties",
			ignoreResourceNotFound=true),
	@PropertySource("classpath:spring/properties/talaria.properties")
	})
@Component
public class TalariaProperties {
	@Autowired
	private ThriftServerProperties thriftServerProperties;

	public ThriftServerProperties getThriftServerProperties() {
		return thriftServerProperties;
	}
	public void setThriftServerProperties(ThriftServerProperties _prop) {
		thriftServerProperties = _prop;
	}

	@Autowired
	private JettyWebSocketServerProperties jettyWebSocketServerproperties;

	public JettyWebSocketServerProperties getJettyWebSocketServerproperties() {
		return jettyWebSocketServerproperties;
	}

	@Valid
	@NotNull
	@Value("#{'${messageQueue.key}'.split(',')}")
	private List<String> messageQueueKeyList;
	public List<String> getMessageQueueKeyList() {
		return messageQueueKeyList;
	}
	public void setMessageQueueKey(List<String> _mqKeyList) {
		messageQueueKeyList = _mqKeyList;
	}
}
