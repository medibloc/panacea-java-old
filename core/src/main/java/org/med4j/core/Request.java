package org.med4j.core;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import org.med4j.utils.Async;

import java.io.IOException;
import java.util.concurrent.Callable;

public abstract class Request<T> implements Callable<T> {
    abstract protected T doSend() throws IOException;

    public T send() throws IOException {
        return doSend();
    }

    @Override
    public T call() throws IOException {
        return doSend();
    }

    public Observable<T> sendAsync() {
        return Async.run(this);
    }

    public Flowable<T> flowable() {
        return Flowable.fromCallable(this);
    }
}
