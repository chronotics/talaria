package org.chronotics.talaria.websocket.springstompserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {
	@Autowired
	private SpringStompServerProperties properties;
	
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
    	assert(properties != null); 
    	if(properties == null) {
    		return;
    	}
        //config.enableSimpleBroker("/topic");
        config.enableSimpleBroker(properties.getDestinationTopic());
        //config.setApplicationDestinationPrefixes("/ap");
        config.setApplicationDestinationPrefixes(properties.getDestinationPrefix());
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
    	assert(properties != null); 
    	if(properties == null) {
    		return;
    	}
    	registry.addEndpoint(properties.getEndPoint()).withSockJS();
        //registry.addEndpoint("/sockJS").withSockJS();
    }

}