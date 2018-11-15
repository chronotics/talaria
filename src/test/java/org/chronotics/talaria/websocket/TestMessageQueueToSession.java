package org.chronotics.talaria.websocket;

import org.chronotics.talaria.common.MessageQueue;
import org.chronotics.talaria.common.MessageQueueMap;
import org.chronotics.talaria.websocket.jetty.AbstractClientHandler;
import org.chronotics.talaria.websocket.jetty.JettyServer;
import org.chronotics.talaria.websocket.jetty.taskexecutor.MessageQueueToSessions;
import org.chronotics.talaria.websocket.jetty.websocket.ClientHandlerExample;
import org.chronotics.talaria.websocket.jetty.websocketlistener.EmptyListener;
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

    private static List<String> msgList = null;
    private static int msgListSize = 1000;
    private static long insertionTime = 5000;

    class TestClient implements Runnable {
        private String url;

        TestClient(String _url) {
            url = _url;
        }

        private WebSocketClient client = null;
        private AbstractClientHandler socket = null;

        public WebSocketClient getClient() {
            return client;
        }

        private Session session = null;

        public Session getSession() {
            return session;
        }

        public AbstractClientHandler getSocket() {
            return socket;
        }

        public void stop() {
            assertNotNull(socket);
            if(socket==null) {
                logger.error("socket is null");
            }
            socket.stop();
        }

        public void close() {
            stop();
            assertNotNull(client);
            if(client==null) {
                logger.error("client is null");
                return;
            }
            try {
                client.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            if (client == null) {
                client = new WebSocketClient();
            }
            ClientHandlerExample socket = new ClientHandlerExample();
            this.socket = socket;
            try {
                client.start();

                URI echoUri = new URI(url);
                ClientUpgradeRequest request = new ClientUpgradeRequest();
                Future<Session> future = client.connect(socket, echoUri, request);
                session = future.get();

                // wait until given time
//                socket.awaitClose(awaitTimeOfClient, TimeUnit.MILLISECONDS);
                // wait until stop
                socket.await();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

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
                    server.setContextHandler("/", JettyServer.SESSIONS);
                    server.addWebSocketListener(
                            "/",
                            "topic",
                            EmptyListener.class,
                            "/topic/");
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

        // create clients
        TestClient client1 = new TestClient(topicUrl1);
        TestClient client2 = new TestClient(topicUrl2);
        TestClient client3 = new TestClient(topicUrl3);
        Thread thread1 = new Thread(client1);
        thread1.start();
        Thread thread2 = new Thread(client2);
        thread2.start();
        Thread thread3 = new Thread(client3);
        thread3.start();

//        thread1.join();
//        thread2.join();
//        thread3.join();

        int count = 0;
        final int sleepTime = 10;

        while (client1.getClient() == null) {
            Thread.sleep(sleepTime);
            count++;
            if (count * sleepTime > startUpTimeOfClient) {
                logger.error("Client1 is not started");
                assertTrue(false);
            }
        }

        while (client2.getClient() == null) {
            Thread.sleep(sleepTime);
            count++;
            if (count * sleepTime > startUpTimeOfClient) {
                logger.error("Client2 is not started");
                assertTrue(false);
            }
        }
        while (client3.getClient() == null) {
            Thread.sleep(sleepTime);
            count++;
            if (count * sleepTime > startUpTimeOfClient) {
                logger.error("Client3 is not started");
                assertTrue(false);
            }
        }
        assertTrue(client1.getClient().isStarting() ||
                client1.getClient().isStarted());
        assertTrue(client2.getClient().isStarting() ||
                client2.getClient().isStarted());
        assertTrue(client3.getClient().isStarting() ||
                client3.getClient().isStarted());

        while (client1.getSession() == null) {
            Thread.sleep(sleepTime);
            count++;
            if (count * sleepTime > startUpTimeOfClient) {
                logger.error("Client1 is not connected");
                assertTrue(false);
            }
        }

        while (client2.getSession() == null) {
            Thread.sleep(sleepTime);
            count++;
            if (count * sleepTime > startUpTimeOfClient) {
                logger.error("Client2 is not connected");
                assertTrue(false);
            }
        }
        while (client3.getSession() == null) {
            Thread.sleep(sleepTime);
            count++;
            if (count * sleepTime > startUpTimeOfClient) {
                logger.error("Client3 is not connected");
                assertTrue(false);
            }
        }

        assertTrue(client1.getSession().isOpen());
        assertTrue(client2.getSession().isOpen());
        assertTrue(client3.getSession().isOpen());

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

        assertEquals(msgListSize,
                ((ClientHandlerExample)(client1.getSocket())).getNumOfReceivedMessage());
        assertEquals(msgListSize,
                ((ClientHandlerExample)(client2.getSocket())).getNumOfReceivedMessage());
        assertEquals(msgListSize,
                ((ClientHandlerExample)(client3.getSocket())).getNumOfReceivedMessage());

        client1.close();
        client2.close();
        client3.close();

        assertTrue(client1.getClient().isStopping() ||
                client1.getClient().isStopped());
        assertTrue(client2.getClient().isStopping() ||
                client2.getClient().isStopped());
        assertTrue(client3.getClient().isStopping() ||
                client3.getClient().isStopped());

        assertEquals(0, mq.size());
    }

}
