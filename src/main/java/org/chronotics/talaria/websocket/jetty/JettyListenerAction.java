package org.chronotics.talaria.websocket.jetty;

public interface JettyListenerAction<T> {
    void execute(JettyListener listener, T ...v);
}
