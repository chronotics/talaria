package org.chronotics.talaria.websocket.jsr356;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@ClientEndpoint
public class Jsr356ClientExample {

    private static final Logger logger =
            LoggerFactory.getLogger(Jsr356ClientExample.class);

    private String url;
    private CountDownLatch latch = null;
    private Session session = null;

    public void sendMessage(String _str) {
        assert(session!=null);
        try {
            session.getBasicRemote().sendText(_str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session _session) {
        setSession(_session);
    }

    @OnMessage
    public void onText(String _message, Session _session) throws IOException {

    }

    @OnClose
    public void onClose(CloseReason _reason, Session _session) {
        assert(session==_session);

        try {
            _session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.stop();
    }

    public boolean awaitClose(int duration, TimeUnit unit)
            throws InterruptedException {
        // wait during duration
        logger.info("awaitClose, duration is {}", duration);
        return this.latch.await(duration,unit);
    }

    private void setSession(Session _session) {
        this.session = _session;
        if(latch!=null) {
            latch.countDown();
        }
        latch = new CountDownLatch(1);
    }

    public void stop() {
        if(latch == null) return;
        latch.countDown();
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
}
