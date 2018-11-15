package org.chronotics.talaria.websocket.jetty.taskexecutor;

import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.chronotics.talaria.common.TaskExecutor;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Future;

public class MessageQueueToSessions<T> extends TaskExecutor {

    private static final Logger logger =
            LoggerFactory.getLogger(MessageQueueToSessions.class);

    private static int futureTimeout = 1000;

    public static String PROPERTY_MQID = "mqId";
    public static String PROPERTY_SESSION = "session";

    private static class MessageQueueObserver<T> implements Observer {
        TaskExecutor<T> executor = null;
        public void setExecutor(TaskExecutor _executor) {
            executor = _executor;
        }
        @Override
        public void update(Observable observable, Object o) {
            if(o instanceof String && o.equals(MessageQueue.REMOVALMESSAGE)) {
                return;
            }
            try {
                executor.execute((T)o);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static MessageQueueObserver observer = null;

    public MessageQueueObserver<T> getMessageQueueObserver() {
        return observer;
    }

    public MessageQueueToSessions() {
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
        Set<Session> sessions = (Set<Session>) this.getProperty(PROPERTY_SESSION);

        if (mqId == null) {
            logger.error("MessageQueue id is not defined as a property");
            return null;
        }

        if (sessions == null) {
            logger.error("List<Session> is not defined as a property");
            return null;
        }

        MessageQueueMap mqMap = MessageQueueMap.getInstance();
        MessageQueue<T> mq = (MessageQueue<T>) mqMap.get(mqId);

        T value = mq.removeFirst();
        if(value == null) {
            logger.error("A message of MessageQueue is null");
            return null;
        }

        if (value instanceof Collection) {
            Collection<T> c = (Collection<T>) value;
            for (T v : c) {
                for(Session session: sessions) {
                    if(session == null) {
                        logger.error("session is null");
                        continue;
                    }
                    if(!session.isOpen()) {
                        logger.error("session is not opened");
                        continue;
                    }
                    Future<Void> future = sendMessage(session, v);
                    future.get();
                }
            }
        } else {
            for(Session session: sessions) {
                if(session == null) {
                    logger.error("session is null");
                    continue;
                }
                if(!session.isOpen()) {
                    logger.error("session is not opened");
                    continue;
                }
                Future<Void> future = sendMessage(session, value);
                future.get();
            }
        }

        return null;
    }

    private static Future<Void> sendMessage(Session _session, Object _value) {
        Future<Void> future = null;
        if(_value instanceof String) {
            future = _session.getRemote().sendStringByFuture((String)_value);
//            logger.info("{} is sent to CLients",_value);
        } else if (_value instanceof byte[]) {
            // chekc ByteBuffer.wrap
//            ret = session.getRemote().sendBytesByFuture(ByteBuffer.wrap((byte[])value));
        } else {
            logger.error("Unsupported data type");
            return null;
        }

        return future;
    }
}
