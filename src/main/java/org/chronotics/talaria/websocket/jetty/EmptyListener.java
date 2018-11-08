package org.chronotics.talaria.websocket.jetty;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class EmptyListener extends AbstractWebsocketListener {
    private static final Logger logger =
        LoggerFactory.getLogger(EmptyListener.class);

    @Override
    public void onWebSocketBinary(byte[] bytes, int i, int i1) {
        logger.info("Server received a message of {} {} {} ", bytes, i, i1);
    }

    @Override
    public void onWebSocketText(String s) {
        logger.info("Server received a message of {}", s);
    }

    @Override
    public void onWebSocketClose(int i, String s) {
        this.session.close();
        this.session = null;
        logger.info("ServerListener::onWebSocketClose");
    }

    @Override
    public void onWebSocketConnect(Session session) {
        this.session = session;
        logger.info("ServerListener::onWebSocketConnect");
    }

    @Override
    public void onWebSocketError(Throwable throwable) {
        logger.error(throwable.toString());
    }
}
