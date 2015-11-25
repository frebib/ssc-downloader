package net.frebib.sscdownloader;

import net.frebib.sscdownloader.concurrent.Task;
import net.frebib.sscdownloader.gui.DownloadClient;

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

    private long size;
    private long bytes;
    private float progress;

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

    public void pause() {
        dlState = State.PAUSED;
    }

    public synchronized void resume() {
        dlState = State.DOWNLOADING;
        notifyAll();
    }

    public void cancel() {
        DownloadClient.LOG.warning("Download cancelled: " + this.hashCode());
        setState(State.CANCELLED);
    }

    public long getBytes() {
        return bytes;
    }
    public long getSize() {
        return size;
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
    public URL getURL() {
        return url;
    }

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

        public short getValue() {
            return v;
        }
        public Color getCol() {
            return col;
        }
    }
}
