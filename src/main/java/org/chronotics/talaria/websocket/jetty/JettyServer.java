package org.chronotics.talaria.websocket.jetty;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author SG Lee
 * @since 2013
 * @description
 * The class of JettyServer is thread safe for handling
 * "Handler" such as ContextHandler and WebSocketListener
 * "SessionSet" to send a message to clients
 */
public class JettyServer {

    private static final Logger logger =
            LoggerFactory.getLogger(JettyServer.class);

    /**
     * Option to create ServletContextHandler
     * The below are the same with the options of ServletContextHandler
     */
    public static final int SESSIONS = 1;
    public static final int SECURITY = 2;
    public static final int GZIP = 4;
    public static final int NO_SESSIONS = 0;
    public static final int NO_SECURITY = 0;

    private Server server = null;
    private int port = 8080;
    private int stopTimeout = 1000; // ms
    private Map<String,ServletContextHandler> contextHandlerMap = null;
    private Set<Session> sessionSet = null;
    private Object syncHandler = new Object();
    private Object syncSessionSet = new Object();

    public JettyServer(int _port) {
        setPort(_port);
        createServer();
//        contextHandlerMap = new ConcurrentHashMap<>();
        contextHandlerMap = new HashMap<>();

        ServletContextHandler handler;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getStopTimeout() {
        return stopTimeout;
    }

    public void setStopTimeout(int _stopTimeout) {
        stopTimeout = _stopTimeout;
    }

    public void createServer() {
        if (server == null) {
            server = new Server(port);
        }
    }

    private Server getServer() {
        return server;
    }

    public void start() {
        assert(server!=null);
        try {
            server.start();
            server.dump(System.err);
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        assert(server!=null);
        server.setStopTimeout(stopTimeout);
        try {
            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isStarted() {
        assert(server!=null);
        return server.isStarted();
    }

    public boolean isStarting() {
        assert(server!=null);
        return server.isStarting();
    }

    public boolean isStopping() {
        assert(server!=null);
        return server.isStopping();
    }

    public boolean isStopped() {
        assert(server!=null);
        return server.isStopped();
    }

    public boolean isFailed() {
        assert(server!=null);
        return server.isFailed();
    }

    public void setContextHandler(String _contextPath, int _option) {
        synchronized (syncHandler) {
            assert (server != null);
            ServletContextHandler contextHandler =
                    new ServletContextHandler(_option);
            contextHandler.setContextPath(_contextPath);
            server.setHandler(contextHandler);
            contextHandlerMap.clear();
            contextHandlerMap.put(_contextPath, contextHandler);
        }
    }

    public void setContextHandler(ServletContextHandler _contextHandler) {
        synchronized (syncHandler) {
            assert (server != null);
            server.setHandler(_contextHandler);
            contextHandlerMap.clear();
            contextHandlerMap.put(_contextHandler.getContextPath(), _contextHandler);
        }
    }

    public void addContextHandler(ServletContextHandler _contextHandler)
            throws Exception {
        synchronized (syncHandler) {
            assert (server != null);
            assert (_contextHandler != null);

            if (!server.getState().equals("STOPPED")) {
                logger.error("You can not add ContextHandler during server's running");
                return;
            }
//        // For runtime
//        if(!server.getState().equals("STOPPED")) {
//           this.stop();
//        }
//        int count = 0;
//        final int sleepTime = 10; // ms
//        while(!server.getState().equals("STOPPED")) {
//            try {
//                Thread.sleep(sleepTime);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            count++;
//            if(count * sleepTime > stopTimeout) {
//                logger.error("It takes long time to stop server");
//            }
//        }

            Map<String, ServletContextHandler> backupMap =
                    new ConcurrentHashMap<>();
            backupMap.putAll(contextHandlerMap);
            contextHandlerMap.clear();
            ////////////////////////////////////////////////////////////////////////
            Handler[] handlerArray = server.getHandlers();
            List<Handler> handlers = new ArrayList<>();
            // If you want to multiple handlers,
            // you have to use difference contextPath
            for (Handler handler : handlerArray) {
                ServletContextHandler contextHandler =
                        (ServletContextHandler) handler;
                if (contextHandlerMap.get(contextHandler.getContextPath()) != null) {
                    // recover
                    contextHandlerMap.clear();
                    contextHandlerMap.putAll(backupMap);
                    logger.error(
                            "You can not add the ServletContext " +
                                    "of which contextPath already exists");
                    throw new IllegalArgumentException(
                            "You can not add the ServletContext " +
                                    "of which contextPath already exists");
                    //                break;
                }
                contextHandlerMap.put(
                        contextHandler.getContextPath(),
                        contextHandler);
                handlers.add(handler);
            }

            if (contextHandlerMap.get(_contextHandler.getContextPath()) != null) {
                // recover
                contextHandlerMap.clear();
                contextHandlerMap.putAll(backupMap);
                logger.error(
                        "You can not add the ServletContext " +
                                "of which contextPath already exists");
                throw new IllegalArgumentException(
                        "You can not add the ServletContext " +
                                "of which contextPath already exists");
            }
            handlers.add(_contextHandler);

            HandlerList handlerList =
                    new HandlerList(handlers.stream()
                            .toArray(Handler[]::new));
            server.setHandler(handlerList);
            ////////////////////////////////////////////////////////////////////////

//        // For runtime
//        this.start();
//        while(!server.getState().equals("STARTED")) {
//            try {
//                Thread.sleep(sleepTime);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            count++;
//            if(count * sleepTime > stopTimeout) {
//                logger.error("It takes long time to stop server");
//            }
//        }

        }
    }

    /**
     * bind WebsocketListener to ServletContextHandler
     * URL will be like the below
     * /_contextPath/_listenerPathSpec
     * @param _contextPath
     * path of ServletContextHandler
     * @param _listenerId
     * _listenerId is used to create ServletHolder
     * @param _listenerClass
     * Class of WebSocketListener
     * @param _listenerPathSpec
     * path of WebSocketListener
     * @return
     */
    public boolean addWebSocketListener(
            String _contextPath,
            String _listenerId,
            Class _listenerClass,
            String _listenerPathSpec) {
        synchronized (syncHandler) {
            if (!server.getState().equals("STOPPED")) {
                logger.error("You can not add ContextHandler during server's running");
                return false;
            }

            ServletContextHandler contextHandler =
                    this.contextHandlerMap.get(_contextPath);
            if (contextHandler == null) {
                logger.error("can not find the ServletContextHandler with "
                        + _contextPath);
                return false;
            }
            contextHandler.addServlet(
                    new ServletHolder(
                            _listenerId,
                            new JettyWebSocketServlet(this, _listenerClass)),
                    _listenerPathSpec);
            return true;
        }
    }

    public void addSession(Session _session) {
        if(_session == null) {
            logger.error("The session you want to add is null");
            return;
        }
        synchronized (syncSessionSet) {
            if(sessionSet == null) {
                sessionSet = new HashSet<>();
            }
            if (sessionSet.contains(_session)) {
                assert (false);
                logger.error("duplicated session");
                return;
            }
            sessionSet.add(_session);
        }
    }

    public boolean removeSession(Session _session) {
        synchronized (syncSessionSet) {
            boolean ret = sessionSet.remove(_session);
            if (!ret) {
                logger.error("failed to remove session");
            }
            return ret;
        }
    }

    public synchronized Set<Session> getSessionSet() {
        synchronized (syncSessionSet) {
            return sessionSet;
        }
    }

    public void sendMessageToClients(Object _value) {
        Set<Session> copiedSessionSet;
        synchronized (syncSessionSet) {
            if (sessionSet == null) {
                return;
            }
            copiedSessionSet = new HashSet<>(sessionSet);
        }
        for (Session session : copiedSessionSet) {
            Future<Void> future =
                    JettySessionCommon.sendMessage(session, _value);
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
