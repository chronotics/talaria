package org.chronotics.talaria.websocket.jetty;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Math.abs;

/**
 * This class is for WebSocket Client
 * Use the annotation of @WebSocket above the derived class
 * and insert codes of super.onConnect(), super.onClose() in corresponding function
 * Since 2015
 * Written by SGlee
 */

public abstract class AbstractClientHandler {
    private static final Logger logger =
            LoggerFactory.getLogger(AbstractClientHandler.class);

    public static long timeoutToSendMessage = 2000; // ms

    private CountDownLatch latch = null;
    private boolean isCloseRequested;
    protected Session session = null;

    private long idleDuration = 5000; // ms
    private long lastAccessTime = 0;

    private AtomicLong numberOfReceivedMessage = new AtomicLong();

    public long getNumberOfReceivedMessage() {
        return numberOfReceivedMessage.get();
    }

    protected AbstractClientHandler() {
        isCloseRequested = false;
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

    public void stop() {
        if(latch == null) return;
        latch.countDown();
    }

    public void setIdleDuration(long _duration) {
        idleDuration = _duration;
    }

    public long getIdleDuration() {
        return idleDuration;
    }

    public Session getSession() {
        return session;
    }

    protected void setSession(Session session) {
        this.session = session;
        if(latch!=null) {
            latch.countDown();
        }
        latch = new CountDownLatch(1);
    }

    /**
     * You have to add "super.onClose()" in a derived class
     * @param statusCode
     * @param reason
     */
    protected void onClose(int statusCode, String reason) {
        isCloseRequested = true;
        logger.info("onClose...statusCode:{}, reason:{}", statusCode, reason);

        assert(session != null);
        assert(latch != null);

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
        lastAccessTime = System.currentTimeMillis();
    }

    /**
     * You have to add "super.onMessage()" in a derived class
     * @param string
     */
    protected void onMessage(String string) {
        lastAccessTime = System.currentTimeMillis();
        numberOfReceivedMessage.incrementAndGet();
    }

    public void setNumberOfReceivedMessage(long _value) {
        numberOfReceivedMessage.set(_value);
    }

    public boolean isBusy() {
        long currTime = System.currentTimeMillis();
        long duration = abs(currTime - lastAccessTime);
        return (duration < idleDuration) ? true : false;
    }
}
