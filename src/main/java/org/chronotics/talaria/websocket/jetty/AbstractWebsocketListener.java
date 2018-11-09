package org.chronotics.talaria.websocket.jetty;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

/**
 * Since 2015
 * Written by SGlee
 */

public abstract class AbstractWebsocketListener implements WebSocketListener {

    protected Session session;

    public Session getSession() {
        return session;
    }

    protected void setSession(Session session) {
        this.session = session;
    }

    /**
     * You hava to add "super.onWebSocketClose" in a derived class
     * @param i
     * @param s
     */
    @Override
    public void onWebSocketClose(int i, String s) {
        if(session!=null) {
            this.session.close();
        }
        this.session = null;
    }

    /**
     * You have to add "super.onWebSocketConnect()" in a derived class
     * @param session
     */
    @Override
    public void onWebSocketConnect(Session session) {
        setSession(session);
    }
}
