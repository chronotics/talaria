package org.chronotics.talaria.websocket.jetty.websocketlistener;

import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.chronotics.talaria.websocket.jetty.JettyListener;
import org.chronotics.talaria.websocket.jetty.JettySessionCommon;
import org.chronotics.talaria.websocket.jetty.taskexecutor.MessageQueueToOneSession;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MQGenByIdListener extends JettyListener {
    private static final Logger logger =
        LoggerFactory.getLogger(MQGenByIdListener.class);

    public static String KEYID = "id";

    public static long delayTimeToRemoveObserverAndMq = 100;

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
        // Session access first!
        List<String> parameterList =
                JettySessionCommon.getParameterList(session, KEYID);
        String mqId = parameterList.get(0);

        super.onWebSocketClose(i,s);
        logger.info(getClass().getName()+"::onWebSocketClose");

        MessageQueueMap mqMap = MessageQueueMap.getInstance();

        // wait until MQ flush
        MessageQueue mq = mqMap.get(mqId);
        while(!mq.isEmpty()) {
            try {
                Thread.sleep(delayTimeToRemoveObserverAndMq);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // remove observer
        mq.removeAllObservers();

        // remove MessageQueue from QueMap
        mqMap.remove(mqId);
    }

    @Override
    public void onWebSocketConnect(Session session) {
        List<String> parameterList =
                JettySessionCommon.getParameterList(session, KEYID);
        String mqId = parameterList.get(0);

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
        MessageQueueToOneSession<String> taskExecutor =
                new MessageQueueToOneSession<>();
        taskExecutor.putProperty(MessageQueueToOneSession.PROPERTY_MQID,mqId);
        taskExecutor.putProperty(MessageQueueToOneSession.PROPERTY_SESSION,session);
        mq.addObserver(taskExecutor.getMessageQueueObserver());

        super.onWebSocketConnect(session);
        logger.info(getClass().getName()+"::onWebSocketConnect");
    }

    @Override
    public void onWebSocketError(Throwable throwable) {
        logger.error(getClass().getName()+"::onWebSocketError");
        logger.error(throwable.toString());
    }
}
