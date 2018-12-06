package org.med4j.utils;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Async task facilitation.
 */
public class Async {
    private static final Scheduler scheduler = Schedulers.io();

    public static <T> Observable<T> run(Callable<T> callable) {
        return Observable.fromCallable(callable)
                .observeOn(scheduler)
                .timeout(60, TimeUnit.SECONDS);
    }
}
