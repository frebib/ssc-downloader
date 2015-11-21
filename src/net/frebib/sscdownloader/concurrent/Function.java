package net.frebib.sscdownloader.concurrent;

public interface Function<T, R> {
    R call(T t) throws Exception;
}
