package org.chronotics.talaria.websocket.jettyclient;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

public class Application {

	public static void main(String[] args) {
		String destUri = "ws://192.168.0.13:8080/echo";
        if (args.length > 0)
        {
            destUri = args[0];
        }

        WebSocketClient client = new WebSocketClient();
        JettySocket socket = new JettySocket();
        try
        {
            client.start();

            URI echoUri = new URI(destUri);
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            client.connect(socket,echoUri,request);
            System.out.printf("Connecting to : %s%n",echoUri);

            // wait for closed socket connection.
            socket.awaitClose(5,TimeUnit.SECONDS);
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
