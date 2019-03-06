package org.chronotics.talaria.websocket.jetty.jettylisteneraction;

import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.chronotics.talaria.websocket.jetty.JettyListener;
import org.chronotics.talaria.websocket.jetty.JettyListenerAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateMessageQueue<T> implements JettyListenerAction {
    private static final Logger logger =
            LoggerFactory.getLogger(CreateMessageQueue.class);
    @Override
    public void execute(JettyListener listener, Object[] v) {
        // id is dynamic and it depends on a parameter of API
        // http://url/?id=***
        String id = listener.getId();
        assert(id != null && !id.equals(""));

        // insert MessageQueue to QueMap
        MessageQueueMap mqMap = MessageQueueMap.getInstance();
        MessageQueue<Object> mq = (MessageQueue<Object>) mqMap.get(id);
        // always null because getId() is unique
        assert(mq==null);
        if(mq == null) {
            mq = new MessageQueue<>(
                    Object.class,
                    MessageQueue.default_maxQueueSize,
                    MessageQueue.OVERFLOW_STRATEGY.NO_INSERTION);
            mqMap.put(id,mq);
        }

//        assert(mq.countSubscription() == 0);
        assert(mq.countObservers() == 0);

//        // add observer
//        MQToClient taskExecutor =
//                new MQToClient(
//                        MQToClient.KIND_OF_RECIEVER.ALL_CLIENTS,
//                        true);
//        taskExecutor.putProperty(MQToClient.PROPERTY_ID, id);
//        taskExecutor.putProperty(MQToClient.PROPERTY_JETTYSERVER, listener.getServer());
//        Observer observer = taskExecutor.getObserver();
//        listener.setObserver(observer);
//        mq.subscribe(observer);
    }
}
