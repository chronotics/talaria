package org.chronotics.talaria.websocket.jetty;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;

/**
 * Since 2015
 * Written by SGlee
 */

public abstract class AbstractWebsocketServlet extends WebSocketServlet {

    // set a 10 second timeout
    // ms
    private long idleTimeout = 10000;

    /**
     * ms
     * @return
     */
    public long getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * ms
     * @param idleTimeout
     */
    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }
}
