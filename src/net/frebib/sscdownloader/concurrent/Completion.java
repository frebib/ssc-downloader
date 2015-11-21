package net.frebib.sscdownloader.concurrent;

public interface Completion<E> {
    void onComplete(E e) throws Exception;
}
