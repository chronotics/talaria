package org.chronotics.talaria;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.stereotype.Component;

@Component
public class TalariaContainerCustomizer implements EmbeddedServletContainerCustomizer {
	@Value("${application.httpPort}")
	private Integer httpPort;

	@Override
	public void customize(ConfigurableEmbeddedServletContainer container) {
		container.setPort(httpPort);
	}

}
