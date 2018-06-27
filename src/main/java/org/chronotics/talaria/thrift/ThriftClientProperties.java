package org.chronotics.talaria.thrift;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Configuration
@Validated
@PropertySources({
	@PropertySource(value = "classpath:spring/properties/missing.properties", ignoreResourceNotFound=true),
	@PropertySource("classpath:spring/properties/thriftclient.properties")
	})
@Component
public class ThriftClientProperties {
	public ThriftClientProperties() {}
	
	public ThriftClientProperties(ThriftClientProperties _properties) {
		this.set(_properties);
	}
	
	public void set(ThriftClientProperties _properties) {
		this.set(_properties.getIp(),
				_properties.getPort());
	}
	
	private void set(
			String ip,
			String port) {
		this.ip = ip;
		this.port = port;
	}

	@Valid
	@NotNull	
	@Value("${thrift.ip}")
	private String ip;
	public String getIp() {
		return ip;
	}
	public void setIp(String _ip) {
		ip = _ip;
	}
	
	@Valid
	@NotNull	
	@Value("${thrift.port}")
	private String port;
	public String getPort() {
		return port;
	}
	public void setPort(String _port) {
		port = _port;
	}
}
