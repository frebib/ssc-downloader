package net.frebib.sscdownloader;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Observable;
import java.util.concurrent.*;

public class DownloadTask extends Observable implements Callable<DownloadTask> {
    private final int CHUNK_SIZE = 4096;

    private File file;
    private URL url;
    private State dlState;

    private float progress = 0f;

    public DownloadTask(URL url, String filename, String path) {
        this.url = url;
        this.file = new File(path, filename);
        this.dlState = State.UNINITIALISED;
    }

    @Override
    public DownloadTask call() throws Exception {
        DownloaderClient.LOG.fine("Download starting for: " + file.getCanonicalPath());

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
            int len = conn.getContentLength();
            if (len < 1) {
                setState(State.ERROR);
                return this;
            }

            file.getParentFile().mkdirs();
            fs = new FileOutputStream(file);
            is = conn.getInputStream();

            setState(State.DOWNLOADING);

            int downloaded = 0;
            while (dlState == State.DOWNLOADING) {
                int chunk = Math.min(CHUNK_SIZE, len - downloaded);
                byte[] buffer = new byte[chunk];

                int count = is.read(buffer);
                if (count == -1) {              // At EOF
                    setState(State.COMPLETED);
                    break;
                } else {                        // Write bytes to file
                    fs.write(buffer, 0, count);
                    downloaded += count;
                    setProgress((float) downloaded / len * 100);
                }
            }
        } catch (Exception e) {
            setState(State.ERROR);
            DownloaderClient.LOG.exception(e);
        } finally {
            try {
                if (fs != null)
                    fs.close();
                if (is != null)
                    is.close();
            } catch (Exception e) {
                DownloaderClient.LOG.exception(e);
            }
        }
        DownloaderClient.LOG.fine("Download finished for: " + file.getCanonicalPath());
        return this;
    }

    private void setState(State state) {
        dlState = state;
        setChanged();
        notifyObservers(state);
    }
    private void setProgress(float percent) {
        progress = percent;
        setChanged();
        notifyObservers(percent);
    }

    public void cancel() {
        DownloaderClient.LOG.warning("Download cancelled: " + this.hashCode());
        setState(State.CANCELLED);
    }
    public float getProgress() {
        return progress;
    }
    public State getState() {
        return dlState;
    }
    public String getFilename() {
        return file.getName();
    }
    public String getFilepath() {
        return file.getAbsolutePath();
    }

    public enum State {
        UNINITIALISED(Color.BLUE, 1),
        INITIALISED(Color.YELLOW, 2),
        DOWNLOADING(Color.ORANGE, 4),
        COMPLETED(Color.GREEN, 8),
        CANCELLED(Color.GRAY, 16),
        ERROR(Color.RED, 32);

        private Color col;
        private short v;

        State(Color col, int val) {
            this.col = col;
            this.v = (short) val;
        }

        public Color getCol() {
            return col;
        }
    }
}
