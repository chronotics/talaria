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
            JettyServer _server,
            Class _listenerClass,
            JettyListenerAction _listenerConnectAction,
            JettyListenerAction _listenerCloseAction,
            JettyListenerAction _listenerErrorAction,
            JettyListenerAction _listenerBinaryAction,
            JettyListenerAction _listenerTextAction) {
        server = _server;
        listnerClass = _listenerClass;

        if(_listenerConnectAction!=null) {
            listenerConnectAction = _listenerConnectAction;
        }

        if(_listenerCloseAction!=null) {
            listenerCloseAction = _listenerCloseAction;
        }

        if(_listenerErrorAction!=null) {
            listenerErrorAction = _listenerErrorAction;
        }

        if(_listenerBinaryAction!=null) {
            listenerBinaryAction = _listenerBinaryAction;
        }

        if(_listenerTextAction!=null) {
            listenerTextAction = _listenerTextAction;
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
