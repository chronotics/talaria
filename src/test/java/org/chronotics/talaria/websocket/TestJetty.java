package org.chronotics.talaria.websocket;

import org.chronotics.talaria.websocket.jetty.JettyServer;
import org.chronotics.talaria.websocket.jetty.JettyServlet;
import org.chronotics.talaria.websocket.jetty.websocket.ClientExample;
import org.chronotics.talaria.websocket.jetty.websocketlistener.ListenerEmpty;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestJetty {
    private static final Logger logger =
            LoggerFactory.getLogger(TestJetty.class);

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
    private static int awaitTimeOfClient = 1000; // ms
    private static int startUpTimeOfClient = 1500; // ms
    private static int startUpTimeOfServer = 1000; //ms
    private static int stopTimeoutOfServer = 1000; // ms

    private static JettyServer server = new JettyServer(port);

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
                    server.setContextHandler("/", JettyServer.SESSIONS);
                    server.addWebSocketListener(
                            "/",
                            "topic",
                            ListenerEmpty.class,
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

    class TestClient implements Runnable {
        private String url;

        TestClient(String _url) {
            url = _url;
        }

        private WebSocketClient client = null;

        public WebSocketClient getClient() {
            return client;
        }

        private Session session = null;

        public Session getSession() {
            return session;
        }

        @Override
        public void run() {
            if (client == null) {
                client = new WebSocketClient();
            }
            ClientExample socket = new ClientExample();
            try {
                client.start();

                URI echoUri = new URI(url);
                ClientUpgradeRequest request = new ClientUpgradeRequest();
                Future<Session> future = client.connect(socket, echoUri, request);
                session = future.get();

                socket.awaitClose(awaitTimeOfClient, TimeUnit.MILLISECONDS);
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                try {
                    client.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void startStopServer() {
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

        startServer();

        try {
            Thread.sleep(startUpTimeOfServer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertTrue(server.isStarting() || server.isStarted());

        assertTrue(server.getServer().getURI().getHost().equals("127.0.0.1"));
        assertEquals(port, server.getServer().getURI().getPort());
    }

    @Test
    public void runMultipleClients() throws InterruptedException {
        Thread.sleep(startUpTimeOfServer);

        assertTrue(server != null);
        assertTrue(server.isStarting() || server.isStarted());

        TestClient client1 = new TestClient(topicUrl1);
        TestClient client2 = new TestClient(topicUrl2);
        TestClient client3 = new TestClient(topicUrl3);
        Thread thread1 = new Thread(client1);
        thread1.start();
        Thread thread2 = new Thread(client2);
        thread2.start();
        Thread thread3 = new Thread(client3);
        thread3.start();

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

        thread1.join();
        thread2.join();
        thread3.join();

        assertTrue(client1.getClient().isStopping() ||
                client1.getClient().isStopped());
        assertTrue(client2.getClient().isStopping() ||
                client2.getClient().isStopped());
        assertTrue(client3.getClient().isStopping() ||
                client3.getClient().isStopped());
    }
}
