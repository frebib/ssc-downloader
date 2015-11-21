package net.frebib.sscdownloader.concurrent;

public interface Progress {
    void onProgress(int progress, int max);
}
