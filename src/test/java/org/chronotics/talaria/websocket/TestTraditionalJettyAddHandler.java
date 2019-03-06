package org.chronotics.talaria.websocket;

import org.chronotics.talaria.websocket.jetty.AbstractClientHandler;
import org.chronotics.talaria.websocket.jetty.JettyWebSocketServlet;
import org.chronotics.talaria.websocket.jetty.clienthandler.ClientHandlerExample;
import org.chronotics.talaria.websocket.jetty.jettylistener.EmptyListener;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestTraditionalJettyAddHandler {
    private static final Logger logger =
            LoggerFactory.getLogger(TestTraditionalJettyAddHandler.class);

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
    private static long awaitTimeOfClient = 1000; // ms
    private static long startUpTimeOfClient = 2000; // ms
    private static long startUpTimeOfServer = 1000; //ms
    private static long stopTimeoutOfServer = 1000; // ms

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

//                    ServletContextHandler gContext =
                    gContext =
                            new ServletContextHandler(ServletContextHandler.SESSIONS);
                    gContext.setContextPath("/topic");
                    ServletHolder wsHolder = new ServletHolder(
                            "EmptyListener",
                            new JettyWebSocketServlet(
                                    null,
                                    EmptyListener.class,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null));
                    gContext.addServlet(wsHolder, "/");

                    HandlerList handlerList = new HandlerList();
                    handlerList.addHandler(gContext);

                    gServer.setHandler(handlerList);

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

    @Test
    public void addHandler() throws InterruptedException {
        Thread.sleep(startUpTimeOfServer);

        assertTrue(gServer != null);
        logger.info(gServer.getState());
        assertTrue(gServer.isStarting() ||
                gServer.isStarted());

        stopServer();
        Thread.sleep(startUpTimeOfServer);

        ///////////////////////////////////////////////////////////////////////
        ServletContextHandler newContext =
                new ServletContextHandler(ServletContextHandler.SESSIONS);
        newContext.setContextPath("/wrong");
        ServletHolder newHolder = new ServletHolder(
                "newListener",
                new JettyWebSocketServlet(
                        null,
                        EmptyListener.class,
                        null,
                        null,
                        null,
                        null,
                        null));
        newContext.addServlet(newHolder, "/");

        Handler []handlerArray = gServer.getHandlers();
        List<Handler> handlers = new ArrayList<>();
        // If you want to multiple handlers,
        // you have to use difference contextPath
        for(Handler handler: handlerArray) {
            handlers.add(handler);
        }
        handlers.add(newContext);
        HandlerList handlerList =
                new HandlerList(handlers.stream()
                        .toArray(Handler[]::new));
        gServer.setHandler(handlerList);
        ///////////////////////////////////////////////////////////////////////

        startServer();
        while(!gServer.isStarted()) {
            Thread.sleep(startUpTimeOfServer);
        }

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

        client1.stop();
        client2.stop();
        client3.stop();

        assertTrue(client1.getClient().isStopping() ||
                client1.getClient().isStopped());
        assertTrue(client2.getClient().isStopping() ||
                client2.getClient().isStopped());
        assertTrue(client3.getClient().isStopping() ||
                client3.getClient().isStopped());
    }
}
