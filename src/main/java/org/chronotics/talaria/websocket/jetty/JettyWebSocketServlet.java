package org.chronotics.talaria.websocket.jetty;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class JettyWebSocketServlet extends WebSocketServlet {
    // set a 10 second timeout
    // ms, 3,600,000 = 1hour
    private static long idleTimeout = 3600000;

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

    protected JettyListenerAction listenerCloseAction = null;
    protected JettyListenerAction listenerConnectAction = null;
    protected JettyListenerAction listenerBinaryAction = null;
    protected JettyListenerAction listenerTextAction = null;
    protected JettyListenerAction listenerErrorAction = null;

    public JettyWebSocketServlet(
            JettyServer server,
            Class listenerClass,
            JettyListenerAction listenerConnectAction,
            JettyListenerAction listenerCloseAction,
            JettyListenerAction listenerErrorAction,
            JettyListenerAction listenerBinaryAction,
            JettyListenerAction listenerTextAction) {
        this.server = server;
        this.listnerClass = listenerClass;

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
    public void configure(WebSocketServletFactory factory) {
        factory.getPolicy().setIdleTimeout(getIdleTimeout());
//        factory.register(listnerClass);
        factory.setCreator(
                new JettyWebSocketCreator(
                        server,
                        listnerClass,
                        listenerConnectAction,
                        listenerCloseAction,
                        listenerErrorAction,
                        listenerBinaryAction,
                        listenerTextAction
                        ));
    }
}
