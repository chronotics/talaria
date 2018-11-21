package org.chronotics.talaria.websocket;

import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.chronotics.talaria.websocket.jetty.JettyClient;
import org.chronotics.talaria.websocket.jetty.JettyServer;
import org.chronotics.talaria.websocket.jetty.taskexecutor.MessageQueueToAllSessions;
import org.chronotics.talaria.websocket.jetty.websocket.ClientHandlerExample;
import org.chronotics.talaria.websocket.jetty.websocketlistener.EmptyListener;
import org.chronotics.talaria.websocket.jetty.websocketlistener.MQGenByIdListener;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestMessageQueueToOneSession {

    private static final Logger logger =
            LoggerFactory.getLogger(TestMessageQueueToOneSession.class);

    private static String contextPath = "/";
    private static String topicId = "topic";
    private static String topicPath = "/topic/";
    private static String otherTopicId = "otherTopic";
    private static String otherTopicPath = "/otherTopic/";
    private static String id1 = "id1";
    private static String id2 = "id2";
    private static String id3 = "id3";
    private static String otherTopicUrl1 = "ws://localhost:8080/otherTopic/?id="+id1;
    private static String otherTopicUrl2 = "ws://localhost:8080/otherTopic/?id="+id2;
    private static String otherTopicUrl3 = "ws://localhost:8080/otherTopic/?id="+id3;
    private static String topicUrl1 = "ws://localhost:8080/topic/?id="+id1;
    private static String topicUrl2 = "ws://localhost:8080/topic/?id="+id2;
    private static String topicUrl3 = "ws://localhost:8080/topic/?id="+id3;
    private static int port = 8080;
    private static int awaitTimeOfClient = 1000; // ms
    private static int startUpTimeOfClient = 1500; // ms
    private static int startUpTimeOfServer = 1500; //ms
    private static int stopTimeoutOfServer = 1000; // ms

    private static JettyServer server = null;

    private static List<String> msgList = null;
    private static int msgListSize = 10000;
    private static long insertionTime = 10000;

    @BeforeClass
    public synchronized static void setup() {
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
                            MQGenByIdListener.class,
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
        Set<Session> sessions = server.getSessionSet();

        MessageQueue<String> mq1 =
                (MessageQueue<String>) MessageQueueMap.getInstance().get(id1);
        MessageQueue<String> mq2 =
                (MessageQueue<String>) MessageQueueMap.getInstance().get(id2);
        MessageQueue<String> mq3 =
                (MessageQueue<String>) MessageQueueMap.getInstance().get(id3);

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                MessageQueueMap mqMap = MessageQueueMap.getInstance();
                long startTime = System.currentTimeMillis();
                for(String msg: msgList) {
                    mq1.addLast(msg);
                }
                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;
                logger.info("Elapsed time to add {} elements to Queue : {} ms", msgList.size(), elapsedTime);
                assertTrue(elapsedTime < insertionTime);
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                MessageQueueMap mqMap = MessageQueueMap.getInstance();
                long startTime = System.currentTimeMillis();
                for(String msg: msgList) {
                    mq2.addLast(msg);
                }
                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;
                logger.info("Elapsed time to add {} elements to Queue : {} ms", msgList.size(), elapsedTime);
                assertTrue(elapsedTime < insertionTime);
            }
        });
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                MessageQueueMap mqMap = MessageQueueMap.getInstance();
                long startTime = System.currentTimeMillis();
                for(String msg: msgList) {
                    mq3.addLast(msg);
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

        int numMsg1 = ((ClientHandlerExample)(client1.getHandler())).getNumOfReceivedMessage();
        int numMsg2 = ((ClientHandlerExample)(client1.getHandler())).getNumOfReceivedMessage();
        int numMsg3 = ((ClientHandlerExample)(client1.getHandler())).getNumOfReceivedMessage();

        client1.stop();
        client2.stop();
        client3.stop();

        logger.info("The number of received message of client1 is {}", numMsg1);
        logger.info("The number of received message of client2 is {}", numMsg2);
        logger.info("The number of received message of client3 is {}", numMsg3);

        while(!mq1.isEmpty() || !mq2.isEmpty() || !mq3.isEmpty()) {
            Thread.sleep(1000);
        }

        logger.info("The size of mq1 is {}", mq1.size());
        logger.info("The size of mq2 is {}", mq2.size());
        logger.info("The size of mq3 is {}", mq3.size());
        assertEquals(0, mq1.size());
        assertEquals(0, mq2.size());
        assertEquals(0, mq3.size());
        assertEquals(msgListSize, numMsg1);
        assertEquals(msgListSize, numMsg2);
        assertEquals(msgListSize, numMsg3);
    }

}
