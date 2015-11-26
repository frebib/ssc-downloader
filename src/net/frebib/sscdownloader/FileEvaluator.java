package net.frebib.sscdownloader;

import net.frebib.sscdownloader.concurrent.BatchExecutor;
import net.frebib.sscdownloader.concurrent.Completion;
import net.frebib.sscdownloader.concurrent.Task;
import net.frebib.sscdownloader.gui.DownloadClient;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Downloads file headers asynchronously and ensures it has the correct extension; if not
 * it tries to find a correct extension match from the given {@link MimeTypeCollection}
 */
public class FileEvaluator {
    private MimeTypeCollection mimeTypes;
    private BatchExecutor<EvalTask, DownloadTask> executor;

    private final List<EvalTask> evalTasks;

    /**
     * Creates a new FileEvaluator instance
     * @param mimes {@link MimeTypeCollection} to match missing extensions to
     * @param threadCount amount of downloader threads to use for asynchronous operation
     * @param done a {@link Completion} callback that is called when the operation is
     *             complete with the resulting {@link DownloadTask} objects to be executed
     */
    public FileEvaluator(MimeTypeCollection mimes, int threadCount, Completion<List<DownloadTask>> done) {
        executor = new BatchExecutor<>(threadCount);
        executor.done(done);
        evalTasks = new ArrayList<>();
        mimeTypes = mimes;
    }

    /**
     * Adds a URL to the queue to be evaluated
     * @param url url to evaluate
     * @param directory save directory to be inserted into the {@link DownloadTask} object
     * @param done a {@link Completion} callback that is called when link has been evaluated
     * @return the FileEvaluator instance, for command chaining
     */
    public FileEvaluator add(URL url, File directory, Completion<DownloadTask> done) {
        EvalTask task = (EvalTask) new EvalTask(url, directory).done(done);
        executor.add(task);
        evalTasks.add(task);
        return this;
    }

    /**
     * Sets the {@link MimeTypeCollection} to match file extensions to
     * @param mimeTypes the collection
     */
    public void setMimeTypes(MimeTypeCollection mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    /**
     * Starts processing the queue of tasks to evaluate,
     * asynchronously with the amount of threads specified in the constructor
     * @return the FileEvaluator instance, for command chaining
     * @throws InterruptedException throws if there is an error starting the executor
     */
    public FileEvaluator start() throws InterruptedException {
        executor.start();
        return this;
    }

    /**
     * Waits for all submitted tasks to end execution then terminates the executor
     * @return the FileEvaluator instance, for command chaining
     */
    public FileEvaluator shutdown() {
        executor.shutdown();
        return this;
    }

    /**
     * Gets the entire list of tasks ever submitted to the {@link FileEvaluator}
     * @return a list of tasks
     */
    public List<EvalTask> getTasks() {
        return evalTasks;
    }

    /**
     * A Task that can be executed by a {@link BatchExecutor} or other {@link ExecutorService}
     */
    public class EvalTask extends Task<URL, DownloadTask> {
        private URL url;
        private File directory;
        private String mimeString, filename;

        private boolean fetched;

        /**
         * Creates a new EvalTask
         * @param url the {@link URL} to get data from and evaluate
         * @param directory the save directory of the resulting {@link DownloadTask}
         */
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

        /**
         * A synchronous method to evaluate a file extension,
         * provided it's header has been fetched from the url
         * @return a {@link DownloadTask} that can be executed
         * @throws IllegalStateException throws if the header hasn't been fetched
         */
        public DownloadTask evalExtension() throws IllegalStateException {
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
