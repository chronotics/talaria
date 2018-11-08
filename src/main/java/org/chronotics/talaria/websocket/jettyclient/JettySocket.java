package org.chronotics.talaria.websocket.jettyclient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is not finished yet, check again before the use
 * 2018.06.19
 * @author sglee
 *
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class JettySocket {
    private static final Logger logger =
            LoggerFactory.getLogger(JettySocket.class);
	private final CountDownLatch latch;

    @SuppressWarnings("unused")
    private Session session;

    private boolean isCloseRequested = false;

    public Session getSession() {
        return session;
    }

    public JettySocket() {
        isCloseRequested = false;
        this.latch = new CountDownLatch(1);
    }

    public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
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

    @OnWebSocketClose
    public void onClose(int statusCode, String reason)
    {
        isCloseRequested = true;

        logger.info("onClose...statusCode:{}, reason:{}",statusCode, reason);

        try {
            this.session.close();
            this.session = null;
        } catch (Exception e) {
            logger.error("error in sesseion.close. error is {}");
            e.printStackTrace();
        }

        logger.info("session.close()");

        this.latch.countDown(); // trigger latch
    }

    @OnWebSocketConnect
    public void onConnect(Session session)
    {
        this.session = session;
        logger.info("client is connected to {}", session.getRemoteAddress().getAddress().getHostAddress());
        try
        {
            Future<Void> fut;
            fut = session.getRemote().sendStringByFuture("ping");
            fut.get(2,TimeUnit.SECONDS); // wait for send to complete.

//            fut = session.getRemote().sendStringByFuture("Thanks for the conversation.");
//            fut.get(2,TimeUnit.SECONDS); // wait for send to complete.

//            session.close(StatusCode.NORMAL,"I'm done");
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        System.out.printf("Got msg: %s%n",msg);
        if(session == null) {
            logger.info("Session is null");
            return;
        }
        if(isCloseRequested) {
            logger.error("Closing... received message is {}, but cannot reply", msg);
            return;
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sendMessage(msg);
    }

    public boolean sendMessage(String msg) {
        if(session == null) {
            logger.info("Session is null");
            return false;
        }
        if(isCloseRequested) {
            logger.error("Closing... received message is {}, but cannot reply", msg);
            return false;
        }
        try
        {
            Future<Void> fut;
            fut = session.getRemote().sendStringByFuture("ping");
            fut.get(2,TimeUnit.SECONDS); // wait for send to complete.
            return true;
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            return false;
        }
    }
}
