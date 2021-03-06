package org.chronotics.talaria.websocket.jetty.taskexecutor;

import org.chronotics.talaria.common.*;
import org.chronotics.talaria.websocket.jetty.JettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import rx.Observer;

import java.util.Collection;

public class MQToClient<T> extends TaskExecutor {

    private static final Logger logger =
            LoggerFactory.getLogger(MQToClient.class);

    public enum KIND_OF_RECIEVER {
        EACH_CLIENT,
        ALL_CLIENTS,
        GROUP
    };

    private boolean isMQElementRemoval = false;

    private KIND_OF_RECIEVER kindOfReceiver;
    public void setKindOfReceiver(KIND_OF_RECIEVER _kindOfReciever) {
        kindOfReceiver = _kindOfReciever;
    }

    private static int futureTimeout = 2000;

    public final static String PROPERTY_JETTYSERVER = "jettyServer";
    public final static String PROPERTY_ID = "id";

//    private class ObserverImp<T> implements Observer<T> {
    private class ObserverImp<T> implements TObserver<T> {
        TaskExecutor<T> executor = null;
        public void setExecutor(TaskExecutor executor) {
            this.executor = executor;
        }
        @Override
        public void update(TObservable<T> observable, T object) {
            if(object instanceof String &&
                    object.equals(MessageQueue.REMOVAL_NOTIFICATION)) {
                return;
            }
            try {
                executor.execute((T)object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        @Override
//        public void onCompleted() {
//
//        }
//
//        @Override
//        public void onError(Throwable throwable) {
//
//        }
//
//        @Override
//        public void onNext(T t) {
////            if(t instanceof String &&
////                    t.equals(MessageQueue.REMOVAL_NOTIFICATION)) {
////                return;
////            }
//            try {
//                executor.execute(t);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }

    private ObserverImp observer = null;

    public ObserverImp<T> getObserver() {
        return this.observer;
    }

    public MQToClient(KIND_OF_RECIEVER kindOfReceiver,
                      boolean isMQElementRemoval) {
        this.observer = new ObserverImp<T>();
        this.observer.setExecutor(this);
        this.kindOfReceiver = kindOfReceiver;
        this.isMQElementRemoval = isMQElementRemoval;
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

        if(isMQElementRemoval) {
            T value = mq.removeFirst();
            if (value == null) {
                logger.error("A message of MessageQueue is null");
                return null;
            }
        }

        if (value instanceof Collection) {
            Collection<T> c = (Collection<T>) value;
            for (T v: c) {
                sendMessage(server, v, id);
            }
        } else {
            sendMessage(server, value, id);
        }

        return null;
    }

    private void sendMessage(JettyServer server, Object value, String id) {
        switch(kindOfReceiver) {
            case EACH_CLIENT:
                server.sendMessageToClient(value,id);
                break;
            case ALL_CLIENTS:
                server.sendMessageToAllClients(value);
                break;
            case GROUP:
                server.sendMessageToGroup(value,id);
                break;
            default:
                logger.error("unsupported receiver type");
                break;
        }
    }
}
