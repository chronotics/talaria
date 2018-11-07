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
//                session.getRemote().sendString("pong");
//                session.getRemote().sendString("hello");
                if(s.equals("ping")) {
                    session.getRemote().sendString("pong");
                    logger.info("pong was sent to clinet");

                } else {
                    logger.info(s);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onWebSocketClose(int i, String s) {
        this.session.close();
        this.session = null;
        logger.info("JettySocket is closed");
    }

    @Override
    public void onWebSocketConnect(Session session) {
        this.session = session;
        logger.info("==================================JettySocket is connected");
        logger.info(session.getUpgradeRequest().getParameterMap().toString());
        logger.info("\n\n\n\n\n\n\n\n\n\n");
    }

    @Override
    public void onWebSocketError(Throwable throwable) {
        logger.error(throwable.toString());
    }
}
