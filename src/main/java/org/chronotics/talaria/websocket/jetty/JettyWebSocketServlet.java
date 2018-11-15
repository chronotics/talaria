package org.chronotics.talaria.websocket.jetty;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class JettyWebSocketServlet extends WebSocketServlet {
    // set a 10 second timeout
    // ms
    private static long idleTimeout = 10000;

    /**
     * ms
     * @return
     */
    public static long getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * ms
     * @param idleTimeout
     */
    public static void setIdleTimeout(long idleTimeout) {
        idleTimeout = idleTimeout;
    }

    private Class listnerClass = null;
    private JettyServer server = null;

    public JettyWebSocketServlet(JettyServer _server, Class _listenerClass) {
        server = _server;
        listnerClass = _listenerClass;
    }

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.getPolicy().setIdleTimeout(getIdleTimeout());
//        factory.register(listnerClass);
        factory.setCreator(new JettyWebSocketCreator(server,listnerClass));
    }
}
