package net.frebib.sscdownloader;

import net.frebib.sscdownloader.concurrent.BatchExecutor;
import net.frebib.sscdownloader.concurrent.Completion;
import net.frebib.sscdownloader.concurrent.Task;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileEvaluator implements Completion<DownloadTask> {
    private final BatchExecutor<Task<URL, DownloadTask>, DownloadTask> executor;
    private final MimeTypeCollection mimeTypes;

    private final List<DownloadTask> results;
    private final Completion<List<DownloadTask>> done;

    public FileEvaluator(MimeTypeCollection mimes, int threadCount, Completion<List<DownloadTask>> done) {
        executor = new BatchExecutor<>(threadCount);
        results = new ArrayList<>();
        mimeTypes = mimes;
        this.done = done;
    }

    public FileEvaluator add(URL url, String directory, Completion<DownloadTask> done) {
        executor.add(new EvalTask(url, directory).done(done, this));
        return this;
    }

    public FileEvaluator start() throws InterruptedException {
        executor.start();
        return this;
    }

    public FileEvaluator shutdown() {
        executor.shutdown();
        return this;
    }

    @Override
    public void onComplete(DownloadTask task) throws Exception {
        if (task != null)
            results.add(task);

        if (executor.getPool().getQueue().size() < 1) {
            executor.shutdown();
            if (done != null)
                done.onComplete(results);
        }
    }

    private class EvalTask extends Task<URL, DownloadTask> {
        private String directory;

        public EvalTask(URL url, String directory) {
            super(url);
            this.directory = directory;
        }

        @Override
        public DownloadTask call(URL url) throws Exception {
            try {
                DownloaderClient.LOG.finer("Evaluating file at \"" + url.toString() + "\"");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("HEAD");
                conn.connect();

                // Test file extension
                String mimeStr = conn.getContentType();
                File f = new File(conn.getURL().getFile());
                String filename = f.getCanonicalFile().getName();

                String ext = "";
                int index = 0;
                index = filename.lastIndexOf('.');
                if (index > 0) {
                    ext = filename.substring(index + 1);
                    filename = filename.substring(0, index);
                }

                // Check if mime type is valid
                if (mimeTypes.hasMime(mimeStr)) {
                    // Use the default extension if it's invalid
                    if (!mimeTypes.hasExtension(ext)) {
                        MimeType type = mimeTypes.getMimeType(mimeStr);
                        if (type != null)
                            ext = Optional.ofNullable(type.getDefaultExtension()).orElse("");
                    }

                    if (ext.isEmpty())     // Otherwise have no extension
                        DownloaderClient.LOG.warning("File \"" + filename + "\" has no extension with mime: " + mimeStr);
                } else
                    return null;    // Return null if the mime type isn't valid

                // Append new extension && remove invalid chars
                if (!ext.isEmpty())
                    filename = filename.replaceAll("[^a-zA-Z0-9.-]", "_") + '.' + ext;

                return new DownloadTask(url, filename, directory);
            } catch (Exception e) {
                DownloaderClient.LOG.exception(e);
            }
            return null;
        }
    }
}
