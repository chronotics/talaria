package org.chronotics.talaria.websocket.springstompserver;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * @author SG Lee
 * @since 3/20/2015
 * @description
 */

@Validated
@PropertySources({
	@PropertySource(value = "classpath:spring/properties/missing.properties", ignoreResourceNotFound=true),
	@PropertySource("classpath:spring/properties/springstompserver.properties")
	})
@Component
public class SpringStompServerProperties {
	
//	public SpringStompServerProperties(SpringStompServerProperties _arg) {
//		this.set(_arg.port, _arg.sessionTimeout, _arg.destinationTopic, _arg.destinationPrefix, _arg.endPoint, _arg.targetDestination);
//	}
//	
//	private void set(
//			Integer port,
//			Integer sessionTimeout,
//			String destinationTopic,
//			String destinationPrefix,
//			String endPoint,
//			String targetDestination) {
//		this.port = port;
//		this.sessionTimeout = sessionTimeout;
//		this.destinationTopic = destinationTopic;
//		this.destinationPrefix = destinationPrefix;
//		this.endPoint = endPoint;
//		this.targetDestination = targetDestination;
//	}
	
	@Valid
	@NotNull
	@Value("${stompserver.port}")
	private Integer port;
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer _port) {
		port = _port;
	}
	
	@Valid
	@NotNull
	@Value("${stompserver.sessionTimeout}")
	private Integer sessionTimeout;
	public Integer getSessionTimeout() {
		return sessionTimeout;
	}
	public void setSessionTimeout(Integer _sessionTimeout) {
		sessionTimeout = _sessionTimeout;
	}
	
	@Valid
	@NotNull
	@Value("${stompserver.destinationTopic}")
	private String destinationTopic;
	public String getDestinationTopic() {
		return destinationTopic;
	}
	public void setDestinationTopic(String _destinationTopic) {
		destinationTopic = _destinationTopic;
	}
	
	@Valid
	@NotNull
	@Value("${stompserver.destinationPrefix}")
	private String destinationPrefix;
	public String getDestinationPrefix() {
		return destinationPrefix;
	}
	public void setDestinationPrefix(String _destinationPrefix) {
		destinationPrefix = _destinationPrefix;
	}
	
	@Valid
	@NotNull
	@Value("${stompserver.endPoint}")
	private String endPoint;
	public String getEndPoint() {
		return endPoint;
	}
	public void setEndPoint(String _endPoint) {
		endPoint = _endPoint;
	}
	
	@Valid
	@NotNull
	@Value("${stompserver.targetDestination}")
	private String targetDestination;
	public String getTargetDestination() {
		return targetDestination;
	}
	public void setTargetDestination(String _targetDestination) {
		targetDestination = _targetDestination;
	}
}
