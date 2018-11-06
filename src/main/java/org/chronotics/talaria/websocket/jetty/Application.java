package org.chronotics.talaria.websocket.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Application {
    public static void main(String[] args) {

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

        ServletHolder wsHolder = new ServletHolder("echo", new JettyServlet());
        context.addServlet(wsHolder, "/echo");

        URL url = Thread.currentThread().getContextClassLoader().getResource("index.html");
        Objects.requireNonNull(url, "unable to find index.html");
        String urlBase = url.toExternalForm().replaceFirst("/[^/]*$", "/");
        ServletHolder defHolder = new ServletHolder("default", new DefaultServlet());
        defHolder.setInitParameter("resourceBase", urlBase);
        defHolder.setInitParameter("dirAllowed", "true");
        context.addServlet(defHolder,"/");

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
