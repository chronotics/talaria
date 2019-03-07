package org.chronotics.talaria.common;

/**
 * RxJava is tried for substitution of this class,
 * thread issue is not solved yet.
 * written by SGLee
 */
public interface TObserver<T> {
    void update(TObservable<T> observable, T object);
}
