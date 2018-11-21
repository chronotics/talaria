package org.chronotics.talaria.common;

import java.util.HashSet;
import java.util.Set;

public abstract class Observable {

    private Object syncObj = new Object();
    protected Set<Observer> observers = null;

    public void addObserver(Observer _observer) {
        assert(_observer!=null);
        if(_observer == null) {
            return;
        }
        synchronized (syncObj) {
            if(observers == null) {
                observers = new HashSet<>(1);
            }
            observers.add(_observer);
        }
    }

    public void removeObserver(Observer _observer) {
        assert(_observer!=null);
        if(_observer == null) {
            return;
        }
        synchronized (syncObj) {
            if(observers == null) {
                return;
            }
            observers.remove(_observer);
        }
    }

    public void removeAllObservers() {
        synchronized (syncObj) {
            if(observers == null) {
                return;
            }
            observers.clear();
        }
    }

    public void notifyObservers(Object _object) {
        Set<Observer> copiedObservers;
        synchronized (syncObj) {
            if(observers == null) {
                return;
            }
            copiedObservers = new HashSet<>(observers);
        }
        for(Observer observer: copiedObservers) {
            observer.update(this, _object);
        }
    }
}
