package org.chronotics.talaria.websocket.jetty.taskexecutor;

import org.chronotics.talaria.common.*;
import org.chronotics.talaria.websocket.jetty.JettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class MQToClient<T> extends TaskExecutor {

    private static final Logger logger =
            LoggerFactory.getLogger(MQToClient.class);

    public enum KIND_OF_RECIEVER {
        EACH_CLIENT,
        ALL_CLIENTS,
        GROUP
    };

    private KIND_OF_RECIEVER kindOfReceiver;
    public void setKindOfReceiver(KIND_OF_RECIEVER _kindOfReciever) {
        kindOfReceiver = _kindOfReciever;
    }

    private static int futureTimeout = 2000;

    public final static String PROPERTY_JETTYSERVER = "jettyServer";
    public final static String PROPERTY_ID = "id";

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

    public MQToClient(KIND_OF_RECIEVER _kindOfReceiver) {
        observer = new ObserverImp<T>();
        observer.setExecutor(this);
        kindOfReceiver = _kindOfReceiver;
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

        T value = mq.removeFirst();
        if(value == null) {
            logger.error("A message of MessageQueue is null");
            return null;
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

    private void sendMessage(JettyServer _server, Object _value, String _id) {
        switch(kindOfReceiver) {
            case EACH_CLIENT:
                _server.sendMessageToClient(_value,_id);
                break;
            case ALL_CLIENTS:
                _server.sendMessageToAllClients(_value);
                break;
            case GROUP:
                _server.sendMessageToGroup(_value,_id);
                break;
            default:
                logger.error("unsupported receiver type");
                break;
        }
    }
}
