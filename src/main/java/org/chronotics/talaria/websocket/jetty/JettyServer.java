package org.chronotics.talaria.websocket.jetty;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Null;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
    // Id must be guaranteed uniqueness for the below
    private Map<String, Session> sessionMap = null;
    private Map<String, Map<String, Session>> sessionGroupMap = null;
    private Object syncHandler = new Object();
    private Object syncSessions = new Object();

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
            String _listenerPathSpec,
            Class _listenerClass,
            JettyListenerAction _listenerConnectAction,
            JettyListenerAction _listenerCloseAction,
            JettyListenerAction _listenerErrorAction,
            JettyListenerAction _listenerBinaryAction,
            JettyListenerAction _listenerTextAction) {
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
                            new JettyWebSocketServlet(
                                    this,
                                    _listenerClass,
                                    _listenerConnectAction,
                                    _listenerCloseAction,
                                    _listenerErrorAction,
                                    _listenerBinaryAction,
                                    _listenerTextAction)),
                    _listenerPathSpec);
            return true;
        }
    }

    public boolean addSession(Session _session, String _groupId, String _id) {
        if(_session == null) {
            logger.error("The session you want to add is null");
            return false;
        }
        synchronized (syncSessions) {
            if(sessionSet == null) {
                sessionSet = new HashSet<>();
            }
            assert(!sessionSet.contains(_session));
            if (sessionSet.contains(_session)) {
                assert (false);
                logger.error("duplicated session");
                return false;
            }
            sessionSet.add(_session);

            // JettyServer must handle client's request without "id"
            if(_id != null) {
                if (sessionMap == null) {
                    sessionMap = new HashMap<>();
                }
                Session sessionM = sessionMap.put(_id, _session);
                if (sessionM != null) {
                    logger.error("Session insertion failed, check duplicated id");
                    return false;
                }
            }

            // GroupId can be null, if a group is not defined
            // In this case, by the way, sessionGroupMap do not create(put) group.
            if(_groupId != null) {
                if (sessionGroupMap == null) {
                    sessionGroupMap = new HashMap<>();
                }
                Map<String, Session> group = sessionGroupMap.get(_groupId);
                if (group == null) {
                    group = new HashMap<>();
                    sessionGroupMap.put(_groupId, group);
                }
                Session sessionG = group.put(_id, _session);
                if (sessionG != null) {
                    logger.error("Session insertion failed, check duplicated groupId");
                    if (group.isEmpty()) {
                        sessionGroupMap.remove(group);
                    }
                    return false;
                }
            }
            return true;
        }
    }

    public boolean removeSession(Session _session, String _groupId, String _id) {
        synchronized (syncSessions) {
            boolean ret = sessionSet.remove(_session);
            if (!ret) {
                logger.error("failed to remove session");
            }

            if(sessionMap != null) {
                Session session = sessionMap.remove(_id);
                assert (session != null);
            }

            if(sessionGroupMap != null) {
                Map<String, Session> group = sessionGroupMap.get(_groupId);
                if (group != null) {
                    Session V = group.remove(_id);
                    if (V == null) {
                        logger.error("Session removal failed");
                        throw new NullPointerException(
                                "couldn't remove session from the group");
                    }
                    if (group.isEmpty()) {
                        sessionGroupMap.remove(group);
                    }
                }
            }

            return ret;
        }
    }

    public Set<Session> getSessionSet() {
        synchronized (syncSessions) {
            return sessionSet;
        }
    }

    public Map<String, Session> getSessionGroupMap(String _groupId) {
        synchronized (syncSessions) {
            if(sessionGroupMap == null) {
                throw new NullPointerException("sessionGroupMap is null");
            }
            return sessionGroupMap.get(_groupId);
        }
    }

    public void sendMessageToAllClients(Object _value) {
        Set<Session> copiedSessionSet;
        synchronized (syncSessions) {
            if (sessionSet == null) {
                logger.error("sessionSet is null");
                throw new NullPointerException("sessionSet is null");
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

    public void sendMessageToClient(Object _value, String _id) {
        Map<String, Session> copiedSessionMap;
        synchronized (syncSessions) {
            if(sessionMap == null) {
                logger.error("sessionMap is null");
                throw new NullPointerException("sessionMap is null");
            }
            copiedSessionMap = new HashMap<>(sessionMap);
        }
        Session session = copiedSessionMap.get(_id);
        if(session == null) {
            logger.error("session not found");
            throw new NullPointerException("session not found");
        }
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

    public void sendMessageToGroup(Object _value, String _groupId) {
        Map<String, Map<String, Session>> copiedSessionGroup;
        synchronized (syncSessions) {
            if(sessionGroupMap == null) {
                logger.error("sessionGroupMap is null");
                throw new NullPointerException("sessionGroupMap is null");
            }
            copiedSessionGroup = new HashMap<>(sessionGroupMap);
        }
        Map<String, Session> group = copiedSessionGroup.get(_groupId);
        if(group == null) {
            logger.error("group not found");
            throw new NullPointerException("group not found");
        }
        for (Map.Entry<String, Session> entry : group.entrySet()) {
            Future<Void> future =
                    JettySessionCommon.sendMessage(entry.getValue(), _value);
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
