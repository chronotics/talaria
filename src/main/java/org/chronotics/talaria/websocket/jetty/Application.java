package org.chronotics.talaria.websocket.jetty;

import org.chronotics.talaria.websocket.jetty.websocketlistener.EmptyListener;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
    private static final Logger logger =
            LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        int port = 8080;

        Server server = new Server(port);

        ServletContextHandler context =
                new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        ServletHolder wsHolder = new ServletHolder(
                "echo",
                new JettyWebSocketServlet(null, EmptyListener.class));
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
