package org.chronotics.talaria.websocket.jetty;

import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class JettyServlet extends AbstractWebsocketServlet {
    private Class listnerClass = null;

    public JettyServlet(Class _class, long _idleTimeOut) {
        listnerClass = _class;
        setIdleTimeout(_idleTimeOut);
    }

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.getPolicy().setIdleTimeout(getIdleTimeout());
        factory.register(listnerClass);
    }
}
