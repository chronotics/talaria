package org.chronotics.talaria.websocket.jetty;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class JettyClient {

    private static final Logger logger =
            LoggerFactory.getLogger(JettyClient.class);

    private String url;
    private Class handlerClass;
    private WebSocketClient client = null;
    private AbstractClientHandler handler = null;
    private Session session = null;

    public static long delayTimeToStop = 100; // ms

    public JettyClient(String _url, Class _handlerClass) {
        url = _url;
        handlerClass = _handlerClass;
        try {
            _handlerClass.asSubclass(AbstractClientHandler.class);
        } catch (ClassCastException e){
            e.printStackTrace();
            throw e;
        }
    }

    private WebSocketClient getClient() {
        return client;
    }

    public AbstractClientHandler getHandler() {
        return handler;
    }

    private Session getSession() {
        return session;
    }

    public void start() {
        start_();
        handler.await();
        stop();
    }

    public void start(int _duration, TimeUnit _unit) {
        start_();
        try {
            handler.awaitClose(_duration, _unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stop();
    }

    public void start_() {
        if (client == null) {
            client = new WebSocketClient();
        }

        if(handler == null) {
            try {
                handler = (AbstractClientHandler)handlerClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if(!client.isStarting() || !client.isStarted()) {
            try {
                client.start();
                URI echoUri = new URI(url);
                ClientUpgradeRequest request = new ClientUpgradeRequest();
                Future<Session> future = client.connect(handler, echoUri, request);
                session = future.get();
                handler.setSession(session);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private void stop_() {
//        assert(handler!=null);
        if(handler==null) {
            logger.error("socket is null");
        } else {
            handler.stop();
        }
    }

    public void stop() {
//        assert(client!=null);
        if(client==null) {
            logger.error("client is null");
        } else {
            try {
                client.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // no meaning, because client.stop() invoke onClose() of a handler
//        stop_();

        while(!isStopped() && !handler.isBusy()) {
            try {
                Thread.sleep(delayTimeToStop);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConnected() {
        if(client == null) {
            return false;
        }
        return session != null ? true: false;
    }

    public boolean isOpen() {
        if(session == null) {
            return false;
        }
        return this.session.isOpen();
    }

    public boolean isStarting() {
        if(client == null) {
            return false;
        }
        return this.client.isStarting();
    }

    public boolean isStarted() {
        if(client == null) {
            return false;
        }
        return this.client.isStarted();
    }

    public boolean isStopping() {
        if(client == null) {
            return false;
        }
        return this.client.isStopping();
    }

    public boolean isStopped() {
        if(client == null) {
            return false;
        }
        return this.client.isStopped();
    }

    public boolean isBusy() {
        if(handler == null) {
            return false;
        }
//        if(isStopped()) {
//            return false;
//        }
        return handler.isBusy();
    }
}
