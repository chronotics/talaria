package org.chronotics.talaria.websocket.jetty.websocketlistener;

import org.chronotics.talaria.websocket.jetty.JettyListener;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmptyListener extends JettyListener {
    @Override
    public void onWebSocketBinary(byte[] bytes, int i, int i1) {
    }

    @Override
    public void onWebSocketText(String s) {
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getGroupId() {
        return null;
    }

    @Override
    public void onWebSocketClose(int i, String s) {
    }

    @Override
    public void onWebSocketConnect(Session session) {
    }

    @Override
    public void onWebSocketError(Throwable throwable) {
    }
}
