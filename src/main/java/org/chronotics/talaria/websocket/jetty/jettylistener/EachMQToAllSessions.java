package org.chronotics.talaria.websocket.jetty.jettylistener;

import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.chronotics.talaria.common.TObserver;
import org.chronotics.talaria.websocket.jetty.JettyListener;
import org.chronotics.talaria.websocket.jetty.taskexecutor.MQToClient;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observer;

public class EachMQToAllSessions extends JettyListener {
    private static final Logger logger =
        LoggerFactory.getLogger(EachMQToAllSessions.class);

//    private Observer observer = null;

    public long delayTimeToRemoveObserverAndMq = 1000;
    public static long delayForIteration = 100;

    public long getDelayTimeToRemoveObserverAndMq() {
        return delayTimeToRemoveObserverAndMq;
    }

    public void setDelayTimeToRemoveObserverAndMq(long delay) {
        this.delayTimeToRemoveObserverAndMq = delay;
    }

    @Override
    public void onWebSocketBinary(byte[] bytes, int i, int i1) {
    }

    @Override
    public void onWebSocketText(String s) {
    }

    @Override
    public void onWebSocketClose(int i, String s) {
        super.onWebSocketClose(i,s);

        String id = getId();
        assert(id != null && !id.equals(""));

        MessageQueueMap mqMap = MessageQueueMap.getInstance();
        // wait until MQ flush
        MessageQueue mq = mqMap.get(id);
        // MQ is unique in the case of EachMQToEachSession
        if(mq == null) {
            throw new NullPointerException("MQ can not be found, check id's uniqueness");
        } else {
            mq.stopAdd(true);
            long startTime = System.currentTimeMillis();
            while (!mq.isEmpty()) {
                try {
                    Thread.sleep(delayForIteration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                long currTime = System.currentTimeMillis();
                if (currTime - startTime > delayTimeToRemoveObserverAndMq) {
                    logger.error("MessageQueue is not cleared within the given delay time");
                    break;
                }
            }
            // clear MQ by force
            mq.clear();
            // remove observer
            mq.removeObserver(this.getObserver());
//            mq.unSubscribe(this.getObserver());
            // remove MessageQueue from QueMap
            mqMap.remove(id);
        }

        logger.info(getClass().getName()+"::onWebSocketClose");
    }

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);

        // id is dynamic and it depends on a parameter of API
        // http://url/?id=***
        String id = getId();
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

        // observer must be unique
        assert(mq.countObservers()==0);
//        assert(mq.countSubscription() == 0);

        // add observer
        MQToClient taskExecutor =
                new MQToClient(
                        MQToClient.KIND_OF_RECIEVER.ALL_CLIENTS,
                        true);
        taskExecutor.putProperty(MQToClient.PROPERTY_ID, id);
        taskExecutor.putProperty(MQToClient.PROPERTY_JETTYSERVER, getServer());
        TObserver observer = taskExecutor.getObserver();
//        Observer observer = taskExecutor.getObserver();
        this.setObserver(observer);
        mq.addObserver(observer);
//        mq.subscribe(observer);

        logger.info(getClass().getName()+"::onWebSocketConnect");
    }

    @Override
    public void onWebSocketError(Throwable throwable) {
        logger.error(getClass().getName()+"::onWebSocketError");
        logger.error(throwable.toString());
    }
}
