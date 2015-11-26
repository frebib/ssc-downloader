package net.frebib.sscdownloader;

import net.frebib.sscdownloader.concurrent.Task;
import net.frebib.sscdownloader.gui.DownloadClient;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A {@link Task} to download a file to a specified directory
 * with progess and completion callbacks.
 * Can be cancelled and paused
 */
public class DownloadTask extends Task<URL, DownloadTask> {
    private final int CHUNK_SIZE = 4096;

    private File file;
    private URL url;
    private State dlState;

    private long size;
    private long bytes;
    private float progress;

    /**
     * Creates a new DownloadTask
     * @param url link to fetch the file from
     * @param filename name of the file to save
     * @param path location to save the file
     */
    public DownloadTask(URL url, String filename, File path) {
        super(url);

        this.url = url;
        this.file = new File(path, filename);
        this.dlState = State.UNINITIALISED;
    }

    @Override
    public DownloadTask call(URL url) throws Exception {
        if (dlState == State.CANCELLED)
            return this;

        DownloadClient.LOG.fine("Download starting for: " + file.getCanonicalPath());
        setState(State.INITIALISED);

        HttpURLConnection conn;
        FileOutputStream fs = null;
        InputStream is = null;

        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.connect();

            int response = conn.getResponseCode();
            if (response < 200 || response > 299) {
                setState(State.ERROR);
                return this;
            }
            size = conn.getContentLength();
            if (size < 1) {
                setState(State.ERROR);
                return this;
            }

            file.getParentFile().mkdirs();
            fs = new FileOutputStream(file);
            is = conn.getInputStream();

            setState(State.DOWNLOADING);

            synchronized (this) {
                while (dlState == State.DOWNLOADING || dlState == State.PAUSED) {
                    if (dlState == State.PAUSED)
                        wait();

                    int chunk = (int) Math.min(CHUNK_SIZE, size - bytes);
                    byte[] buffer = new byte[chunk];

                    int count = is.read(buffer);
                    if (count == -1) {              // At EOF
                        setState(State.COMPLETED);
                        break;
                    } else {                        // Write bytes to file
                        fs.write(buffer, 0, count);
                        bytes += count;
                        setProgress((float) bytes / size * 100);
                    }
                }
            }
        } catch (Exception e) {
            setState(State.ERROR);
            DownloadClient.LOG.exception(e);
        } finally {
            try {
                if (fs != null)
                    fs.close();
                if (is != null)
                    is.close();
            } catch (Exception e) {
                DownloadClient.LOG.exception(e);
            }
        }
        DownloadClient.LOG.fine("Download finished for: " + file.getCanonicalPath());
        return this;
    }

    private void setState(State state) {
        if (state == State.CANCELLED && dlState.getValue() > State.PAUSED.getValue())
            return;
        dlState = state;
        setChanged();
        notifyObservers(state);
    }
    private void setProgress(float percent) {
        progress = percent;
        setChanged();
        notifyObservers(percent);
    }

    /**
     * Pauses the download. Can cause the download
     * to fail if paused for too long
     */
    public void pause() {
        if (dlState.getValue() > State.INITIALISED.getValue())
            dlState = State.PAUSED;
    }

    /**
     * Resumes a paused download
     */
    public synchronized void resume() {
        if (dlState !=  State.PAUSED) return;
        dlState = State.DOWNLOADING;
        notifyAll();
    }

    /**
     * Cancels the download
     */
    public void cancel() {
        DownloadClient.LOG.warning("Download cancelled: " + this.hashCode());
        setState(State.CANCELLED);
    }

    /**
     * Gets the amount of the file that is downloaded in bytes
     */
    public long getBytes() {
        return bytes;
    }

    /**
     * Gets the length of the download in bytes
     */
    public long getSize() {
        return size;
    }

    /**
     * Gets the progress of the download as a percentage
     */
    public float getProgress() {
        return progress;
    }

    /**
     * Gets the state of the download
     */
    public State getState() {
        return dlState;
    }

    /**
     * Gets the name of the file that is/will be saved
     */
    public String getFilename() {
        return file.getName();
    }

    /**
     * Gets the full absolute path where the file is saved
     */
    public String getFilepath() {
        return file.getAbsolutePath();
    }

    /**
     * Gets the {@link URL} from where the file is downloaded
     */
    public URL getURL() {
        return url;
    }

    /**
     * Used to represent the exact state of a {@link DownloadTask}
     */
    public enum State {
        UNINITIALISED(new Color(30, 80, 170), 1),
        INITIALISED(new Color(255, 255, 0), 2),
        DOWNLOADING(new Color(255, 115, 0), 4),
        PAUSED(new Color(200, 0, 200), 8),
        COMPLETED(new Color(0, 115, 0), 16),
        CANCELLED(new Color(100, 100, 100), 32),
        ERROR(new Color(255, 0, 0), 64);

        private Color col;
        private short v;

        State(Color col, int val) {
            this.col = col;
            this.v = (short) val;
        }

        /**
         * Gets the numeric value of the State
         */
        public short getValue() {
            return v;
        }

        /**
         * Gets the associated colour from the State
         * @return the associated colour
         */
        public Color getCol() {
            return col;
        }
    }
}
