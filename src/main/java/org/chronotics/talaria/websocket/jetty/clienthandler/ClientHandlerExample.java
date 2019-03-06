package org.chronotics.talaria.websocket.jetty.clienthandler;

import org.chronotics.talaria.websocket.jetty.AbstractClientHandler;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Math.abs;

/**
 * 2018. 11. 09
 * @author sglee
 */

@WebSocket(maxTextMessageSize = 64 * 1024)
public class ClientHandlerExample extends AbstractClientHandler {
    private static final Logger logger =
            LoggerFactory.getLogger(ClientHandlerExample.class);

    public ClientHandlerExample() {
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        super.onClose(statusCode, reason);
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        super.onConnect(session);
    }

    @Override
    public boolean isBusy() {
        return super.isBusy();
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        super.onMessage(msg);

        // your business logic
        logger.info("Client received {} \n",msg);
    }
}
