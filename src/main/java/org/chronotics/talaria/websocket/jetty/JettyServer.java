package org.chronotics.talaria.websocket.jetty;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class JettyServer {
    private Server server = null;

    public void setup() {
        server = new Server();

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(9000);

        server.addConnector(connector);

        HandlerList handlerList =
                new HandlerList();

        server.setHandler(handlerList);

//        ServletContextHandler handler =
//                new ServletContextHandler(ServletContextHandler.SESSIONS);
//
//        handlerList.addHandler(handler);
//
//        handler.setContextPath("/");
//        handler.addServlet(JettyServlet.class, "/test");

    }

    public boolean addHandler(ServletContextHandler _handler) {
       if(server == null) {
           return false;
       }
       HandlerList handlerList = (HandlerList)server.getHandler();
       if(handlerList != null) handlerList.addHandler(_handler);

       return true;
    }

    public void start() throws Exception {
        server.start();
        server.dump(System.err);
        server.join();
    }

    public void stop() throws Exception {
        server.stop();
    }
}
