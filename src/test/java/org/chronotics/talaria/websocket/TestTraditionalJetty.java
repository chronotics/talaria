package org.chronotics.talaria.websocket;

import org.chronotics.talaria.websocket.jetty.websocket.ClientExample;
import org.chronotics.talaria.websocket.jetty.websocketlistener.ListenerEmpty;
import org.chronotics.talaria.websocket.jetty.JettyServlet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestTraditionalJetty {
    private static final Logger logger =
            LoggerFactory.getLogger(TestTraditionalJetty.class);

    private static Server gServer = null;
    private static ServletContextHandler gContext = null;

    private static String otherTopicUrl1 = "ws://localhost:8080/otherTopic/?id=111";
    private static String otherTopicUrl2 = "ws://localhost:8080/otherTopic/?id=222";
    private static String otherTopicUrl3 = "ws://localhost:8080/otherTopic/?id=333";
    private static String topicUrl1 = "ws://localhost:8080/topic/?id=111";
    private static String topicUrl2 = "ws://localhost:8080/topic/?id=222";
    private static String topicUrl3 = "ws://localhost:8080/topic/?id=333";
    private static String wrongUrl1 = "ws://localhost:8080/wrong/?id=111";
    private static String wrongUrl2 = "ws://localhost:8080/wrong/?id=222";
    private static String wrongUrl3 = "ws://localhost:8080/wrong/?id=333";
    private static int port = 8080;
    private static int awaitTimeOfClient = 1000; // ms
    private static int startUpTimeOfClient = 2000; // ms
    private static int startUpTimeOfServer = 1000; //ms
    private static int stopTimeoutOfServer = 1000; // ms

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
                if (gServer == null) {
                    gServer = new Server(port);

                    gContext =
                            new ServletContextHandler(ServletContextHandler.SESSIONS);
                    gContext.setContextPath("/");

                    ServletHolder wsHolder = new ServletHolder(
                            "ListenerEmpty",
                            new JettyServlet(ListenerEmpty.class));
                    gContext.addServlet(wsHolder, "/topic/");

                    gServer.setHandler(gContext);

//                    HandlerCollection newHandlerCollection = new HandlerCollection();
//                    Handler []handlerArray = gServer.getHandlers();
//                    newHandlerCollection.addHandler(newContext);
//                    for(Handler handler: handlerArray) {
//                        newHandlerCollection.addHandler(handler);
//                    }
//                    gServer.setHandler(newHandlerCollection);

//                    URL url = Thread.currentThread().getContextClassLoader().getResource("index.html");
//                    Objects.requireNonNull(url, "unable to find index.html");
//                    String urlBase = url.toExternalForm().replaceFirst("/[^/]*$", "/");
//                    ServletHolder defHolder = new ServletHolder("default", new DefaultServlet());
//                    defHolder.setInitParameter("resourceBase", urlBase);
//                    defHolder.setInitParameter("dirAllowed", "true");
//                    gContext.addServlet(defHolder,"/");
                }

                if (gServer.isStopped()) {
                    try {
                        gServer.start();
                        gServer.join();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private static void stopServer() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (!gServer.isStopped()) {
                    try {
                        gServer.setStopTimeout(stopTimeoutOfServer);
                        gServer.stop();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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

        if (gServer.isStarted()) {
            TestTraditionalJetty.stopServer();
            try {
                Thread.sleep(startUpTimeOfServer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        assertTrue(gServer.isStopping() ||
                gServer.isStopped());

        startServer();
        try {
            Thread.sleep(startUpTimeOfServer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertTrue(gServer.isStarting() ||
                gServer.isStarted());

        assertTrue(gServer.getURI().getHost().equals("127.0.0.1"));
        assertEquals(port, gServer.getURI().getPort());

//        stopServer();
//        try {
//            Thread.sleep(startUpTimeOfServer);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        assertTrue(gServer.isStopping() ||
//                gServer.isStopped());
    }

    @Test
    public void runMultipleClientsWithWrongClient1() throws InterruptedException {
        Thread.sleep(startUpTimeOfServer);

        assertTrue(gServer != null);
        assertTrue(gServer.isStarting() ||
                gServer.isStarted());

        TestClient client1 = new TestClient(wrongUrl1);
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

        assertNull(client1.getSession());
    }

    @Test
    public void runMultipleClientsWithWrongClient2() throws InterruptedException {
        Thread.sleep(startUpTimeOfServer);

        assertTrue(gServer != null);
        assertTrue(gServer.isStarting() ||
                gServer.isStarted());

        TestClient client1 = new TestClient(topicUrl1);
        TestClient client2 = new TestClient(wrongUrl2);
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

        while (client3.getSession() == null) {
            Thread.sleep(sleepTime);
            count++;
            if (count * sleepTime > startUpTimeOfClient) {
                logger.error("Client3 is not connected");
                assertTrue(false);
            }
        }

        assertNull(client2.getSession());
        return;
    }

    @Test
    public void runMultipleClientsWithWrongClient3() throws InterruptedException {
        Thread.sleep(startUpTimeOfServer);

        assertTrue(gServer != null);
        assertTrue(gServer.isStarting() ||
                gServer.isStarted());

        TestClient client1 = new TestClient(topicUrl1);
        TestClient client2 = new TestClient(topicUrl2);
        TestClient client3 = new TestClient(wrongUrl3);
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

        assertNull(client3.getSession());
        return;
    }

    @Test
    public void runMultipleClients() throws InterruptedException {
        Thread.sleep(startUpTimeOfServer);

        assertTrue(gServer != null);
        logger.info(gServer.getState());
        assertTrue(gServer.isStarting() ||
                gServer.isStarted());

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

    @Test
    public void addServlet() throws InterruptedException {
        Thread.sleep(startUpTimeOfServer);

        assertTrue(gServer != null);
        logger.info(gServer.getState());
        assertTrue(gServer.isStarting() ||
                gServer.isStarted());

        ServletHolder wsHolder = new ServletHolder(
                "rootListener",
                new JettyServlet(ListenerEmpty.class));
        gContext.addServlet(wsHolder, "/otherTopic/");

        TestClient client1 = new TestClient(otherTopicUrl1);
        TestClient client2 = new TestClient(otherTopicUrl2);
        TestClient client3 = new TestClient(otherTopicUrl3);
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
