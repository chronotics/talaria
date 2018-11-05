package org.chronotics.talaria.websocket;

import org.chronotics.talaria.websocket.jsr356.ClientSocket;
import org.chronotics.talaria.websocket.jsr356.ServerSocket;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
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

@RunWith(SpringJUnit4ClassRunner.class)
public class TestJSR356 {
    private static final Logger logger =
            LoggerFactory.getLogger(TestJSR356.class);

    private ServerSocket serverSocket = null;
    public void startServer() {
//        String dst = "ws://localhost:8080/hello/";
//        serverSocket = new ServerSocket();
//        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
//        try {
//            container.connectToServer(serverSocket, new URI(dst));
//        } catch (DeploymentException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
    }

    @BeforeClass
    public static void setup() {

    }

    @AfterClass
    public static void tearDown() {

    }

    @Test
    public void testClient() {
//        startServer();

//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        String dest = "ws://localhost:8080";
        ClientSocket socket = new ClientSocket();
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            container.connectToServer(socket, new URI(dest));
        } catch (DeploymentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        try {
            socket.getLatch().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        socket.sendMessage("ping");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
