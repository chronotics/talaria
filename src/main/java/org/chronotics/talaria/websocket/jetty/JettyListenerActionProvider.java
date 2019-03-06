package org.chronotics.talaria.websocket.jetty;

import java.util.ArrayList;
import java.util.List;

public class JettyListenerActionProvider<T> {

    private List<JettyListenerAction> listenerActionList =
            new ArrayList<>();

    public static class Builder {
        private List<JettyListenerAction> actions = new ArrayList<>();

        public JettyListenerActionProvider build() {
            JettyListenerActionProvider provider = new JettyListenerActionProvider();
            for(JettyListenerAction action : actions) {
                provider.listenerActionList.add(action);
            }
            return provider;
        }

        public Builder addAction(JettyListenerAction action) {
            actions.add(action);
            return this;
        }
    }

    public List<JettyListenerAction> getActions() {
        return listenerActionList;
    }

    public void executeActions(JettyListener listener, T ...v) {
        for(JettyListenerAction action : listenerActionList) {
            action.execute(listener, v);
        }
    }
}
