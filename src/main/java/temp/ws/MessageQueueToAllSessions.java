package temp.ws;

import org.chronotics.talaria.common.*;
import org.chronotics.talaria.websocket.jetty.JettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class MessageQueueToAllSessions<T> extends TaskExecutor {

    private static final Logger logger =
            LoggerFactory.getLogger(MessageQueueToAllSessions.class);

    private static int futureTimeout = 1000;

    public final static String PROPERTY_JETTYSERVER = "jettyServer";
    public final static String PROPERTY_ID = "id";
//    public final static String PROPERTY_SESSION = "session";

    private static class ObserverImp<T> implements Observer {
        TaskExecutor<T> executor = null;
        public void setExecutor(TaskExecutor _executor) {
            executor = _executor;
        }
        @Override
        public void update(Observable observable, Object o) {
            if(o instanceof String &&
                    o.equals(MessageQueue.REMOVAL_NOTIFICATION)) {
                return;
            }
            try {
                executor.execute((T)o);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static ObserverImp observer = null;

    public Observer getObserver() {
        return observer;
    }

    public MessageQueueToAllSessions() {
        observer = new ObserverImp<T>();
        observer.setExecutor(this);
    }

    @Override
    public int getFutureTimeout() {
        return futureTimeout;
    }

    @Override
    public T call() throws Exception {
        String id = (String) this.getProperty(PROPERTY_ID);
        assert(id != null);
        if (id == null) {
            logger.error("MessageQueue's id is not defined as a property");
            return null;
        }

//        Set<Session> sessions = (Set<Session>) this.getProperty(PROPERTY_SESSION);
//        assert(sessions != null);
//        if (sessions == null) {
//            logger.error("List<Session> is not defined as a property");
//            return null;
//        }

        JettyServer server = (JettyServer)this.getProperty(PROPERTY_JETTYSERVER);
        assert(server != null);
        if(server == null) {
            logger.error("JettyServer is not defined as a property");
        }

        MessageQueueMap mqMap = MessageQueueMap.getInstance();
        MessageQueue<T> mq = (MessageQueue<T>) mqMap.get(id);

        T value = mq.removeFirst();
        if(value == null) {
            logger.error("A message of MessageQueue is null");
            return null;
        }

        if (value instanceof Collection) {
            Collection<T> c = (Collection<T>) value;
            for (T v : c) {
                server.sendMessageToAllClients(v);
//                for(Session session: sessions) {
//                    if(session == null) {
//                        logger.error("session is null");
//                        continue;
//                    }
//                    if(!session.isOpen()) {
//                        logger.error("session is not opened");
//                        continue;
//                    }
//                    Future<Void> future =
//                            JettySessionCommon.sendMessage(session, v);
//                    future.get();
//                }
            }
        } else {
            server.sendMessageToAllClients(value);
//            for(Session session: sessions) {
//                if(session == null) {
//                    logger.error("session is null");
//                    continue;
//                }
//                if(!session.isOpen()) {
//                    logger.error("session is not opened");
//                    continue;
//                }
//                Future<Void> future =
//                        JettySessionCommon.sendMessage(session, value);
//                future.get();
//            }
        }

        return null;
    }
}
