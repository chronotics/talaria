package org.chronotics.talaria.websocket;

import org.chronotics.talaria.common.MessageQueueMap;
import org.chronotics.talaria.websocket.jetty.JettyServer;
import org.chronotics.talaria.websocket.jetty.websocketlistener.TaskExecutorListener;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class TestMessageSendToAllSessions {

    private static final Logger logger =
            LoggerFactory.getLogger(TestMessageSendToAllSessions.class);

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
    private static long awaitTimeOfClient = 1000; // ms
    private static long startUpTimeOfClient = 1500; // ms
    private static long startUpTimeOfServer = 1500; //ms
    private static long stopTimeoutOfServer = 1000; // ms

    private static JettyServer server = null;

    private static List<String> msgList = null;
    private static int msgListSize = 1000;
    private static long insertionTime = 1000;

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
                            TaskExecutorListener.class,
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
}
