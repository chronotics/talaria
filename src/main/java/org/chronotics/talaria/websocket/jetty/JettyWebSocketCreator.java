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

    protected JettyListenerAction listenerCloseAction = null;
    protected JettyListenerAction listenerConnectAction = null;
    protected JettyListenerAction listenerBinaryAction = null;
    protected JettyListenerAction listenerTextAction = null;
    protected JettyListenerAction listenerErrorAction = null;

    public JettyWebSocketCreator(
            JettyServer server,
            Class webSocketClass,
            JettyListenerAction listenerConnectAction,
            JettyListenerAction listenerCloseAction,
            JettyListenerAction listenerErrorAction,
            JettyListenerAction listenerBinaryAction,
            JettyListenerAction listenerTextAction) {
        this.server = server;
        this.webSocketClass = webSocketClass;

        if(listenerConnectAction!=null) {
            this.listenerConnectAction = listenerConnectAction;
        }

        if(listenerCloseAction!=null) {
            this.listenerCloseAction = listenerCloseAction;
        }

        if(listenerErrorAction!=null) {
            this.listenerErrorAction = listenerErrorAction;
        }

        if(listenerBinaryAction!=null) {
            this.listenerBinaryAction = listenerBinaryAction;
        }

        if(listenerTextAction!=null) {
            this.listenerTextAction = listenerTextAction;
        }
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

                    if(listenerConnectAction!=null) {
                        listener.setConnectAction(listenerConnectAction);
                    }

                    if(listenerCloseAction!=null) {
                        listener.setCloseAction(listenerCloseAction);
                    }

                    if(listenerErrorAction!=null) {
                        listener.setErrorAction(listenerErrorAction);
                    }

                    if(listenerBinaryAction!=null) {
                        listener.setBinaryAction(listenerBinaryAction);
                    }

                    if(listenerTextAction!=null) {
                        listener.setTextAction(listenerTextAction);
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
