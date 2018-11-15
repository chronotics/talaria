package org.chronotics.talaria.websocket.jetty;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JettyWebSocketCreator implements WebSocketCreator {

    private static final Logger logger =
            LoggerFactory.getLogger(JettyWebSocketCreator.class);

    private JettyServer server = null;
    private Class webSocketClass = null;

    public JettyWebSocketCreator(JettyServer _server, Class _websocketClass) {
        server = _server;
        webSocketClass = _websocketClass;
    }

    @Override
    public Object createWebSocket(ServletUpgradeRequest servletUpgradeRequest, ServletUpgradeResponse servletUpgradeResponse) {
        if(webSocketClass == null) {
            logger.error("webSocketClass is not defined");
            return null;
        }
        Object object = null;
        try {
            object = webSocketClass.newInstance();
            if(object instanceof JettyListener) {
                if(server!=null) {
                    JettyListener listener = (JettyListener) object;
                    listener.setServer(server);
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return object;
    }
}
