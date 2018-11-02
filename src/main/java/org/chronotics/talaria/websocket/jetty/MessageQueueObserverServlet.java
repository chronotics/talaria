package org.chronotics.talaria.websocket.jetty;

import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class MessageQueueObserverServlet extends AbstractWebsocketServlet {
    @Override
    public void configure(WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.getPolicy().setIdleTimeout(getIdleTimeout());
        webSocketServletFactory.register(MessageQueueObserverSocket.class);
    }
}
