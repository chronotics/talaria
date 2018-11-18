package org.chronotics.talaria.websocket;

import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.chronotics.talaria.websocket.jetty.AbstractClientHandler;
import org.chronotics.talaria.websocket.jetty.JettyClient;
import org.chronotics.talaria.websocket.jetty.JettyServer;
import org.chronotics.talaria.websocket.jetty.taskexecutor.MessageQueueToSessions;
import org.chronotics.talaria.websocket.jetty.websocket.ClientHandlerExample;
import org.chronotics.talaria.websocket.jetty.websocketlistener.EmptyListener;
import org.eclipse.jetty.util.Jetty;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestMessageQueueToSession {

    private static final Logger logger =
            LoggerFactory.getLogger(TestMessageQueueToSession.class);

    private static String contextPath = "/";
    private static String topicId = "topic";
    private static String topicPath = "/topic/";
    private static String otherTopicId = "otherTopic";
    private static String otherTopicPath = "/otherTopic/";
    private static String otherTopicUrl1 = "ws://localhost:8080/otherTopic/?id=111";
    private static String otherTopicUrl2 = "ws://localhost:8080/otherTopic/?id=222";
    private static String otherTopicUrl3 = "ws://localhost:8080/otherTopic/?id=333";
    private static String topicUrl1 = "ws://localhost:8080/topic/?id=111";
    private static String topicUrl2 = "ws://localhost:8080/topic/?id=222";
    private static String topicUrl3 = "ws://localhost:8080/topic/?id=333";
    private static int port = 8080;
    private static int awaitTimeOfClient = 1000; // ms
    private static int startUpTimeOfClient = 1500; // ms
    private static int startUpTimeOfServer = 1000; //ms
    private static int stopTimeoutOfServer = 1000; // ms

    private static JettyServer server = null;
    private static String mqId = "testQueue";

//    private static JettyClient client1 = null;
//    private static JettyClient client2 = null;
//    private static JettyClient client3 = null;

    private static List<String> msgList = null;
    private static int msgListSize = 1000;
    private static long insertionTime = 10000;

//    class TestClient implements Runnable {
//        private String url;
//
//        TestClient(String _url) {
//            url = _url;
//        }
//
//        private WebSocketClient client = null;
//        private AbstractClientHandler socket = null;
//
//        public WebSocketClient getClient() {
//            return client;
//        }
//
//        private Session session = null;
//
//        public Session getSession() {
//            return session;
//        }
//
//        public AbstractClientHandler getSocket() {
//            return socket;
//        }
//
//        public void stop() {
//            assertNotNull(socket);
//            if(socket==null) {
//                logger.error("socket is null");
//            }
//            socket.stop();
//        }
//
//        public void close() {
//            stop();
//            assertNotNull(client);
//            if(client==null) {
//                logger.error("client is null");
//                return;
//            }
//            try {
//                client.stop();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void run() {
//            if (client == null) {
//                client = new WebSocketClient();
//            }
//            ClientHandlerExample socket = new ClientHandlerExample();
//            this.socket = socket;
//            try {
//                client.start();
//
//                URI echoUri = new URI(url);
//                ClientUpgradeRequest request = new ClientUpgradeRequest();
//                Future<Session> future = client.connect(socket, echoUri, request);
//                session = future.get();
//
//                // wait until given time
////                socket.awaitClose(awaitTimeOfClient, TimeUnit.MILLISECONDS);
//                // wait until stop
//                socket.await();
//            } catch (Throwable t) {
//                t.printStackTrace();
//            }
//        }
//    }

    @BeforeClass
    public synchronized static void setup() {
        MessageQueueMap mqMap = MessageQueueMap.getInstance();
        if(!mqMap.isEmpty()) {
            mqMap.clear();
        }
        MessageQueue<String> mq =
                new MessageQueue<>(
                        String.class,
                        msgListSize,
//                                    MessageQueue.default_maxQueueSize,
                        MessageQueue.OVERFLOW_STRATEGY.NO_INSERTION);
        mqMap.put(mqId, mq);
        mq.setNotifyRemoval(true);

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
                            EmptyListener.class,
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
        JettyClient client2 = new JettyClient(topicUrl1, ClientHandlerExample.class);
        JettyClient client3 = new JettyClient(topicUrl1, ClientHandlerExample.class);

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

        MessageQueueToSessions<String> taskExecutor =
                new MessageQueueToSessions<>();
        taskExecutor.putProperty(MessageQueueToSessions.PROPERTY_MQID,mqId);
        taskExecutor.putProperty(MessageQueueToSessions.PROPERTY_SESSION,sessions);

        MessageQueue<String> mq =
                (MessageQueue<String>) MessageQueueMap.getInstance().get(mqId);
        mq.addObserver(taskExecutor.getMessageQueueObserver());

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                MessageQueueMap mqMap = MessageQueueMap.getInstance();
                if(mqMap.isEmpty()) {
                    MessageQueue<String> mq =
                            new MessageQueue<>(
                                    String.class,
                                    msgListSize,
//                                    MessageQueue.default_maxQueueSize,
                                    MessageQueue.OVERFLOW_STRATEGY.NO_INSERTION);
                    mqMap.put(mqId, mq);
                }
                long startTime = System.currentTimeMillis();
                MessageQueue<String> mq =
                        (MessageQueue<String>) MessageQueueMap.getInstance().get(mqId);
                for(String msg: msgList) {
                    mq.addLast(msg);
                }
                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;
                logger.info("Elapsed time to add {} elements to Queue : {} ms", msgList.size(), elapsedTime);
                assertTrue(elapsedTime < insertionTime);
            }
        });

        Thread.sleep(insertionTime);

        client1.stop();
        client2.stop();
        client3.stop();

        assertEquals(msgListSize,
                ((ClientHandlerExample)(client1.getHandler())).getNumOfReceivedMessage());
        assertEquals(msgListSize,
                ((ClientHandlerExample)(client2.getHandler())).getNumOfReceivedMessage());
        assertEquals(msgListSize,
                ((ClientHandlerExample)(client3.getHandler())).getNumOfReceivedMessage());


        assertEquals(0, mq.size());
    }

}
