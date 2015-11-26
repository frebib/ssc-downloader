package net.frebib.sscdownloader.concurrent;

public interface Progress {
    /**
     * A callback for progress events for the worker framework
     * @param progress the progress value from the worker thread
     * @param max the highest value that the progress can be
     */
    void onProgress(int progress, int max);
}
