package org.chronotics.talaria;

import org.chronotics.talaria.websocket.jetty.JettyServer;
import org.chronotics.talaria.websocket.jetty.websocketlistener.SessionToSessionGroupThroughMQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "org.chronotics.talaria.common",
        "org.chronotics.talaria.websocket"})
public class WebSocketServer {
    private static final Logger logger =
            LoggerFactory.getLogger(WebSocketServer.class);

    public static void main(String[] args) {
        // run spring boot
        ApplicationContext context = SpringApplication
                .run(WebSocketServer.class,args);

        String contextPath = "/";
        String topicId = "topic";
        String topicPath = "/topic/";
        int port = 8081;
        JettyServer server = new JettyServer(port);
        server.setContextHandler(contextPath, JettyServer.SESSIONS);
        server.addWebSocketListener(
                contextPath,
                topicId,
                SessionToSessionGroupThroughMQ.class,
                topicPath);
        server.start();
    }
}
