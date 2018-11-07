package org.chronotics.talaria.jettyclient;

import org.chronotics.talaria.websocket.jetty.JettyServer;
import org.chronotics.talaria.websocket.jetty.JettyServlet;
import org.chronotics.talaria.websocket.jettyclient.JettySocket;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
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
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestJettyClient {
    private static final Logger logger =
            LoggerFactory.getLogger(TestJettyClient.class);

    private static Server server = null;
    private static String serverUrl1 = "ws://192.168.0.13:8080/?id=111";
    private static String serverUrl2 = "ws://192.168.0.13:8080/?id=222";
    private static String serverUrl3 = "ws://192.168.0.13:8080/?id=333";
    private static String serverUrl4 = "ws://192.168.0.13:8080/?id=444";
    private static String serverUrl5 = "ws://192.168.0.13:8080/?id=555";

    @BeforeClass
    public static void setup() {

//        int port = 8080;
//        server = new Server(port);
//
//        ServletContextHandler context =
//                new ServletContextHandler(ServletContextHandler.SESSIONS);
//        context.setContextPath("/");
//        server.setHandler(context);
//
//        ServletHolder wsHolder = new ServletHolder("echo", new JettyServlet(JettySocket.class));
//        context.addServlet(wsHolder, "/*");
//
////        URL url = Thread.currentThread().getContextClassLoader().getResource("index.html");
////        Objects.requireNonNull(url, "unable to find index.html");
////        String urlBase = url.toExternalForm().replaceFirst("/[^/]*$", "/");
////        ServletHolder defHolder = new ServletHolder("default", new DefaultServlet());
////        defHolder.setInitParameter("resourceBase", urlBase);
////        defHolder.setInitParameter("dirAllowed", "true");
////        context.addServlet(defHolder,"/");
//
//        try {
//            server.start();
////            server.join();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @AfterClass
    public static void teardown() {
//        try {
//            server.stop();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    class ClientTest implements Runnable {
        private String url;
        ClientTest(String _url) {
            url = _url;
        }
        @Override
        public void run() {
            WebSocketClient client = new WebSocketClient();
            JettySocket socket = new JettySocket();
            try
            {
                client.start();

                URI echoUri = new URI(url);
                ClientUpgradeRequest request = new ClientUpgradeRequest();
                client.connect(socket,echoUri,request);
                System.out.printf("Connecting to : %s%n",echoUri);

                socket.awaitClose(3,TimeUnit.SECONDS);
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
            finally
            {
                try
                {
                    client.stop();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void testServerStart() throws InterruptedException {
//        assertTrue(server != null);
        Thread thread1 = new Thread(new ClientTest(serverUrl1));
        thread1.start();
        Thread thread2 = new Thread(new ClientTest(serverUrl2));
        thread2.start();
        Thread thread3 = new Thread(new ClientTest(serverUrl3));
        thread3.start();

        thread1.join();
        thread2.join();
        thread3.join();
    }
}
