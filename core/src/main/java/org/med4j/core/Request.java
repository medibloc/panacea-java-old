package org.med4j.core;

import com.google.protobuf.Message;

import org.med4j.utils.Async;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.reactivex.Flowable;

public abstract class Request<T> {
    abstract protected T doSend() throws IOException;

    public T send() throws IOException {
        return doSend();
    }

    public CompletableFuture<T> sendAsync() {
        return  Async.run(this::doSend);
    }

    public Flowable<T> flowable() {
        return Flowable.fromCallable(this::send);
    }
}
