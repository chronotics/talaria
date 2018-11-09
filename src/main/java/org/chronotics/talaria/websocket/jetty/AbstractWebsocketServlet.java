package org.chronotics.talaria.websocket.jetty;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;

/**
 * Since 2015
 * Written by SGlee
 */

public abstract class AbstractWebsocketServlet extends WebSocketServlet {

    // set a 10 second timeout
    private long idleTimeout = 10000;

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }
}
