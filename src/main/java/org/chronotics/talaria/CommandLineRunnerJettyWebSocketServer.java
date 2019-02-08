package org.chronotics.talaria;

import org.chronotics.talaria.thrift.ThriftServer;
import org.chronotics.talaria.thrift.ThriftServerProperties;
import org.chronotics.talaria.websocket.jetty.JettyServer;
import org.chronotics.talaria.websocket.jetty.JettyWebSocketServerProperties;
import org.chronotics.talaria.websocket.jetty.websocketlistener.EachMQToAllSessions;
import org.chronotics.talaria.websocket.jetty.websocketlistener.EachMQToAllSessionsReadOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.Executors;

@SpringBootApplication
public class CommandLineRunnerJettyWebSocketServer implements CommandLineRunner {

	private static final Logger logger = 
			LoggerFactory.getLogger(CommandLineRunnerJettyWebSocketServer.class);
					
	@Autowired
	private ApplicationContext context;


	private static JettyServer server = null;

	@Override
	public void run(String... arg0) throws Exception {
		
		TalariaProperties properties =
				(TalariaProperties)context.getBean("talariaProperties");
					
		assert(properties != null);
		if(properties == null) {
			return;
		}

		// Jetty WebSocket server properties
		JettyWebSocketServerProperties jettyWebSocketServerProperties =
				properties.getJettyWebSocketServerproperties();

		assert(jettyWebSocketServerProperties != null);
		if(jettyWebSocketServerProperties == null) {
			return;
		}
		assert(!jettyWebSocketServerProperties.isNull());
		if(jettyWebSocketServerProperties.isNull()) {
			logger.error("JettyWebSocketServerProperties is null");
			return;
		}

		/**
		 * start JettyWebSocketServer
		 * EachMQToAllSession
		 * send messages on each MQ to all websocket clients
		 */
//		Executors.newSingleThreadExecutor().execute(new Runnable() {
//			@Override
//			public void run() {
//            }
//        });
		Executors.newSingleThreadExecutor().execute( () -> {
			if(server == null) {
				server = new JettyServer(
						Integer.valueOf(jettyWebSocketServerProperties.getPort()));
				server.setContextHandler(
						jettyWebSocketServerProperties.getContextPath(),
						JettyServer.SESSIONS);
				server.addWebSocketListener(
						jettyWebSocketServerProperties.getContextPath(),
						jettyWebSocketServerProperties.getTopicId(),
						EachMQToAllSessionsReadOnly.class,
						jettyWebSocketServerProperties.getTopicPath());
			}

			if(server.isStopped()) {
				server.start();
			}
		});

		logger.info("Jetty WebSocket server is started...");
		logger.info("URL of Jetty WebSocket server is {}:{}",
				jettyWebSocketServerProperties.getIp(),
				jettyWebSocketServerProperties.getPort());
	}
}
