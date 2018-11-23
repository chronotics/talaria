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

public class GroupMQToGroupSessions extends JettyListener {
    private static final Logger logger =
            LoggerFactory.getLogger(GroupMQToGroupSessions.class);

    public static String KEY_ID = "id";
    public static String KEY_GROUP = "group";

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
    public void onWebSocketClose(int i, String s) {
        super.onWebSocketClose(i,s);

        // Session access first!
//        List<String> paraListId =
//                JettySessionCommon.getParameterList(session, KEY_ID);
//        String mqId = paraListId.get(0);

        List<String> paraListGroup =
                JettySessionCommon.getParameterList(session, KEY_GROUP);
        String groupId = paraListGroup.get(0);

        MessageQueueMap mqMap = MessageQueueMap.getInstance();

        // wait until MQ flush
        MessageQueue mq = mqMap.get(groupId);
        if(mq == null) {
            return;
        }
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
        mqMap.remove(groupId);

        logger.info(getClass().getName()+"::onWebSocketClose");
    }

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);

//        List<String> paraListId =
//                JettySessionCommon.getParameterList(session, KEY_ID);
//        String mqId = paraListId.get(0);

        List<String> paraListGroup =
                JettySessionCommon.getParameterList(session, KEY_GROUP);

        String groupId = paraListGroup.get(0);
        assert(groupId != null && !groupId.equals(""));

        // insert MessageQueue to QueMap
        MessageQueueMap mqMap = MessageQueueMap.getInstance();
        MessageQueue<Object> mq = (MessageQueue<Object>) mqMap.get(groupId);
        if(mq == null) {
            mq = new MessageQueue<>(
                    Object.class,
                    MessageQueue.default_maxQueueSize,
                    MessageQueue.OVERFLOW_STRATEGY.NO_INSERTION);
        }
        if(!mqMap.put(groupId, mq)) {
            logger.error("duplicated client id exist");
            return;
        }

        // add observer
        MessageQueueToEachSession<String> taskExecutor =
                new MessageQueueToEachSession<>();
        taskExecutor.putProperty(MessageQueueToEachSession.PROPERTY_MQID,groupId);
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
