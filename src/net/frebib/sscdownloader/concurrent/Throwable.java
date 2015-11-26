package net.frebib.sscdownloader.concurrent;

public interface Throwable {
    /**
     * An error handler for the worker framework
     * @param e exception that is thrown
     */
    void onError(Exception e);
}
