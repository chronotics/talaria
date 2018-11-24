package org.chronotics.talaria.websocket.jetty.websocketlistener;

import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.chronotics.talaria.websocket.jetty.JettyListener;
import org.chronotics.talaria.websocket.jetty.JettySessionCommon;
import org.chronotics.talaria.websocket.jetty.taskexecutor.MessageQueueToEachSession;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EachMQToEachSession extends JettyListener {
    private static final Logger logger =
        LoggerFactory.getLogger(EachMQToEachSession.class);

    public static String KEY_ID = "id";
    public static String KEY_GROUPID = "groupId";

    public long delayTimeToRemoveObserverAndMq = 1000;
    public static long delayForIteration = 100;

    public long getDelayTimeToRemoveObserverAndMq() {
        return delayTimeToRemoveObserverAndMq;
    }

    public void setDelayTimeToRemoveObserverAndMq(long _delay) {
        this.delayTimeToRemoveObserverAndMq = _delay;
    }

    @Override
    public void onWebSocketBinary(byte[] bytes, int i, int i1) {
//        logger.info(getClass().getName() +
//                " received a message of {} {} {} ", bytes, i, i1);
    }

    @Override
    public void onWebSocketText(String s) {
//        logger.info(getClass().getName() +
//                " received a message of {}", s);
    }

    @Override
    public String getId() {
        List<String> parameterList =
                JettySessionCommon.getParameterList(session, KEY_ID);
        assert(parameterList!=null);
        return parameterList==null? null : parameterList.get(0);
    }

    @Override
    public String getGroupId() {
        List<String> parameterList =
                JettySessionCommon.getParameterList(session, KEY_GROUPID);
        assert(parameterList!=null);
        return parameterList==null? null : parameterList.get(0);
    }

    @Override
    public void onWebSocketClose(int i, String s) {
        super.onWebSocketClose(i,s);

        // Session access first!
        String mqId = getId();
        assert(mqId != null);

        MessageQueueMap mqMap = MessageQueueMap.getInstance();

        // wait until MQ flush
        MessageQueue mq = mqMap.get(mqId);
        mq.stopAdd(true);
        long startTime = System.currentTimeMillis();
        while(!mq.isEmpty()) {
            try {
                Thread.sleep(delayForIteration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long currTime = System.currentTimeMillis();
            if(currTime - startTime > delayTimeToRemoveObserverAndMq) {
                logger.error("MessageQueue is not cleared within the given delay time");
                break;
            }
        }
        // clear MQ with force
        mq.clear();
        // remove observer
        mq.removeAllObservers();
        // remove MessageQueue from QueMap
        mqMap.remove(mqId);

        logger.info(getClass().getName()+"::onWebSocketClose");
    }

    @Override
    public void onWebSocketConnect(Session session) {
        List<String> parameterList =
                JettySessionCommon.getParameterList(session, KEY_ID);
        String mqId = parameterList.get(0);
        assert(mqId != null && !mqId.equals(""));
        if(mqId == null || mqId.equals("")) {
            logger.error("Client session is not connected because of invalid Id");
            return;
        }

        super.onWebSocketConnect(session);

        // insert MessageQueue to QueMap
        MessageQueueMap mqMap = MessageQueueMap.getInstance();
        MessageQueue<Object> mq =
                new MessageQueue<>(
                        Object.class,
                        MessageQueue.default_maxQueueSize,
                        MessageQueue.OVERFLOW_STRATEGY.NO_INSERTION);
        if(!mqMap.put(mqId, mq)) {
            logger.error("duplicated client id exist");
            return;
        }

        // add observer
        MessageQueueToEachSession<String> taskExecutor =
                new MessageQueueToEachSession<>();
        taskExecutor.putProperty(MessageQueueToEachSession.PROPERTY_MQID,mqId);
        taskExecutor.putProperty(MessageQueueToEachSession.PROPERTY_SESSION,session);
        mq.addObserver(taskExecutor.getObserver());

        logger.info(getClass().getName()+"::onWebSocketConnect");
    }

    @Override
    public void onWebSocketError(Throwable throwable) {
        logger.error(getClass().getName()+"::onWebSocketError");
        logger.error(throwable.toString());
    }
}
