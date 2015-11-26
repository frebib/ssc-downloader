package net.frebib.sscdownloader.concurrent;

public interface Function<T, R> {
    /**
     * A function to execute for the worker framework
     * @param t the intial argument passed when starting the worker
     * @return the return value from the worker function
     * @throws Exception any exception that is thrown from within
     *         the function. Should be caught by a {@link Throwable}
     */
    R call(T t) throws Exception;
}
