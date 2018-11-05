package org.chronotics.talaria.websocket.jsr356;

import javax.websocket.*;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@ClientEndpoint
public class ClientSocket {
    CountDownLatch latch = new CountDownLatch(1);
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
        this.session = session;
        latch.countDown();
    }

    @OnMessage
    public void onText(String _msg, Session _session) throws IOException {
        System.out.println("A message is received from server " + _msg);
        sendMessage("ping");
    }

    @OnClose
    public void onClose(CloseReason _reasion, Session _session) {
        System.out.println("Closing a websocket due to " +
                _reasion.getReasonPhrase());

        try {
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}
