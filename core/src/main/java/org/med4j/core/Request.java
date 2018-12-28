package org.medibloc.panacea.core;

import io.reactivex.Flowable;
import org.medibloc.panacea.utils.Async;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public abstract class Request<T> {
    abstract protected T doSend() throws IOException;

    public T send() throws IOException {
        return doSend();
    }

    public Future<T> sendAsync() {
        return Async.run(new Sender<T>());
    }

    public Flowable<T> flowable() {
        return Flowable.fromCallable(new Sender<T>());
    }

    class Sender<V> implements Callable<V> {
        @Override
        public V call() throws IOException {
            return (V)doSend();
        }
    }
}
