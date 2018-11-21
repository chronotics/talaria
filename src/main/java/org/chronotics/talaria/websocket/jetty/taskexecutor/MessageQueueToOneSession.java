package org.chronotics.talaria.websocket.jetty.taskexecutor;

import org.chronotics.talaria.common.*;
import org.chronotics.talaria.websocket.jetty.JettySessionCommon;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.Future;

public class MessageQueueToOneSession<T> extends TaskExecutor {

    private static final Logger logger =
            LoggerFactory.getLogger(MessageQueueToOneSession.class);

    private static int futureTimeout = 2000;

    public final static String PROPERTY_MQID = "mqId";
    public final static String PROPERTY_SESSION = "session";

    private class MessageQueueObserver<T> implements Observer {
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

    private MessageQueueObserver observer = null;

    public MessageQueueObserver<T> getMessageQueueObserver() {
        return observer;
    }

    public MessageQueueToOneSession() {
        observer = new MessageQueueObserver<T>();
        observer.setExecutor(this);
    }

    @Override
    public int getFutureTimeout() {
        return futureTimeout;
    }

    @Override
    public T call() throws Exception {
        String mqId = (String) this.getProperty(PROPERTY_MQID);
        Session session = (Session)(this.getProperty(PROPERTY_SESSION));

        assert(mqId != null);
        if (mqId == null) {
            logger.error("MessageQueue id is not defined as a property");
            return null;
        }

        assert(session != null);
        if (session == null) {
            logger.error("Session is not defined as a property");
            return null;
        }

        MessageQueueMap mqMap = MessageQueueMap.getInstance();
        MessageQueue<T> mq = (MessageQueue<T>) mqMap.get(mqId);
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
                if(!session.isOpen()) {
                    logger.error("session is not opened");
                    continue;
                }
                Future<Void> future =
                        JettySessionCommon.sendMessage(session, v);
                future.get();
            }
        } else {
            if(!session.isOpen()) {
                logger.error("session is not opened");
            }
            Future<Void> future =
                    JettySessionCommon.sendMessage(session, value);
            future.get();
        }

        return null;
    }
}
