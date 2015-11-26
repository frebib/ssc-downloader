package net.frebib.sscdownloader.concurrent;

import java.util.ArrayList;
import java.util.List;

/**
 * A flexible asynchronous task executor which supports
 * supports argument passing and callbacks upon completion
 * @param <T> type of input data
 * @param <R> type of return data
 */
public class Worker<T, R> {
    private boolean cancelled = false, complete;
    private Thread thread;
    private Function<T, R> task;
    private List<Completion<R>> done;
    private Progress prog;
    private Throwable error;

    private T t;

    /**
     * Creates a new Worker with a task
     * @param task
     */
    public Worker(Function<T, R> task) {
        this();
        this.task = task;
    }

    /**
     * Creates a new named worker
     * @param name
     */
    public Worker(String name) {
        this();
        thread.setName(name);
    }

    /**
     * Creates a default worker with no name
     */
    public Worker() {
        this.done = new ArrayList<>();

        thread = new Thread(() -> {
            try {
                R r = task.call(t);
                if (!cancelled)
                    for (Completion<R> handler : done)
                        handler.onComplete(r);
                complete = true;
            } catch (Exception ex) {
                if (error != null)
                    error.onError(ex);
                else {
                    ex.printStackTrace();
                    System.exit(1);
                }
            }
        });
    }

    /**
     * Adds a job for the worker to execute. Required
     * @param task job to execute
     * @return the Worker instance for call chaining
     */
    public Worker<T, R> todo(Function<T, R> task) {
        this.task = task;
        return this;
    }

    /**
     * Adds a completion callback function that is called when
     * the worker has completed. Many callbacks can be added
     * @param done the callback
     * @return the Worker instance for call chaining
     */
    public Worker<T, R> done(Completion<R> done) {
        this.done.add(done);
        return this;
    }

    /**
     * Catches any exceptions that may occur inside the worker and
     * requires them to be handled here, otherwise they will just
     * be logged
     * @param error error handler
     * @return the Worker instance for call chaining
     */
    public Worker<T, R> error(Throwable error) {
        this.error = error;
        return this;
    }

    /**
     * A progress callback that the worker can call from within
     * it's task
     * @param prog handler for the progress event
     * @return the Worker instance for call chaining
     */
    public Worker<T, R> progress(Progress prog) {
        this.prog = prog;
        return this;
    }

    /**
     * Starts the worker asynchronously in a new thread and
     * passes the value to the worker function
     * @param t argument to pass to the worker
     * @return the Worker instance for call chaining
     */
    public Worker<T, R> start(T t) {
        this.t = t;
        thread.start();
        return this;
    }

    /**
     * Gets whether the worker has completed it's operation
     * @return true if the worker has completed
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * Attempts to cancel the worker operation. This will prevent
     * the callback from being called if/when the task completes
     */
    public void cancel() {
        cancelled = true;
    }
}
