package org.chronotics.talaria.websocket.jetty.websocket;

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

    private int numOfReceivedMessage = 0;

    public int getNumOfReceivedMessage() {
        return numOfReceivedMessage;
    }

    public ClientHandlerExample() {
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        super.onClose(statusCode, reason);
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        super.onConnect(session);
        lastAccessTime = System.currentTimeMillis();
    }

    private long lastAccessTime = 0;

    @Override
    public boolean isBusy() {
        long currTime = System.currentTimeMillis();
        long duration = abs(currTime - lastAccessTime);
        return (duration < idleDuration) ? true: false;
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        lastAccessTime = System.currentTimeMillis();
        numOfReceivedMessage++;
        logger.info("Client received {} \n",msg);
    }
}
