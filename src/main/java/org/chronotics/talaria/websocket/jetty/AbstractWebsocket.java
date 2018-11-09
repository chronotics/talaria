package org.chronotics.talaria.websocket.jetty;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Since 2015
 * Written by SGlee
 */

public abstract class AbstractWebsocket {
    private static final Logger logger =
            LoggerFactory.getLogger(AbstractWebsocket.class);

    public static int timeoutToSendMessage = 2000;

    protected CountDownLatch latch = null;
    protected boolean isCloseRequested;
    protected Session session = null;

    protected AbstractWebsocket() {
        isCloseRequested = false;
        latch = new CountDownLatch(1);
    }

    public boolean awaitClose(int duration, TimeUnit unit)
            throws InterruptedException {
        // wait during duration
        logger.info("awaitClose, duration is {}", duration);
        return this.latch.await(duration,unit);
    }

    public void await() {
        // wait until latch down
        try {
            logger.info("waiting for close...");
            this.latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Session getSession() {
        return session;
    }

    protected void setSession(Session session) {
        this.session = session;
    }

    /**
     * You have to add "super.onClose()" in a derived class
     * @param statusCode
     * @param reason
     */
    protected void onClose(int statusCode, String reason) {
        isCloseRequested = true;
        logger.info("onClose...statusCode:{}, reason:{}",statusCode, reason);

        if(session != null) {
            session.close();
            session = null;
        }
        logger.info("session is closed");

        this.latch.countDown(); // trigger latch
    }

    /**
     * You have to add "super.onConnect()" in a derived class
     * @param session
     */
    protected void onConnect(Session session) {
        setSession(session);
    }

    public boolean sendMessage(String _message) {
        if(isCloseRequested) {
            logger.error("Closing... received message is {}, but cannot reply", _message);
            return false;
        }
        if(session == null) {
            logger.info("Session is null");
            return false;
        }

        try {
            Future<Void> fut;
            fut = session.getRemote().sendStringByFuture(_message);
            fut.get(timeoutToSendMessage,TimeUnit.MILLISECONDS); // wait for send to complete.
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
}
