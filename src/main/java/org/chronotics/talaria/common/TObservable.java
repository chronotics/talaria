package org.chronotics.talaria.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * This Observable_ class is thread safe
 * RxJava was tried for substitution of this class,
 * thread issue is not solved yet.
 * Written by SGLee
 */
public abstract class TObservable<T> {

    private static final Logger logger =
            LoggerFactory.getLogger(TObservable.class);

    private Object syncObj = new Object();
    protected Set<TObserver> observers = null;

    public void addObserver(TObserver observer) {
        assert(observer!=null);
        if(observer == null) {
            logger.error("The observer you want to add is null");
            return;
        }
        synchronized (syncObj) {
            if(observers == null) {
                observers = new HashSet<>(1);
            }
            observers.add(observer);
        }
    }

    public void removeObserver(TObserver observer) {
        assert(observer!=null);
        if(observer == null) {
            logger.error("The observer you want to remove is null");
            return;
        }
        synchronized (syncObj) {
            if(observers == null) {
                logger.debug("There are no observers to remove");
                return;
            }
            observers.remove(observer);
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

    public void notifyObservers(T object) {
        Set<TObserver> copiedObservers;
        synchronized (syncObj) {
            if(observers == null) {
//                logger.debug("There are no observers");
                return;
            }
            copiedObservers = new HashSet<>(observers);
        }
        for(TObserver observer: copiedObservers) {
            observer.update(this, object);
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
