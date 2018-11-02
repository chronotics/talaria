package org.chronotics.talaria.websocket.jetty;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JettySocket extends AbstractWebsocketListener {
    private static final Logger logger =
        LoggerFactory.getLogger(JettySocket.class);

    @Override
    public void onWebSocketBinary(byte[] bytes, int i, int i1) {

    }

    @Override
    public void onWebSocketText(String s) {
        logger.info("[WS Server] onText");

        if(session!=null && session.isOpen()) {
            try {
//                session.getRemote().sendString("hello");
                if(s.equals("ping")) {
                    session.getRemote().sendString("pong");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onWebSocketClose(int i, String s) {
        this.session = null;
    }

    @Override
    public void onWebSocketConnect(Session session) {
        this.session = session;
        logger.info("JettySocket is connected");
    }

    @Override
    public void onWebSocketError(Throwable throwable) {

    }
}
