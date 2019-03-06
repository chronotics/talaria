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

    private JettyListenerActionProvider connectActionProvider = null;
    private JettyListenerActionProvider closeActionProvider = null;
    private JettyListenerActionProvider textActionProvider = null;
    private JettyListenerActionProvider binaryActionProvider = null;
    private JettyListenerActionProvider errorActionProvider = null;

    public JettyWebSocketServlet(
            JettyServer server,
            Class listenerClass,
            JettyListenerActionProvider connectActionProvider,
            JettyListenerActionProvider closeActionProvider,
            JettyListenerActionProvider textActionProvider,
            JettyListenerActionProvider binaryActionProvider,
            JettyListenerActionProvider errorActionProvider) {
        this.server = server;
        this.listnerClass = listenerClass;

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
    public void configure(WebSocketServletFactory factory) {
        factory.getPolicy().setIdleTimeout(getIdleTimeout());
//        factory.register(listnerClass);
        factory.setCreator(
                new JettyWebSocketCreator(
                        server,
                        listnerClass,
                        connectActionProvider,
                        closeActionProvider,
                        textActionProvider,
                        binaryActionProvider,
                        errorActionProvider
                        ));
    }
}
