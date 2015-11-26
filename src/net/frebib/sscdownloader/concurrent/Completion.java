package net.frebib.sscdownloader.concurrent;

public interface Completion<E> {
    /**
     * A completion callback event handler for the worker framework
     * @param e a return value passed back from the worker
     * @throws Exception any exception that may throw from the
     *         completion handler. Should be caught by a {@link Throwable}
     */
    void onComplete(E e) throws Exception;
}
