package net.frebib.sscdownloader.concurrent;

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
public class BatchExecutor<T extends Callable<R>, R> {
    private ThreadPoolExecutor pool;
    private Collection<T> tasks;

    /**
     * Initialises a batch executor with an empty queue and set amount of threads
     * @param threadCount Amount of threads to use for execution of tasks
     */
    public BatchExecutor(int threadCount) {
        this.tasks = new ArrayList<T>();
        this.pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount);
    }

    /**
     * Adds a task to the queue
     * @param task Task to add
     */
    public BatchExecutor add(T task) {
        tasks.add(task);
        return this;
    }
    /**
     * Adds a task to the queue
     * @param tasks Tasks to add
     */
    public BatchExecutor addAll(Collection<? extends T> tasks) {
        this.tasks.addAll(tasks);
        return this;
    }

    /**
     * Executes all submitted tasks
     * @throws InterruptedException
     * @returns A list of Future objects corresponding to each
     * {@link Callable} task that was submitted for execution
     */
    public void start() throws InterruptedException {
        pool.invokeAll(this.tasks);
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
