package org.chronotics.talaria.websocket.jetty;

import com.google.common.primitives.Ints;
import org.chronotics.talaria.common.TObserver;
import org.chronotics.talaria.common.TaskExecutor;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
//import rx.Observer;

import javax.annotation.Nullable;
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

//    private Observer observer = null;
    private TObserver observer = null;

//    protected TaskExecutor<String> stringExecutor = null;
//    protected TaskExecutor<byte []> bytesExecutor = null;

    private JettyListenerActionProvider connectActionProvider = null;
    private JettyListenerActionProvider closeActionProvider = null;
    private JettyListenerActionProvider textActionProvider = null;
    private JettyListenerActionProvider binaryActionProvider = null;
    private JettyListenerActionProvider errorActionProvider = null;

    protected JettyServer server = null;
    protected Session session = null;
    private String id = null;
    private String groupId = null;

    @Nullable
//    public Observer getObserver() {
//        return observer;
//    }
    public TObserver getObserver() {
        return observer;
    }

//    public void setObserver(Observer observer) {
//        this.observer = observer;
//    }
    public void setObserver(TObserver observer) {
        this.observer = observer;
    }

    public JettyServer getServer() {
        return server;
    }

    public void setServer(JettyServer server) {
        this.server = server;
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

        if(connectActionProvider!=null) {
            connectActionProvider.executeActions(
                    this,
                    session);
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

        if(closeActionProvider!=null) {
            closeActionProvider.executeActions(
                    this,
                    session,
                    String.valueOf(i));
        }
    }

    @Override
    public void onWebSocketError(Throwable var1) {
        if(errorActionProvider!=null) {
            errorActionProvider.executeActions(
                    this,
                    var1);
        }
    }

    @Override
    public void onWebSocketBinary(byte[] bytes, int i, int i1) {
        if(binaryActionProvider!=null) {
            binaryActionProvider.executeActions(
                    this,
                    bytes,
                    Ints.toByteArray(i),
                    Ints.toByteArray(i1));
        }
    }

    @Override
    public void onWebSocketText(String s) {
        if(textActionProvider!=null) {
            textActionProvider.executeActions(
                    this,
                    s);
        }
    }

//    protected void setStringExecutor(TaskExecutor<String> executor) {
//        stringExecutor = executor;
//    }
//
//    protected TaskExecutor<String> getStringExecutor() {
//        return stringExecutor;
//    }
//
//    protected void setBytesExecutor(TaskExecutor<byte []> executor) {
//        bytesExecutor = executor;
//    }
//
//    protected TaskExecutor<byte []> getBytesExecutor() {
//        return bytesExecutor;
//    }

    public JettyListenerActionProvider getConnectActionProvider() {
        return connectActionProvider;
    }

    public void setConnectActionProvider(JettyListenerActionProvider connectActionProvider) {
        this.connectActionProvider = connectActionProvider;
    }

    public JettyListenerActionProvider getCloseActionProvider() {
        return closeActionProvider;
    }

    public void setCloseActionProvider(JettyListenerActionProvider closeActionProvider) {
        this.closeActionProvider = closeActionProvider;
    }

    public JettyListenerActionProvider getTextActionProvider() {
        return textActionProvider;
    }

    public void setTextActionProvider(JettyListenerActionProvider textActionProvider) {
        this.textActionProvider = textActionProvider;
    }

    public JettyListenerActionProvider getBinaryActionProvider() {
        return binaryActionProvider;
    }

    public void setBinaryActionProvider(JettyListenerActionProvider binaryActionProvider) {
        this.binaryActionProvider = binaryActionProvider;
    }

    public JettyListenerActionProvider getErrorActionProvider() {
        return errorActionProvider;
    }

    public void setErrorActionProvider(JettyListenerActionProvider errorActionProvider) {
        this.errorActionProvider = errorActionProvider;
    }

}
