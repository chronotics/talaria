package org.chronotics.talaria.websocket.jetty;

import java.util.function.Function;

public interface JettyListenerAction<T> {
    void execute(JettyListener listener, T ...v);

//    void onConnect(Function<T, T> func, JettyListener listener, T ...v);
//    void onClose(Function<T, T> func, JettyListener listener, T ...v);
//    void onText(Function<T, T> func, JettyListener listener, T ...v);
//    void onBinary(Function<T, T> func, JettyListener listener, T ...v);
//    void onError(Function<T, T> func, JettyListener listener, T ...v);
}
