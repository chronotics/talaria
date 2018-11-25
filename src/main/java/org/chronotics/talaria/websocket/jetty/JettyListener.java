package org.chronotics.talaria.websocket.jetty;

import org.chronotics.talaria.common.TaskExecutor;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import java.util.List;

/**
 * This class is for WebSocketListener of WebSocket Server
 * Use the annotation of @WebSocket above the derived class
 * and insert codes of super.onWebSocketConnect(),
 * super.onWebSocketClose() in corresponding function
 * Since 2015
 * Written by SGlee
 */

public abstract class JettyListener implements WebSocketListener {

    public static String KEY_ID = "id";
    public static String KEY_GROUPID = "groupId";

    protected TaskExecutor<String> stringExecutor = null;
    protected TaskExecutor<byte []> bytesExecutor = null;

    protected JettyServer server = null;
    protected Session session = null;
    private String id = null;
    private String groupId = null;

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

    public String getId() {
        return id;
    }

    public String getGroupId() {
        return groupId;
    }
    /**
     * You hava to add "super.onWebSocketClose" in a derived class
     * @param i
     * @param s
     */
    @Override
    public void onWebSocketClose(int i, String s) {
        if(server!=null) {
            server.removeSession(session, getGroupId(), getId());
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
        List<String> parameterListId =
                JettySessionCommon.getParameterList(session, KEY_ID);
        if(parameterListId == null) {
            id = null;
        } else {
            id = parameterListId.get(0);
        }
        List<String> parameterListGroupId =
                JettySessionCommon.getParameterList(session, KEY_GROUPID);
        if(parameterListGroupId == null) {
            groupId = null;
        } else {
            groupId = parameterListGroupId.get(0);
        }

        setSession(session);
        if(server!=null) {
            server.addSession(session, getGroupId(), getId());
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
