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

    private JettyListenerActionProvider connectActionProvider = null;
    private JettyListenerActionProvider closeActionProvider = null;
    private JettyListenerActionProvider textActionProvider = null;
    private JettyListenerActionProvider binaryActionProvider = null;
    private JettyListenerActionProvider errorActionProvider = null;

    public JettyWebSocketCreator(
            JettyServer server,
            Class webSocketClass,
            JettyListenerActionProvider connectActionProvider,
            JettyListenerActionProvider closeActionProvider,
            JettyListenerActionProvider textActionProvider,
            JettyListenerActionProvider binaryActionProvider,
            JettyListenerActionProvider errorActionProvider) {
        this.server = server;
        this.webSocketClass = webSocketClass;

        if(connectActionProvider!=null) {
            this.connectActionProvider = connectActionProvider;
        }
        if(closeActionProvider!=null) {
            this.closeActionProvider = closeActionProvider;
        }
        if(textActionProvider!=null) {
            this.textActionProvider = textActionProvider;
        }
        if(binaryActionProvider!=null) {
            this.binaryActionProvider = binaryActionProvider;
        }
        if(errorActionProvider!=null) {
            this.errorActionProvider = errorActionProvider;
        }
    }

    @Override
    public Object createWebSocket(
            ServletUpgradeRequest servletUpgradeRequest,
            ServletUpgradeResponse servletUpgradeResponse) {
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

                    if(connectActionProvider!=null) {
                        listener.setConnectActionProvider(connectActionProvider);
                    }
                    if(closeActionProvider!=null) {
                        listener.setCloseActionProvider(closeActionProvider);
                    }
                    if(textActionProvider!=null) {
                        listener.setTextActionProvider(textActionProvider);
                    }
                    if(binaryActionProvider!=null) {
                        listener.setBinaryActionProvider(binaryActionProvider);
                    }
                    if(errorActionProvider!=null) {
                        listener.setErrorActionProvider(errorActionProvider);
                    }
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
