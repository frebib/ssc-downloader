package net.frebib.sscdownloader.concurrent;

import net.frebib.sscdownloader.DownloaderClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public abstract class Task<T, R> implements Callable<R>, Function<T, R> {
    protected final List<Completion<R>> done;
    private final T t;

    public Task(T t) {
        this.done = new ArrayList<>();
        this.t = t;
    }

    public Task<T, R> done(Completion<R>... done) {
        this.done.addAll(Arrays.asList(done));
        return this;
    }

    @Override
    public R call() throws Exception {
        R r = this.call(t);
        done.stream().forEach(d -> {
            try {
                if (d != null)
                    d.onComplete(r);
            } catch (Exception e) {
                DownloaderClient.LOG.exception(e);
            }
        });
        return r;
    }
}
