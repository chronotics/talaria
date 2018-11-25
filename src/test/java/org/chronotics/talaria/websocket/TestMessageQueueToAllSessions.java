package org.chronotics.talaria.websocket;

import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.chronotics.talaria.websocket.jetty.JettyClient;
import org.chronotics.talaria.websocket.jetty.JettyServer;
import org.chronotics.talaria.websocket.jetty.taskexecutor.MQToClient;
import org.chronotics.talaria.websocket.jetty.websocket.ClientHandlerExample;
import org.chronotics.talaria.websocket.jetty.websocketlistener.EmptyListener;
import org.chronotics.talaria.websocket.jetty.websocketlistener.GroupMQToGroupSessions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestMessageQueueToAllSessions {

    private static final Logger logger =
            LoggerFactory.getLogger(TestMessageQueueToAllSessions.class);

    private static String contextPath = "/";
    private static String topicId = "topic";
    private static String topicPath = "/topic/";
    private static String otherTopicId = "otherTopic";
    private static String otherTopicPath = "/otherTopic/";
    private static String id1 = "id1";
    private static String id2 = "id2";
    private static String id3 = "id3";
    private static String groupId = "group1";
    private static String otherTopicUrl1 = "ws://localhost:8080/otherTopic/?id="+id1;
    private static String otherTopicUrl2 = "ws://localhost:8080/otherTopic/?id="+id2;
    private static String otherTopicUrl3 = "ws://localhost:8080/otherTopic/?id="+id3;
    private static String topicUrl1 = "ws://localhost:8080/topic/?id="+id1+"&groupId="+groupId;
    private static String topicUrl2 = "ws://localhost:8080/topic/?id="+id2+"&groupId="+groupId;
    private static String topicUrl3 = "ws://localhost:8080/topic/?id="+id3+"&groupId="+groupId;
    private static int port = 8080;
    private static long awaitTimeOfClient = 1000; // ms
    private static long startUpTimeOfClient = 1500; // ms
    private static long startUpTimeOfServer = 1000; //ms
    private static long stopTimeoutOfServer = 1000; // ms

    private static JettyServer server = null;
//    private static String mqId = "testQueue";

    private static List<String> msgList = null;
    private static int msgListSize = 1000;
    private static long insertionTime = 1000;

    @BeforeClass
    public synchronized static void setup() {
        MessageQueueMap mqMap = MessageQueueMap.getInstance();
        if(!mqMap.isEmpty()) {
            mqMap.clear();
        }
//        MessageQueue<String> mq =
//                new MessageQueue<>(
//                        String.class,
//                        msgListSize,
////                                    MessageQueue.default_maxQueueSize,
//                        MessageQueue.OVERFLOW_STRATEGY.NO_INSERTION);
//        mqMap.put(mqId, mq);
//        mq.setRemovalNotification(true);

        msgList = new ArrayList<>();
        for(int i=0; i<msgListSize; i++) {
            msgList.add(String.valueOf(i));
        }
        insertionTime = Math.max(insertionTime, msgListSize / 1000);

        startServer();

    }

    @AfterClass
    public synchronized static void teardown() {
        stopServer();

        MessageQueueMap mqMap = MessageQueueMap.getInstance();
        mqMap.clear();

    }

    private static void startServer() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if(server == null) {
                    server = new JettyServer(port);
                    server.setContextHandler(contextPath, JettyServer.SESSIONS);
                    server.addWebSocketListener(
                            contextPath,
                            topicId,
                            GroupMQToGroupSessions.class,
                            topicPath);
                }

                if(server.isStopped()) {
                    server.start();
                }
            }
        });
    }

    private static void stopServer() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if(!server.isStopped()) {
                    server.stop();
                }
            }
        });
    }

    @Test
    public void sendMessageToMessageQueue() throws InterruptedException {
        // waiting for server start
        assertNotNull(server);
        Thread.sleep(startUpTimeOfServer);
        assertTrue(server.isStarting() || server.isStarted());

        JettyClient client1 = new JettyClient(topicUrl1, ClientHandlerExample.class);
        JettyClient client2 = new JettyClient(topicUrl2, ClientHandlerExample.class);
        JettyClient client3 = new JettyClient(topicUrl3, ClientHandlerExample.class);

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                client1.start();
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                client2.start();
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                client3.start();
            }
        });

        int count = 0;
        final int sleepTime = 10;

        while (!client1.isStarted()) {
            Thread.sleep(sleepTime);
            count++;
            if (count * sleepTime > startUpTimeOfClient) {
                logger.error("Client1 is not started");
                assertTrue(false);
            }
        }
        while (!client2.isStarted()) {
            Thread.sleep(sleepTime);
            count++;
            if (count * sleepTime > startUpTimeOfClient) {
                logger.error("Client2 is not started");
                assertTrue(false);
            }
        }
        while (!client3.isStarted()) {
            Thread.sleep(sleepTime);
            count++;
            if (count * sleepTime > startUpTimeOfClient) {
                logger.error("Client3 is not started");
                assertTrue(false);
            }
        }

        assertTrue(client1.isStarting() || client1.isStarted());
        assertTrue(client2.isStarting() || client2.isStarted());
        assertTrue(client3.isStarting() || client3.isStarted());

        while (!client1.isOpen()) {
            Thread.sleep(sleepTime);
            count++;
            if (count * sleepTime > startUpTimeOfClient) {
                logger.error("Client1 is not connected");
                assertTrue(false);
            }
        }

        while (!client2.isOpen()) {
            Thread.sleep(sleepTime);
            count++;
            if (count * sleepTime > startUpTimeOfClient) {
                logger.error("Client2 is not connected");
                assertTrue(false);
            }
        }
        while (!client3.isOpen()) {
            Thread.sleep(sleepTime);
            count++;
            if (count * sleepTime > startUpTimeOfClient) {
                logger.error("Client3 is not connected");
                assertTrue(false);
            }
        }

        assertTrue(client1.isOpen());
        assertTrue(client2.isOpen());
        assertTrue(client3.isOpen());

        // now clients are connected to a server

        // get session from a server
//        Set<Session> sessions = server.getSessionSet();

//        MessageQueueToAllSessions<String> taskExecutor =
//                new MessageQueueToAllSessions<>();
//        taskExecutor.putProperty(MessageQueueToAllSessions.PROPERTY_ID,mqId);
////        taskExecutor.putProperty(MessageQueueToAllSessions.PROPERTY_SESSION,sessions);
//
//        MQToClient<String> taskExecutor =
//                new MQToClient<>(MQToClient.KIND_OF_RECIEVER.ALL_CLIENTS);
//        taskExecutor.putProperty(MQToClient.PROPERTY_ID, mqId);
//        taskExecutor.putProperty(MQToClient.PROPERTY_JETTYSERVER, getServer());
//        MessageQueue<String> mq =
//                (MessageQueue<String>) MessageQueueMap.getInstance().get(mqId);
//        mq.addObserver(taskExecutor.getObserver());

        Thread.sleep(1000);

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                MessageQueueMap mqMap = MessageQueueMap.getInstance();
//                if(mqMap.isEmpty()) {
//                    MessageQueue<String> mq =
//                            new MessageQueue<>(
//                                    String.class,
//                                    msgListSize,
////                                    MessageQueue.default_maxQueueSize,
//                                    MessageQueue.OVERFLOW_STRATEGY.NO_INSERTION);
//                    mqMap.put(mqId, mq);
//                }
//                MessageQueue<String> mq = (MessageQueue<String>) mqMap.get(groupId);

                long startTime = System.currentTimeMillis();
                MessageQueue<String> mq =
                        (MessageQueue<String>) MessageQueueMap.getInstance().get(groupId);
                assertTrue(mq!=null);
                for(String msg: msgList) {
                    mq.addLast(msg);
                }
                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;
                logger.info("Elapsed time to add {} elements to Queue : {} ms", msgList.size(), elapsedTime);
                assertTrue(elapsedTime < insertionTime);
            }
        });

        while(client1.isBusy() || client2.isBusy() || client3.isBusy()) {
            Thread.sleep(500);
        }

        client1.stop();
        client2.stop();
        client3.stop();

        long numMsg1 = ((ClientHandlerExample)(client1.getHandler())).getNumberOfReceivedMessage();
        long numMsg2 = ((ClientHandlerExample)(client1.getHandler())).getNumberOfReceivedMessage();
        long numMsg3 = ((ClientHandlerExample)(client1.getHandler())).getNumberOfReceivedMessage();

        logger.info("The number of received message of client1 is {}", numMsg1);
        logger.info("The number of received message of client2 is {}", numMsg2);
        logger.info("The number of received message of client3 is {}", numMsg3);
        assertEquals(msgListSize, numMsg1);
        assertEquals(msgListSize, numMsg2);
        assertEquals(msgListSize, numMsg3);
        MessageQueue<String> mq =
                (MessageQueue<String>) MessageQueueMap.getInstance().get(groupId);
        assertEquals(0, mq.size());
    }

}
