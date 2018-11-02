package org.chronotics.talaria.websocket.jetty;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class Application {
    public static void main(String[] args) {

        JettyServer jettyServer = new JettyServer();
        jettyServer.setup();

        ServletContextHandler handler =
                new ServletContextHandler(ServletContextHandler.SESSIONS);

        handler.setContextPath("/");
        handler.addServlet(JettyServlet.class, "/test");

        jettyServer.addHandler(handler);

        try {
            jettyServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        String destUri = "ws://localhost:9000";
//        if (args.length > 0)
//        {
//            destUri = args[0];
//        }
//
//        WebSocketClient client = new WebSocketClient();
//        JettySocket socket = new JettySocket();
//        try
//        {
//            client.start();
//
//            URI echoUri = new URI(destUri);
//            ClientUpgradeRequest request = new ClientUpgradeRequest();
//            client.connect(socket,echoUri,request);
//            System.out.printf("Connecting to : %s%n",echoUri);
//
//            Thread.sleep(2000);
//            // wait for closed socket connection.
////            socket.awaitClose(5,TimeUnit.SECONDS);
//        }
//        catch (Throwable t)
//        {
//            t.printStackTrace();
//        }
//        finally
//        {
//            try
//            {
//                client.stop();
//            }
//            catch (Exception e)
//            {
//                e.printStackTrace();
//            }
//        }

    }
}
