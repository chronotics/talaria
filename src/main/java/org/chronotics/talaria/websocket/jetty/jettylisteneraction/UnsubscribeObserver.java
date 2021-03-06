package org.chronotics.talaria.websocket.jetty.jettylisteneraction;

import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.chronotics.talaria.common.TObserver;
import org.chronotics.talaria.websocket.jetty.JettyListener;
import org.chronotics.talaria.websocket.jetty.JettyListenerAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observer;

public class UnsubscribeObserver<T> implements JettyListenerAction<T> {
    private static final Logger logger =
            LoggerFactory.getLogger(UnsubscribeObserver.class);
    public long delayTimeToRemoveObserverAndMq = 1000;
    public long delayForIteration = 100;

    @Override
    public void execute(JettyListener listener, T... v) {
        String id = listener.getId();
        assert(id != null && !id.equals(""));

        MessageQueueMap mqMap = MessageQueueMap.getInstance();
        // wait until MQ flush
        MessageQueue mq = mqMap.get(id);
        // MQ is unique in the case of EachMQToEachSession
        if(mq == null) {
            throw new NullPointerException("MQ can not be found, check id's uniqueness");
        } else {
//            mq.stopAdd(true);
//            long startTime = System.currentTimeMillis();
//            while (!mq.isEmpty()) {
//                try {
//                    Thread.sleep(delayForIteration);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                long currTime = System.currentTimeMillis();
//                if (currTime - startTime > delayTimeToRemoveObserverAndMq) {
//                    logger.error("MessageQueue is not cleared within the given delay time");
//                    break;
//                }
//            }
//            Observer observer = listener.getObserver();
//            if(observer != null) {
//                mq.unSubscribe(observer);
//            }
//            listener.setObserver(null);
//            mq.stopAdd(false);

//            Observer observer = listener.getObserver();
            TObserver observer = listener.getObserver();
            if(observer != null) {
                mq.removeObserver(observer);
//                mq.unSubscribe(observer);
            }
        }
    }
}
