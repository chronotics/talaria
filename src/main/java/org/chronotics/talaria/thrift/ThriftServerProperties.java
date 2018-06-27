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
	@PropertySource("classpath:spring/properties/thriftserver.properties")
	})
@Component
public class ThriftServerProperties {
	
	public ThriftServerProperties() {}
	
	public ThriftServerProperties(ThriftServerProperties _properties) {
		this.set(_properties);
	}
	
	public void set(ThriftServerProperties _properties) {
		this.set(_properties.getIp(),
				_properties.getPort(),
				_properties.getSecurePort(),
				_properties.getSecureKeyStore(),
				_properties.getSecureKeyPass(),
				_properties.getSecureServer());
	}
	
	private void set(
			String ip,
			String port,
			String securePort,
			String secureKeyStore,
			String secureKeyPass,
			String secureServer) {
		this.ip = ip;
		this.port = port;
		this.securePort = securePort;
		this.secureKeyStore = secureKeyStore;
		this.secureKeyPass = secureKeyPass;
		this.secureServer = secureServer;
	}
	
	public boolean isNull() {
		if(ip == null && 
				port == null &&
				securePort == null &&
				secureKeyStore ==null &&
				secureKeyPass == null &&
				secureServer == null) {
			return true;
		} else {
			return false;
		}
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
	
	@Valid
	@NotNull	
	@Value("${thrift.securePort}")
	private String securePort;
	public String getSecurePort() {
		return securePort;
	}
	public void setSecurePort(String _port) {
		securePort = _port;
	}
	
	@Valid
	@NotNull	
	@Value("${thrift.secureKeyStore}")
	private String secureKeyStore;
	public String getSecureKeyStore() {
		return secureKeyStore;
	}
	public void setSecureKeyStore(String _secureKeyStore) {
		secureKeyStore = _secureKeyStore;
	}
	
	@Valid
	@NotNull	
	@Value("${thrift.secureKeyPass}")
	private String secureKeyPass;
	public String getSecureKeyPass() {
		return secureKeyPass;
	}
	public void setSecureKeyPass(String _secureKeyPass) {
		secureKeyPass = _secureKeyPass;
	}
	
	@Valid
	@NotNull
	@Value("${thrift.secureServer}")
	private String secureServer;
	public String getSecureServer() {
		return secureServer;
	}
	public void setSecureServer(String _secureServer) {
		secureServer = _secureServer;
	}
}
