package org.chronotics.talaria.websocket.jetty.websocket;

import org.chronotics.talaria.websocket.jetty.AbstractWebsocket;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 2018. 11. 09
 * @author sglee
 */

@WebSocket(maxTextMessageSize = 64 * 1024)
public class ClientExample extends AbstractWebsocket {
    private static final Logger logger =
            LoggerFactory.getLogger(ClientExample.class);

    public ClientExample() {
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        super.onClose(statusCode, reason);
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        super.onConnect(session);

//        logger.info("client is connected to {}",
//                session.getRemoteAddress().getAddress().getHostAddress());

        sendMessage("ping");
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        System.out.printf("Got msg: %s%n",msg);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sendMessage(msg);
    }

}
