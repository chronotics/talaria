package org.chronotics.talaria.websocket.jetty;

import com.google.common.primitives.Ints;
import org.chronotics.talaria.common.CallableExecutor;
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

public class JettyListener implements WebSocketListener {

    public static String KEY_ID = "id";
    public static String KEY_GROUPID = "groupId";

    protected CallableExecutor<String> stringExecutor = null;
    protected CallableExecutor<byte []> bytesExecutor = null;

    protected JettyListenerAction closeAction = null;
    protected JettyListenerAction connectAction = null;
    protected JettyListenerAction binaryAction = null;
    protected JettyListenerAction textAction = null;
    protected JettyListenerAction errorAction = null;

    protected JettyServer server = null;
    protected Session session = null;
    private String id = null;
    private String groupId = null;

    public JettyServer getServer() {
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

        if(server!=null) {
            if(server.addSession(session, getGroupId(), getId()) == false) {
                session.close();
            } else {
                setSession(session);
            }
        }

        if(connectAction!=null) {
            connectAction.execute(this, session);
        }
    }

    /**
     * You hava to add "super.onWebSocketClose" in a derived class
     * @param i
     * @param s
     */
    @Override
    public void onWebSocketClose(int i, String s) {
        if(server!=null && this.session!=null) {
            server.removeSession(session, getGroupId(), getId());
        }
        if(session!=null) {
            this.session.close();
        }
        this.session = null;

        if(closeAction!=null) {
            closeAction.execute(this, session, String.valueOf(i));
        }
    }

    @Override
    public void onWebSocketError(Throwable var1) {
        if(errorAction!=null) {
            errorAction.execute(this, var1);
        }
    }

    @Override
    public void onWebSocketBinary(byte[] bytes, int i, int i1) {
        if(binaryAction!=null) {
            binaryAction.execute(
                    this,
                    bytes,
                    Ints.toByteArray(i),
                    Ints.toByteArray(i1));
        }
    }

    @Override
    public void onWebSocketText(String s) {
        if(textAction!=null) {
            textAction.execute(this, s);
        }
    }

    protected void setStringExecutor(CallableExecutor<String> _executor) {
        stringExecutor = _executor;
    }

    protected CallableExecutor<String> getStringExecutor() {
        return stringExecutor;
    }

    protected void setBytesExecutor(CallableExecutor<byte []> _executor) {
        bytesExecutor = _executor;
    }

    protected CallableExecutor<byte []> getBytesExecutor() {
        return bytesExecutor;
    }

    public JettyListenerAction getCloseAction() {
        return closeAction;
    }

    public void setCloseAction(JettyListenerAction closeAction) {
        this.closeAction = closeAction;
    }

    public JettyListenerAction getConnectAction() {
        return connectAction;
    }

    public void setConnectAction(JettyListenerAction connectAction) {
        this.connectAction = connectAction;
    }

    public JettyListenerAction getBinaryAction() {
        return binaryAction;
    }

    public void setBinaryAction(JettyListenerAction binaryAction) {
        this.binaryAction = binaryAction;
    }

    public JettyListenerAction getTextAction() {
        return textAction;
    }

    public void setTextAction(JettyListenerAction textAction) {
        this.textAction = textAction;
    }

    public JettyListenerAction getErrorAction() {
        return errorAction;
    }

    public void setErrorAction(JettyListenerAction errorAction) {
        this.errorAction = errorAction;
    }

}
