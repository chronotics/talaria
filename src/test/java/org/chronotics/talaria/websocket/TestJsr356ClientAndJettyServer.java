package org.chronotics.talaria.websocket;

import org.chronotics.talaria.websocket.jetty.JettyServer;
import org.chronotics.talaria.websocket.jetty.websocketlistener.EmptyListener;
import org.chronotics.talaria.websocket.jsr356.Jsr356ClientExample;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestJsr356ClientAndJettyServer {
    private static final Logger logger =
            LoggerFactory.getLogger(TestJsr356ClientAndJettyServer.class);

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

    @Test
    public void testClient() throws InterruptedException {
        assertNotNull(server);
        Thread.sleep(startUpTimeOfServer);
        assertTrue(server.isStarting() || server.isStarted());

        Jsr356ClientExample socket = new Jsr356ClientExample();
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            container.connectToServer(socket, new URI(topicUrl1));
        } catch (DeploymentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        long startTime = System.currentTimeMillis();
        socket.sendMessage("ping");
        socket.awaitClose(1100, TimeUnit.MILLISECONDS);

        long endTime = System.currentTimeMillis();

        assertTrue((endTime-startTime) > 1000);
    }
}
