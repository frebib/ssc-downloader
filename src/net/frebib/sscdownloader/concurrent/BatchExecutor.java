package net.frebib.sscdownloader.concurrent;

import net.frebib.sscdownloader.DownloaderClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * Wraps a {@link ThreadPoolExecutor} and batch executes all
 * added tasks, then disposes of the {@link ThreadPoolExecutor}
 * @param <T> {@link Callable} task to execute
 * @param <R> Return type of the {@link Callable} task
 */
public class BatchExecutor<T extends Task<?, R>, R> implements Completion<R> {
    private ThreadPoolExecutor pool;
    private Collection<T> tasks;
    private List<Completion<List<R>>> dones;
    private List<R> results;
    private int completedTasks;

    /**
     * Initialises a batch executor with an empty queue and set amount of threads
     * @param threadCount Amount of threads to use for execution of tasks
     */
    public BatchExecutor(int threadCount) {
        this.tasks = new ArrayList<>();
        this.dones = new ArrayList<>();
        this.pool  = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount);
        this.results = new ArrayList<R>(this.tasks.size());
    }

    public BatchExecutor<T, R> done(Completion<List<R>> done) {
        dones.add(done);
        return this;
    }

    /**
     * Adds a task to the queue
     * @param task Task to add
     */
    public BatchExecutor add(T task) {
        task.done(this);
        tasks.add(task);
        return this;
    }
    /**
     * Adds a task to the queue
     * @param tasks Tasks to add
     */
    public BatchExecutor addAll(Collection<? extends T> tasks) {
        tasks.stream().forEach(t -> t.done(this));
        this.tasks.addAll(tasks);
        return this;
    }

    @Override
    public synchronized void onComplete(R r) throws Exception {
        results.add(r);

        if (results.size() >= tasks.size()) {
            shutdown();
            dones.stream().forEachOrdered(d -> {
                if (d != null)
                    try {
                        d.onComplete(results);
                    } catch (Exception e) {
                        DownloaderClient.LOG.exception(e);
                    }
            });
        }
    }

    /**
     * Executes all submitted tasks
     * @throws InterruptedException
     * @returns A list of Future objects corresponding to each
     * {@link Callable} task that was submitted for execution
     */
    public void start() throws InterruptedException {
        this.tasks.stream()
                .filter(t -> t != null)
                .forEach(pool::submit);
    }

    /**
     * Terminates the execution pool and disposes
     * of it so it cannot be used again
     */
    public void shutdown() {
        pool.shutdown();
    }

    /**
     * Gets the underlying {@link Executor} that runs the tasks
     * @return A {@link ThreadPoolExecutor} object
     */
    public ThreadPoolExecutor getPool() {
        return pool;
    }
}
