package net.frebib.sscdownloader;

import net.frebib.sscdownloader.concurrent.Task;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask extends Task<URL, DownloadTask> {
    private final int CHUNK_SIZE = 4096;

    private File file;
    private URL url;
    private State dlState;

    private float progress = 0f;

    public DownloadTask(URL url, String filename, File path) {
        super(url);

        this.url = url;
        this.file = new File(path, filename);
        this.dlState = State.UNINITIALISED;
    }

    @Override
    public DownloadTask call(URL url) throws Exception {
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
    public String getURL() {
        return url.toString();
    }

    public enum State {
        UNINITIALISED(new Color(30, 80, 170), 1),
        INITIALISED(new Color(255, 255, 0), 4),
        DOWNLOADING(new Color(255, 115, 0), 2),
        COMPLETED(new Color(0, 115, 0), 8),
        CANCELLED(new Color(100, 100, 100), 16),
        ERROR(new Color(255, 0, 0), 32);

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
