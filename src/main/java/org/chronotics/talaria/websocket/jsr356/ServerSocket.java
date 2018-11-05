package org.chronotics.talaria.websocket.jsr356;

import org.springframework.context.annotation.Configuration;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@Configuration
@ServerEndpoint("/hello")
public class ServerSocket {

    Session session = null;

    private void sendMessage(String _str) {
        assert(session!=null);
        try {
            session.getBasicRemote().sendText(_str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session _session) {
        assert(_session!=null);
        session = _session;
        System.out.println("Websocket opened: " + session.getId());
    }

    @OnMessage
    public void onMessage(String _msg, Session _session) {
        // to reply
        sendMessage("pong");
    }

    @OnClose
    public void onClose(CloseReason _reason, Session _session) {
        System.out.println("Closing a websocket due to " +
        _reason.getReasonPhrase());

        try {
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
