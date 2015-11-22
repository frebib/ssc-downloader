package net.frebib.sscdownloader.concurrent;

public class Worker<T, R> {
    private boolean cancelled = false, complete;
    private Thread thread;
    private Function<T, R> task;
    private Completion<R> done;
    private Progress prog;
    private Throwable error;

    private T t;

    public Worker(Function<T, R> task) {
        this();
        this.task = task;
    }
    public Worker(String name) {
        this();
        thread.setName(name);
    }
    public Worker() {
        thread = new Thread(() -> {
            try {
                R r = task.call(t);
                if (!cancelled && done != null)
                    done.onComplete(r);
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

    public Worker<T, R> todo(Function<T, R> task) {
        this.task = task;
        return this;
    }
    public Worker<T, R> done(Completion<R> done) {
        this.done = done;
        return this;
    }

    public Worker<T, R> error(Throwable error) {
        this.error = error;
        return this;
    }

    public Worker<T, R> progress(Progress prog) {
        this.prog = prog;
        return this;
    }

    public Worker<T, R> start(T t) {
        this.t = t;
        thread.start();
        return this;
    }

    public boolean isComplete() {
        return complete;
    }

    public void cancel() {
        cancelled = true;
    }
}
