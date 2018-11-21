package org.chronotics.talaria.websocket.jetty;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author SG Lee
 * @since 2013
 * @description
 * The class of JettyClient is designed to use Jetty's WebSocketClient, comfortably.
 * You have to set handlerClass of which class can be defined with @WebSocket annotation.
 * Most of all, you have to add super.onConnect(), onClose(), onMessage() in corresponding functions
 * This class will be stopped gracefully with the delay of delayToStop
 */
public class JettyClient {

    private static final Logger logger =
            LoggerFactory.getLogger(JettyClient.class);

    private String url;
    private Class handlerClass;
    private WebSocketClient client = null;
    private AbstractClientHandler handler = null;
    private Session session = null;

    public long delayToStop = 2000; // ms
    private final static long delayForIteration = 100; // ms

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

    public long getDelayToStop() {
        return delayToStop;
    }

    public void setDelayToStop(long _delay) {
        delayToStop = _delay;
    }

    public void start() {
        if(isStarted() || isStarted()) {
            logger.info("Client is already started");
            return;
        }
        start_();
        handler.await();
        stop();
    }

    /**
     *
     * @param _duration
     * Client will be stopped after _duration
     * @param _unit
     * TimeUnit
     */
    public void start(int _duration, TimeUnit _unit) {
        if(isStarted() || isStarted()) {
            logger.info("Client is already started");
            return;
        }
        start_();
        try {
            handler.awaitClose(_duration, _unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stop();
    }

    private void start_() {
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
        if(handler==null) {
            logger.error("socket is null");
        } else {
            handler.stop();
        }
    }

    public void stop() {
        if(client==null) {
            logger.error("client is null");
        } else {
            try {
                client.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ///////////////////////////////////////////////////////////////////
        // The blow hasno meaning,
        // because client.stop() invoke onClose() of a handler
//        stop_();
        ///////////////////////////////////////////////////////////////////

        long startTime = System.currentTimeMillis();
        while(!isStopped() && handler.isBusy()) {
            try {
                Thread.sleep(delayForIteration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long currTime = System.currentTimeMillis();
            if(currTime - startTime > delayToStop) {
                logger.info("Delay to stop() was over than the set value");
                break;
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
        return handler.isBusy();
    }
}
