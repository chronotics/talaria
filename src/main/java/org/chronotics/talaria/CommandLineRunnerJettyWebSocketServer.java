package org.chronotics.talaria;

import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.chronotics.talaria.common.TObserver;
import org.chronotics.talaria.websocket.jetty.JettyListener;
import org.chronotics.talaria.websocket.jetty.JettyListenerActionProvider;
import org.chronotics.talaria.websocket.jetty.JettyServer;
import org.chronotics.talaria.websocket.jetty.JettyWebSocketServerProperties;
import org.chronotics.talaria.websocket.jetty.taskexecutor.MQToClient;
import org.chronotics.talaria.websocket.jetty.jettylisteneraction.CreateMessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import rx.Observer;

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
		Executors.newSingleThreadExecutor().execute(()-> {
            if (server == null) {
                server = new JettyServer(
                        Integer.valueOf(jettyWebSocketServerProperties.getPort()));
                server.setContextHandler(
                        jettyWebSocketServerProperties.getContextPath(),
                        JettyServer.SESSIONS);
                server.addWebSocketListener(
                        jettyWebSocketServerProperties.getContextPath(),
                        jettyWebSocketServerProperties.getTopicId(),
                        jettyWebSocketServerProperties.getTopicPath(),
////						EachMQToAllSessionsReadOnly.class,
////						null,
//                        JettyListener.class,
//                        (listener, session) -> {
//                            String id = listener.getId();
//                            assert (id != null && !id.equals(""));
//                            MessageQueueMap mqMap = MessageQueueMap.getInstance();
//                            MessageQueue<Object> mq = (MessageQueue<Object>) mqMap.get(id);
//                            assert (mq == null);
//                            // add observer
//                            MQToClient taskExecutor =
//                                    new MQToClient(
//                                            MQToClient.KIND_OF_RECIEVER.ALL_CLIENTS,
//                                            false);
////											true);
//                            taskExecutor.putProperty(
//                                    MQToClient.PROPERTY_ID,
//                                    id);
//                            taskExecutor.putProperty(
//                                    MQToClient.PROPERTY_JETTYSERVER,
//                                    listener.getServer());
////							this.observer = taskExecutor.getObserver();
////                            mq.addObserver(taskExecutor.getObserver());
//							mq.subscribe(taskExecutor.getObserver());
//                        },
						JettyListener.class,
						new JettyListenerActionProvider
                            .Builder()
                            .addAction(new CreateMessageQueue())
                            .addAction(
                                (listener, session) -> {
									String id = listener.getId();
									assert(id != null && !id.equals(""));
									MessageQueueMap mqMap = MessageQueueMap.getInstance();
									MessageQueue<Object> mq = (MessageQueue<Object>) mqMap.get(id);
									// always null because getId() is unique
									assert(mq!=null);

                                    // add observer
                                    MQToClient taskExecutor =
                                            new MQToClient(
                                                    MQToClient.KIND_OF_RECIEVER.ALL_CLIENTS,
                                                    false);
                                    taskExecutor.putProperty(
                                    		MQToClient.PROPERTY_ID,
											id);
                                    taskExecutor.putProperty(
                                    		MQToClient.PROPERTY_JETTYSERVER,
											listener.getServer());

//									Observer observer = taskExecutor.getObserver();
//									listener.setObserver(observer);
//									mq.subscribe(observer);
									TObserver observer = taskExecutor.getObserver();
									listener.setObserver(observer);
									mq.addObserver(observer);
                                }
							)
                            .build(),
//                            (listener, session) -> {
//                                String id = listener.getId();
//                                assert (id != null && !id.equals(""));
//                                MessageQueueMap mqMap = MessageQueueMap.getInstance();
//                                MessageQueue<Object> mq = (MessageQueue<Object>) mqMap.get(id);
//                                assert (mq == null);
//                                // add observer
//                                MQToClient taskExecutor =
//                                        new MQToClient(
//                                                MQToClient.KIND_OF_RECIEVER.ALL_CLIENTS,
//                                                false);
//    //											true);
//                                taskExecutor.putProperty(
//                                        MQToClient.PROPERTY_ID,
//                                        id);
//                                taskExecutor.putProperty(
//                                        MQToClient.PROPERTY_JETTYSERVER,
//                                        listener.getServer());
//    //							this.observer = taskExecutor.getObserver();
//    //                            mq.addObserver(taskExecutor.getObserver());
//                                mq.subscribe(taskExecutor.getObserver());
//                            }).build(),
                        null,
                        null,
                        null,
                        null
                );
            }
            if (server.isStopped()) {
                server.start();
            }
		});

		logger.info("Jetty WebSocket server is started...");
		logger.info("URL of Jetty WebSocket server is {}:{}",
				jettyWebSocketServerProperties.getIp(),
				jettyWebSocketServerProperties.getPort());
	}
}
