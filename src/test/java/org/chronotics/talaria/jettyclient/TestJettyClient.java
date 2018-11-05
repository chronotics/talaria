package org.chronotics.talaria.jettyclient;

import org.chronotics.talaria.websocket.jetty.JettyServer;
import org.chronotics.talaria.websocket.jetty.JettyServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestJettyClient {
    private static final Logger logger =
            LoggerFactory.getLogger(TestJettyClient.class);

    private static JettyServer jettyServer = null;
    @BeforeClass
    public static void setup() {
        jettyServer = new JettyServer();
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
    }

    @AfterClass
    public static void teardown() {
        try {
            jettyServer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testServerStart() {
//        assertTrue(jettyServer!=null);
    }
}
