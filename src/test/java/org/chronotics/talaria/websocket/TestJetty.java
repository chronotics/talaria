package org.chronotics.talaria.websocket;

import org.chronotics.talaria.websocket.jetty.JettyClient;
import org.chronotics.talaria.websocket.jetty.JettyServer;
import org.chronotics.talaria.websocket.jetty.websocket.ClientHandlerExample;
import org.chronotics.talaria.websocket.jetty.websocketlistener.EmptyListener;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * The below is a scenario based test of JettyServer and JettyClient
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class TestJetty {
    private static final Logger logger =
            LoggerFactory.getLogger(TestJetty.class);

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
    private static String wrongUrl1 = "ws://localhost:8080/wrong";
    private static String wrongUrl2 = "ws://localhost:8080/wrong";
    private static String wrongUrl3 = "ws://localhost:8080/wrong";
    private static int port = 8080;
    private static long awaitTimeOfClient = 1000; // ms
    private static long startUpTimeOfClient = 1500; // ms
    private static long startUpTimeOfServer = 1000; //ms
    private static long stopTimeoutOfServer = 1000; // ms

    private static JettyServer server = null;

    @BeforeClass
    public synchronized static void setup() {
        startServer();
    }

    @AfterClass
    public synchronized static void teardown() {
        stopServer();
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

//    class TestClient implements Runnable {
//        private String url;
//        private Class handlerClass;
//        private WebSocketClient client = null;
//        private AbstractClientHandler handler = null;
//
//        public TestClient(String _url, Class _handlerClass) {
//            url = _url;
//            handlerClass = _handlerClass;
//        }
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
//        public AbstractClientHandler getHandler() {
//            return handler;
//        }
//
//        public void start() {
//            if (client == null) {
//                client = new WebSocketClient();
//            }
//
//            if(handler == null) {
//                try {
//                    handler = (AbstractClientHandler)handlerClass.newInstance();
//                } catch (InstantiationException e) {
//                    e.printStackTrace();
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            if(!client.isStarting() || !client.isStarted()) {
//                try {
//                    client.start();
//                    URI echoUri = new URI(url);
//                    ClientUpgradeRequest request = new ClientUpgradeRequest();
//                    Future<Session> future = client.connect(handler, echoUri, request);
//                    session = future.get();
//                } catch (Throwable t) {
//                    t.printStackTrace();
//                }
//            }
//
//            handler.await();
//        }
//
//        public void stop() {
//            assertNotNull(handler);
//            if(handler==null) {
//                logger.error("socket is null");
//            }
//            handler.stop();
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
//            start();
//        }
//    }

    @Test
    public void startStopServer() throws InterruptedException {
        assertNotNull(server);

        Thread.sleep(startUpTimeOfServer);
        assertTrue(server.isStarting() || server.isStarted());

        if(server.isStarted()) {
            stopServer();
        }

        Thread.sleep(startUpTimeOfServer);

        assertTrue(server.isStopping() || server.isStopped());

        startServer();

        try {
            Thread.sleep(startUpTimeOfServer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertTrue(server.isStarting() || server.isStarted());

//        logger.info("server ip is {}", server.getServer().getURI().getHost());
//        assertTrue(server.getServer().getURI().getHost().equals("127.0.0.1"));
        assertEquals(port, server.getPort());
    }

    @Test
    public void runMultipleClients() throws InterruptedException {
        assertNotNull(server);

        Thread.sleep(startUpTimeOfServer);

        assertTrue(server.isStarting() || server.isStarted());

        JettyClient client1 = new JettyClient(topicUrl1,ClientHandlerExample.class);
        JettyClient client2 = new JettyClient(topicUrl2,ClientHandlerExample.class);
        JettyClient client3 = new JettyClient(topicUrl3,ClientHandlerExample.class);

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

        client1.stop();
        client2.stop();
        client3.stop();

        assertTrue(client1.isStopping() || client1.isStopped());
        assertTrue(client2.isStopping() || client2.isStopped());
        assertTrue(client3.isStopping() || client3.isStopped());
    }

    @Test
    public void addListener() throws InterruptedException {
        assertNotNull(server);

        assertTrue(server.isStarting() || server.isStarted());

        if(server.isStarted()) {
            stopServer();
            try {
                Thread.sleep(startUpTimeOfServer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        assertTrue(server.isStopping() || server.isStopped());

        server.addWebSocketListener(
                contextPath,
                otherTopicId,
                EmptyListener.class,
                otherTopicPath);

        startServer();

        try {
            Thread.sleep(startUpTimeOfServer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertTrue(server.isStarting() || server.isStarted());

        JettyClient client1 = new JettyClient(otherTopicUrl1,ClientHandlerExample.class);
        JettyClient client2 = new JettyClient(otherTopicUrl2,ClientHandlerExample.class);
        JettyClient client3 = new JettyClient(otherTopicUrl3,ClientHandlerExample.class);
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

        client1.stop();
        client2.stop();
        client3.stop();

        assertTrue(client1.isStopping() || client1.isStopped());
        assertTrue(client2.isStopping() || client2.isStopped());
        assertTrue(client3.isStopping() || client3.isStopped());
    }

    @Test
    public void sendMessageToClients() throws InterruptedException {
        assertNotNull(server);

        Thread.sleep(startUpTimeOfServer);

        assertTrue(server.isStarting() || server.isStarted());

        JettyClient client1 = new JettyClient(topicUrl1,ClientHandlerExample.class);
        JettyClient client2 = new JettyClient(topicUrl2,ClientHandlerExample.class);
        JettyClient client3 = new JettyClient(topicUrl3,ClientHandlerExample.class);
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

        for(int i = 0; i< 1000; i++) {
            server.sendMessageToAllClients("Hello");
        }

        client1.stop();
        client2.stop();
        client3.stop();

        assertTrue(client1.isStopping() || client1.isStopped());
        assertTrue(client2.isStopping() || client2.isStopped());
        assertTrue(client3.isStopping() || client3.isStopped());
    }

    @Test
    public void startStopMultipleClients() throws InterruptedException {
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

        client1.stop();
        client2.stop();
        client3.stop();

        assertTrue(client1.isStopping() || client1.isStopped());
        assertTrue(client2.isStopping() || client2.isStopped());
        assertTrue(client3.isStopping() || client3.isStopped());

        long startTime = System.currentTimeMillis();

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                client1.stop();
                client2.stop();
                client3.stop();
            }
        });

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

        // wait and will be interrupted after 2000 ms

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        logger.info("elapsed time is {}", elapsedTime);

        assertTrue(elapsedTime < 1000);

        Thread.sleep(4000);

        assertTrue(client1.isStopped());
        assertTrue(client2.isStopped());
        assertTrue(client3.isStopped());

    }

    @Test
    public void startClientAndStop() throws InterruptedException {
        assertNotNull(server);

        Thread.sleep(startUpTimeOfServer);

        assertTrue(server.isStarting() || server.isStarted());

        long startTime = System.currentTimeMillis();

        JettyClient client1 = new JettyClient(topicUrl1, ClientHandlerExample.class);
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                client1.start(3000,TimeUnit.MILLISECONDS);
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
        assertTrue(client1.isStarting() || client1.isStarted());

        while (!client1.isOpen()) {
            Thread.sleep(sleepTime);
            count++;
            if (count * sleepTime > startUpTimeOfClient) {
                logger.error("Client1 is not connected");
                assertTrue(false);
            }
        }

        assertTrue(client1.isOpen());

        client1.stop();

        long endTime = System.currentTimeMillis();
        logger.info("elapsed time {}", endTime - startTime);
        assertTrue(endTime-startTime < 1000);

        assertTrue(client1.isStopping() || client1.isStopped());
    }

    @Test
    public void startClientAndThreadStop() throws InterruptedException {
        assertNotNull(server);

        Thread.sleep(startUpTimeOfServer);

        assertTrue(server.isStarting() || server.isStarted());

        long startTime = System.currentTimeMillis();

        JettyClient client1 = new JettyClient(topicUrl1, ClientHandlerExample.class);
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                client1.start(3000,TimeUnit.MILLISECONDS);
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
        assertTrue(client1.isStarting() || client1.isStarted());

        while (!client1.isOpen()) {
            Thread.sleep(sleepTime);
            count++;
            if (count * sleepTime > startUpTimeOfClient) {
                logger.error("Client1 is not connected");
                assertTrue(false);
            }
        }

        assertTrue(client1.isOpen());

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                client1.stop();
            }
        });

        while (!client1.isStopped()) {
            Thread.sleep(sleepTime);
            count++;
            if (count * sleepTime > startUpTimeOfClient) {
                logger.error("Client1 is not connected");
                assertTrue(false);
            }
        }

        long endTime = System.currentTimeMillis();
        logger.info("elapsed time {}", endTime - startTime);
        assertTrue(endTime-startTime < 1000);

        assertTrue(client1.isStopping() || client1.isStopped());
    }

    @Test
    public void startClientAndAwaitToStop() throws InterruptedException {
        assertNotNull(server);

        Thread.sleep(startUpTimeOfServer);

        assertTrue(server.isStarting() || server.isStarted());

        long startTime = System.currentTimeMillis();

        JettyClient client1 = new JettyClient(topicUrl1, ClientHandlerExample.class);
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                client1.start(3000,TimeUnit.MILLISECONDS);
            }
        });

        Thread.sleep(1000);

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
        assertTrue(client1.isStarting() || client1.isStarted());

        while (!client1.isOpen()) {
            Thread.sleep(sleepTime);
            count++;
            if (count * sleepTime > startUpTimeOfClient) {
                logger.error("Client1 is not connected");
                assertTrue(false);
            }
        }

        assertTrue(client1.isOpen());

        Thread.sleep(2500);

        long endTime = System.currentTimeMillis();
        logger.info("elapsed time {}", endTime - startTime);
        assertTrue(endTime-startTime >= 3500);

        assertTrue(client1.isStopping() || client1.isStopped());
    }
}
