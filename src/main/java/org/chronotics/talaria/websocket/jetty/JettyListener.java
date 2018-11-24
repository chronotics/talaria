package org.chronotics.talaria.websocket.jetty;

import org.chronotics.talaria.common.TaskExecutor;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

/**
 * This class is for WebSocketListener of WebSocket Server
 * Use the annotation of @WebSocket above the derived class
 * and insert codes of super.onWebSocketConnect(),
 * super.onWebSocketClose() in corresponding function
 * Since 2015
 * Written by SGlee
 */

public abstract class JettyListener implements WebSocketListener {

    protected TaskExecutor<String> stringExecutor = null;
    protected TaskExecutor<byte []> bytesExecutor = null;

    protected JettyServer server = null;
    protected Session session = null;

    protected JettyServer getServer() {
        return server;
    }

    public void setServer(JettyServer _server) {
        server = _server;
    }

    public Session getSession() {
        return session;
    }

    protected void setSession(Session session) {
        this.session = session;
    }

    public abstract String getId();

    public abstract String getGroupId();

    /**
     * You hava to add "super.onWebSocketClose" in a derived class
     * @param i
     * @param s
     */
    @Override
    public void onWebSocketClose(int i, String s) {
        if(server!=null) {
            server.removeSession(session, getId(), getGroupId());
        }
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
        if(server!=null) {
            server.addSession(session, getId(), getGroupId());
        }
    }

    protected void setStringExecutor(TaskExecutor<String> _executor) {
        stringExecutor = _executor;
    }

    protected TaskExecutor<String> getStringExecutor() {
        return stringExecutor;
    }

    protected void setBytesExecutor(TaskExecutor<byte []> _executor) {
        bytesExecutor = _executor;
    }

    protected TaskExecutor<byte []> getBytesExecutor() {
        return bytesExecutor;
    }
}
