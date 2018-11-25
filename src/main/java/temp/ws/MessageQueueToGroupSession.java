package temp.ws;

import org.chronotics.talaria.common.*;
import org.chronotics.talaria.websocket.jetty.JettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class MessageQueueToGroupSession<T> extends TaskExecutor {

    private static final Logger logger =
            LoggerFactory.getLogger(MessageQueueToGroupSession.class);

    private static int futureTimeout = 2000;

    public final static String PROPERTY_JETTYSERVER = "jettyServer";
    public final static String PROPERTY_ID = "id";
//    public final static String PROPERTY_SESSIONGROUP = "sessionGroup";

    private class ObserverImp<T> implements Observer {
        TaskExecutor<T> executor = null;
        public void setExecutor(TaskExecutor _executor) {
            executor = _executor;
        }
        @Override
        public void update(Observable _observable, Object _object) {
            if(_object instanceof String &&
                    _object.equals(MessageQueue.REMOVAL_NOTIFICATION)) {
                return;
            }
            try {
                executor.execute((T)_object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ObserverImp observer = null;

    public ObserverImp<T> getObserver() {
        return observer;
    }

    public MessageQueueToGroupSession() {
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
            logger.error("MessageQueue id is not defined as a property");
            return null;
        }

//        Session session = (Session)(this.getProperty(PROPERTY_SESSION));
//        assert(session != null);
//        if (session == null) {
//            logger.error("Session is not defined as a property");
//            return null;
//        }

        JettyServer server = (JettyServer)this.getProperty(PROPERTY_JETTYSERVER);
        assert(server != null);
        if(server == null) {
            logger.error("JettyServer is not defined as a property");
        }

        MessageQueueMap mqMap = MessageQueueMap.getInstance();
        MessageQueue<T> mq = (MessageQueue<T>) mqMap.get(id);
        if(mq == null) {
            logger.error("MessageQueue is null. Check the correct Id");
            return null;
        }

        T value = mq.removeFirst();
        if(value == null) {
            logger.error("A message of MessageQueue is null");
            return null;
        }

        if (value instanceof Collection) {
            Collection<T> c = (Collection<T>) value;
            for (T v : c) {
                server.sendMessageToGroup(v,id);
//                if(!session.isOpen()) {
//                    logger.error("session is not opened");
//                    continue;
//                }
//                Future<Void> future =
//                        JettySessionCommon.sendMessage(session, v);
//                future.get();
            }
        } else {
            server.sendMessageToGroup(value,id);
//            if(!session.isOpen()) {
//                logger.error("session is not opened");
//            }
//            Future<Void> future =
//                    JettySessionCommon.sendMessage(session, value);
//            future.get();
        }

        return null;
    }
}
