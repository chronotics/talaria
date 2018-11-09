package org.chronotics.talaria.websocket.jetty;

import org.chronotics.talaria.common.MessageQueue;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

public class MessageQueueObserver
        extends AbstractWebsocketListener
        implements Observer {

    private static final Logger logger =
            LoggerFactory.getLogger(MessageQueueObserver.class);

    @Override
    public void onWebSocketBinary(byte[] bytes, int i, int i1) {

    }

    @Override
    public void onWebSocketText(String s) {

    }

    @Override
    public void onWebSocketClose(int i, String s) {

    }

    @Override
    public void onWebSocketConnect(Session session) {
        setSession(session);
        logger.info("MessageQueueObserver is connected");
    }

    @Override
    public void onWebSocketError(Throwable throwable) {

    }

    @Override
    public void update(Observable observable, Object o) {
        String msg = (String)o;
        if(msg.equals(MessageQueue.notifyingMessageRemove)) {
        } else {
            if(o instanceof String) {
                msg = (String)o;
            } else {
                return;
            }

            if(session!=null && session.isOpen()) {
                try {
                    session.getRemote().sendString(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
