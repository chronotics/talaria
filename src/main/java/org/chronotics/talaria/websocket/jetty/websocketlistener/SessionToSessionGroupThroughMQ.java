package org.chronotics.talaria.websocket.jetty.websocketlistener;

import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.chronotics.talaria.common.Observer;
import org.chronotics.talaria.websocket.jetty.JettyListener;
import org.chronotics.talaria.websocket.jetty.taskexecutor.MQToClient;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class SessionToSessionGroupThroughMQ extends JettyListener {
    private static final Logger logger =
            LoggerFactory.getLogger(SessionToSessionGroupThroughMQ.class);

    private Observer observer = null;

    public long delayTimeToRemoveObserverAndMq = 1000;
    public static long delayForIteration = 100;

    public long getDelayTimeToRemoveObserverAndMq() {
        return delayTimeToRemoveObserverAndMq;
    }

    public void setDelayTimeToRemoveObserverAndMq(long _delay) {
        this.delayTimeToRemoveObserverAndMq = _delay;
    }

    /**
     *
     * @param bytes
     * @param i
     * offset
     * @param i1
     * length
     */
    @Override
    public void onWebSocketBinary(byte[] bytes, int i, int i1) {
        String id = getGroupId();
        MessageQueueMap mqMap = MessageQueueMap.getInstance();
        MessageQueue mq = mqMap.get(id);
        byte[] copiedBytes = Arrays.copyOfRange(bytes,i,i+i1);
        mq.addLast(copiedBytes);
    }

    @Override
    public void onWebSocketText(String s) {
        String id = getGroupId();
        MessageQueueMap mqMap = MessageQueueMap.getInstance();
        MessageQueue mq = mqMap.get(id);
        mq.addLast(s);
        logger.info("received message is {}", s);
    }

    @Override
    public void onWebSocketClose(int i, String s) {
        super.onWebSocketClose(i,s);

        String id = getGroupId();
        assert(id != null && !id.equals(""));

        MessageQueueMap mqMap = MessageQueueMap.getInstance();
        // wait until MQ flush
        MessageQueue mq = mqMap.get(id);
        // MQ can be removed by the other member of a group
        if(mq != null) {
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
            mq.removeObserver(this.observer);
            // remove MessageQueue from QueMap
            mqMap.remove(id);
        }

        logger.info(getClass().getName()+"::onWebSocketClose");
    }

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);

        if(this.session == null) {
            return;
        }

        String id = getGroupId();
        assert(id != null && !id.equals(""));

        // insert MessageQueue to QueMap
        MessageQueueMap mqMap = MessageQueueMap.getInstance();
        MessageQueue<Object> mq = (MessageQueue<Object>) mqMap.get(id);
        // mq can be added by the other member of a group
        // The below code is for the first time insertion
        if(mq == null) {
            mq = new MessageQueue<>(
                    Object.class,
                    MessageQueue.default_maxQueueSize,
                    MessageQueue.OVERFLOW_STRATEGY.NO_INSERTION);
            mqMap.put(id, mq);
        }

        // observer can be added by the other member of a group
        // The below code is for the first time insertion
        if(mq.countObservers() == 0) {
            // add observer
            MQToClient taskExecutor =
                    new MQToClient(MQToClient.KIND_OF_RECIEVER.GROUP);
            taskExecutor.putProperty(MQToClient.PROPERTY_ID, id);
            taskExecutor.putProperty(MQToClient.PROPERTY_JETTYSERVER, getServer());
            this.observer = taskExecutor.getObserver();
            mq.addObserver(this.observer);
        }

        logger.info(getClass().getName()+"::onWebSocketConnect");
    }

    @Override
    public void onWebSocketError(Throwable throwable) {
        logger.error(getClass().getName()+"::onWebSocketError");
        logger.error(throwable.toString());
    }
}
