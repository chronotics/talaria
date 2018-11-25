package org.chronotics.talaria.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * This Observable class is thread safe
 * Written by SGLee
 */
public abstract class Observable {

    private static final Logger logger =
            LoggerFactory.getLogger(Observable.class);

    private Object syncObj = new Object();
    protected Set<Observer> observers = null;

    public void addObserver(Observer _observer) {
        assert(_observer!=null);
        if(_observer == null) {
            logger.error("The observer you want to add is null");
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
            logger.error("The observer you want to remove is null");
            return;
        }
        synchronized (syncObj) {
            if(observers == null) {
                logger.debug("There are no observers to remove");
                return;
            }
            observers.remove(_observer);
        }
    }

    public void removeAllObservers() {
        synchronized (syncObj) {
            if(observers == null) {
                logger.debug("There are no observers to remove");
                return;
            }
            observers.clear();
        }
    }

    public void notifyObservers(Object _object) {
        Set<Observer> copiedObservers;
        synchronized (syncObj) {
            if(observers == null) {
//                logger.debug("There are no observers");
                return;
            }
            copiedObservers = new HashSet<>(observers);
        }
        for(Observer observer: copiedObservers) {
            observer.update(this, _object);
        }
    }

    public int countObservers() {
        synchronized (syncObj) {
            if(observers == null) {
                return 0;
            } else {
                return observers.size();
            }
        }
    }
}
