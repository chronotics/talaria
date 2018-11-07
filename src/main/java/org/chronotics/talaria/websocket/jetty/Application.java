package org.chronotics.talaria.websocket.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Application {
    private static final Logger logger =
            LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {

//        JettyServer jettyServer = new JettyServer();
//        jettyServer.setup();
//
//        ServletContextHandler handler =
//                new ServletContextHandler(ServletContextHandler.SESSIONS);
//
//        handler.setContextPath("/");
////        handler.addServlet(JettyServlet.class, "/hello");
//
//        jettyServer.addHandler(handler);
//
//        try {
//            jettyServer.start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        int port = 8080;

        Server server = new Server(port);

        ServletContextHandler context =
                new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        ServletHolder wsHolder = new ServletHolder("echo", new JettyServlet(JettySocket.class));
        context.addServlet(wsHolder, "/*");

//        URL url = Thread.currentThread().getContextClassLoader().getResource("index.html");
//        Objects.requireNonNull(url, "unable to find index.html");
//        String urlBase = url.toExternalForm().replaceFirst("/[^/]*$", "/");
//        ServletHolder defHolder = new ServletHolder("default", new DefaultServlet());
//        defHolder.setInitParameter("resourceBase", urlBase);
//        defHolder.setInitParameter("dirAllowed", "true");
//        context.addServlet(defHolder,"/");

        try {
            server.start();
            logger.info("Websocket server started");
            server.join();
            logger.info("server.join()");

//            try{
//                Thread.sleep(2000);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            server.stop();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
