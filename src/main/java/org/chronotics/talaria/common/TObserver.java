package org.chronotics.talaria.common;

/**
 * RxJava is tried for substitution of this class,
 * but I couldn't find the way to deal with multi-thread test
 * written by SGLee
 */
public interface TObserver<T> {
    void update(TObservable<T> observable, T object);
}
