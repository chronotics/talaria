package org.chronotics.talaria.thrift;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.stereotype.Component;

//@Component
//public class ThriftContainerCustomizer implements EmbeddedServletContainerCustomizer {
//	@Value("${thrift.httpPort}")
//	private Integer httpPort;
//
//	@Override
//	public void customize(ConfigurableEmbeddedServletContainer container) {
//		container.setPort(httpPort);
//	}
//}
