package net.frebib.sscdownloader.concurrent;

import net.frebib.sscdownloader.gui.DownloadClient;

import java.util.*;
import java.util.concurrent.Callable;

public abstract class Task<T, R> extends Observable implements Callable<R>, Function<T, R> {
    protected final List<Completion<R>> done;
    private final T t;

    public Task(T t) {
        this.done = new ArrayList<>();
        this.t = t;
    }

    /**
     * Adds a {@link Completion} event to the worker Task
     * @param done completion event handler to add
     * @return the Task instance for function call chaining
     */
    public Task<T, R> done(Completion<R> done) {
        this.done.add(done);
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
                DownloadClient.LOG.exception(e);
            }
        });
        return r;
    }
}
