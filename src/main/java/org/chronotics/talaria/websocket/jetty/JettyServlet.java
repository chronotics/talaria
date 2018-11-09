package org.chronotics.talaria.websocket.jetty;

import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class JettyServlet extends AbstractWebsocketServlet {
    private Class listnerClass = null;

    public JettyServlet(Class _class) {
        listnerClass = _class;
    }

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.getPolicy().setIdleTimeout(getIdleTimeout());
        factory.register(listnerClass);
    }
}
