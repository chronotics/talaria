package org.chronotics.talaria.websocket.jetty;

import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class JettyServlet extends AbstractWebsocketServlet {
    @Override
    public void configure(WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.getPolicy().setIdleTimeout(getIdleTimeout());
        webSocketServletFactory.register(JettySocket.class);
    }
}
