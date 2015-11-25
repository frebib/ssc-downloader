package net.frebib.sscdownloader;

import net.frebib.sscdownloader.concurrent.BatchExecutor;
import net.frebib.sscdownloader.concurrent.Completion;
import net.frebib.sscdownloader.concurrent.Task;
import net.frebib.sscdownloader.gui.DownloadClient;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class FileEvaluator {
    private final BatchExecutor<EvalTask, DownloadTask> executor;
    private final MimeTypeCollection mimeTypes;

    private final List<EvalTask> evalTasks;
    private final List<DownloadTask> results;

    public FileEvaluator(MimeTypeCollection mimes, int threadCount, Completion<List<DownloadTask>> done) {
        executor = new BatchExecutor<>(threadCount);
        executor.done(done);
        results = new ArrayList<>();
        evalTasks = new ArrayList<>();
        mimeTypes = mimes;
    }

    public FileEvaluator add(URL url, File directory, Completion<DownloadTask> done) {
        EvalTask task = (EvalTask) new EvalTask(url, directory).done(done);
        executor.add(task);
        evalTasks.add(task);
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

    private class EvalTask extends Task<URL, DownloadTask> {
        private URL url;
        private File directory;
        private String mimeString, filename;

        private boolean fetched;

        public EvalTask(URL url, File directory) {
            super(url);
            this.directory = directory;
        }

        @Override
        public DownloadTask call(URL url) throws Exception {
            try {
                DownloadClient.LOG.finer("Evaluating url: \"" + url.toString() + "\"");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("HEAD");
                conn.connect();

                fetched = true;

                // Test file extension
                File f = new File(url.getFile());
                this.url = conn.getURL();
                filename = f.getCanonicalFile().getName();
                mimeString = conn.getContentType();

                return evalExtension();
            } catch (Exception e) {
                DownloadClient.LOG.exception(e);
            }
            return null;
        }

        private DownloadTask evalExtension() throws IllegalStateException {
            if (!fetched)
                throw new IllegalStateException("Cannot evaluate file, it has not been fetched");

            String ext = "";
            int index = 0;
            index = filename.lastIndexOf('.');
            if (index > 0) {
                ext = filename.substring(index + 1);
                filename = filename.substring(0, index);
            }

            // TODO: Check globally against all MimeTypes
            // Check if mime type is valid
            if (mimeTypes.hasMime(mimeString)) {
                // Use the default extension if it's invalid
                if (!mimeTypes.hasExtension(ext)) {
                    MimeType type = mimeTypes.getMimeType(mimeString);
                    if (type != null)
                        ext = Optional.ofNullable(type.getDefaultExtension()).orElse("");
                }

                if (ext.isEmpty())     // Otherwise have no extension
                    DownloadClient.LOG.warning("File \"" + filename + "\" has no extension with mime: " + mimeString);
            } else
                return null;    // Return null if the mime type isn't valid

            // Append new extension && remove invalid chars
            if (!ext.isEmpty())
                filename = filename.replaceAll("[^a-zA-Z0-9.-]", "_") + '.' + ext;

            return new DownloadTask(url, filename, directory);
        }
    }
}
