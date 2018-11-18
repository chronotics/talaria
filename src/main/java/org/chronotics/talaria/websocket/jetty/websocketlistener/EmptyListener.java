package org.chronotics.talaria.websocket.jetty.websocketlistener;

import org.chronotics.talaria.websocket.jetty.JettyListener;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class EmptyListener extends JettyListener {
    private static final Logger logger =
        LoggerFactory.getLogger(EmptyListener.class);

    @Override
    public void onWebSocketBinary(byte[] bytes, int i, int i1) {
        logger.info(getClass().getName() +
                " received a message of {} {} {} ", bytes, i, i1);
    }

    @Override
    public void onWebSocketText(String s) {
        logger.info(getClass().getName() +
                " received a message of {}", s);
//        Future<Void> future = session.getRemote().sendStringByFuture("pong");
//        try {
//            future.get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
//        logger.info(getClass().getName() + " sent pong");
    }

    @Override
    public void onWebSocketClose(int i, String s) {
        super.onWebSocketClose(i,s);
        logger.info(getClass().getName()+"::onWebSocketClose");
    }

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);
        logger.info(getClass().getName()+"::onWebSocketConnect");
    }

    @Override
    public void onWebSocketError(Throwable throwable) {
        logger.error(getClass().getName()+"::onWebSocketError");
        logger.error(throwable.toString());
    }
}
