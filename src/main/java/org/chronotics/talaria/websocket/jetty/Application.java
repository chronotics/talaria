package org.chronotics.talaria.websocket.jetty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * if you don't want to use ScheduledUpdates,
 * comment "org.chronotics.talaria"
 */
@SpringBootApplication
@ComponentScan(basePackages = {
        "org.chronotics.talaria",
        "org.chronotics.talaria.websocket.jetty"})
public class Application {
    private static final Logger logger =
            LoggerFactory.getLogger(Application.class);

    private static JettyServer server = null;

    public static void main(String[] args) throws Exception {
        /**
         * Traditional Method
         */
//        int port = 8080;
//
//        Server server = new Server(port);
//
//        ServletContextHandler context =
//                new ServletContextHandler(ServletContextHandler.SESSIONS);
//        context.setContextPath("/");
//        server.setHandler(context);
//
//        ServletHolder wsHolder = new ServletHolder(
//                "echo",
//                new JettyWebSocketServlet(null, EmptyListener.class));
//        context.addServlet(wsHolder, "/*");
//
////        URL url = Thread.currentThread().getContextClassLoader().getResource("index.html");
////        Objects.requireNonNull(url, "unable to find index.html");
////        String urlBase = url.toExternalForm().replaceFirst("/[^/]*$", "/");
////        ServletHolder defHolder = new ServletHolder("default", new DefaultServlet());
////        defHolder.setInitParameter("resourceBase", urlBase);
////        defHolder.setInitParameter("dirAllowed", "true");
////        context.addServlet(defHolder,"/");
//
//        try {
//            server.start();
//            logger.info("Websocket server started");
//            server.join();
//            logger.info("server.join()");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        /**
//         * The proposed method
//         */
//        // run spring boot
//        ApplicationContext context = SpringApplication
//                .run(Application.class,args);
//
//        JettyWebSocketServerProperties jettyWebSocketServerProperties =
//                (JettyWebSocketServerProperties)context.getBean("jettyWebSocketServerProperties");
//        if(jettyWebSocketServerProperties == null) {
//            logger.error("check DI injection of JettyWebSocketServerProperties");
//        }
//
//        assert(jettyWebSocketServerProperties != null);
//        if(jettyWebSocketServerProperties == null) {
//            return;
//        }
//        assert(!jettyWebSocketServerProperties.isNull());
//        if(jettyWebSocketServerProperties.isNull()) {
//            logger.error("JettyWebSocketServerProperties is null");
//            return;
//        }
    }
}
