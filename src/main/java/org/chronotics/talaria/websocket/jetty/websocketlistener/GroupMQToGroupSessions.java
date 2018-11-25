package org.chronotics.talaria.websocket.jetty.websocketlistener;

import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.chronotics.talaria.common.Observer;
import org.chronotics.talaria.websocket.jetty.JettyListener;
import org.chronotics.talaria.websocket.jetty.JettySessionCommon;
import org.chronotics.talaria.websocket.jetty.taskexecutor.MQToClient;
import temp.ws.MessageQueueToEachSession;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GroupMQToGroupSessions extends JettyListener {
    private static final Logger logger =
            LoggerFactory.getLogger(GroupMQToGroupSessions.class);

//    public static String KEY_ID = "id";
//    public static String KEY_GROUPID = "groupId";
    private Observer observer = null;

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

//    @Override
//    public String getId() {
//        List<String> parameterList =
//                JettySessionCommon.getParameterList(session, KEY_ID);
//        assert(parameterList!=null);
//        return parameterList==null? null : parameterList.get(0);
//    }
//
//    @Override
//    public String getGroupId() {
//        List<String> parameterList =
//                JettySessionCommon.getParameterList(session, KEY_GROUPID);
//        assert(parameterList!=null);
//        return parameterList==null? null : parameterList.get(0);
//    }

    @Override
    public void onWebSocketClose(int i, String s) {
        String id = getGroupId();
        assert(id != null);

        super.onWebSocketClose(i,s);

        MessageQueueMap mqMap = MessageQueueMap.getInstance();

        // wait until MQ flush
        MessageQueue mq = mqMap.get(id);
        if(mq == null) {
            // MQ can be removed by the other member of a group
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
        mq.removeObserver(this.observer);
        // remove MessageQueue from QueMap
        mqMap.remove(id);

        logger.info(getClass().getName()+"::onWebSocketClose");
    }

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);

        String id = getGroupId();
        assert(id != null && !id.equals(""));
        if(id == null || id.equals("")) {
            logger.error("Client session is not connected because of invalid Id");
            return;
        }

        // insert MessageQueue to QueMap
        MessageQueueMap mqMap = MessageQueueMap.getInstance();
        MessageQueue<Object> mq = (MessageQueue<Object>) mqMap.get(id);
        if(mq == null) {
            mq = new MessageQueue<>(
                    Object.class,
                    MessageQueue.default_maxQueueSize,
                    MessageQueue.OVERFLOW_STRATEGY.NO_INSERTION);
        }
        // mq is not unique because of mq is for multiple sessions
        mqMap.put(id, mq);
//        if(!mqMap.put(id, mq)) {
//            // observer is already added by the other member of a group
//            return;
//        }

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
