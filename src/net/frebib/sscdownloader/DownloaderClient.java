package net.frebib.sscdownloader;

import net.frebib.sscdownloader.concurrent.BatchExecutor;
import net.frebib.sscdownloader.concurrent.Worker;
import net.frebib.sscdownloader.gui.DownloadFrame;
import net.frebib.sscdownloader.gui.DownloadListModel;
import net.frebib.util.Log;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DownloaderClient {
    public static final Log LOG = new Log(Level.FINEST)
            .setLogOutput(new SimpleDateFormat("'log/mailclient'yyyy-MM-dd hh-mm-ss'.log'")
                    .format(new Date()));

    private DownloadFrame frame;
    private DownloadListModel listModel;

    private FileEvaluator eval;
    private BatchExecutor<DownloadTask, DownloadTask> downloader;

    public DownloaderClient() {
        frame = new DownloadFrame();
        frame.btnGo.addActionListener(this::onGoClick);

        listModel = frame.getListModel();
    }

    public void start() {
        frame.setVisible(true);
    }

    public void onGoClick(ActionEvent e) {
        if (!listModel.isEmpty()) {
            try {
                frame.updateStatus(DownloadFrame.Status.DOWNLOADING);
                downloader.start();
            } catch (InterruptedException ex) {
                DownloaderClient.LOG.exception(ex);
            }
            return;
        }

        // TODO: Add null/empty checks on folder and

        URL webpage;
        try {
            webpage = new URL(frame.getURL());
        } catch (MalformedURLException ex) {
            JOptionPane.showMessageDialog(frame, "Invalid URL",
                    "The webpage you entered doesn't appear to be valid.\n\n" +
                            ex.getMessage(), JOptionPane.WARNING_MESSAGE);
            return;
        }
        frame.setURL(webpage.toString());
        final String dir = frame.getSaveDir();
        final int threads = frame.getThreadCount();

        // TODO: Implement filterFrame and MimeType getting
        //MimeTypeCollection mimes = filterFrame.getMimeTypes();
        MimeTypeCollection mimes = MimeTypeCollection.COMMON_IMAGES;
        eval = new FileEvaluator(mimes, threads, tasks -> {
            frame.btnGo.setEnabled(true);
            frame.updateStatus(DownloadFrame.Status.GRABBED);
            frame.setDownloadCount(0);

            tasks = tasks.stream()
                    .filter(x -> x != null)
                    .collect(Collectors.toList());
            tasks.stream().forEach(t ->
                    t.done(r -> frame.incDownloadCount()));

            downloader = new BatchExecutor<>(threads);
            downloader.done(res -> frame.updateStatus(DownloadFrame.Status.DOWNLOADED));
            downloader.addAll(tasks);
        });

        new Worker<URL, List<URL>>()
                .todo(url -> WebpageCrawler.parse(url, WebpageCrawler.LinkType.Both, 30000))
                .done(links -> {
                    frame.setDownloadCount(links.size());
                    frame.updateStatus(DownloadFrame.Status.GRABBING);
                    links.stream().forEach(url ->
                            eval.add(url, dir, dl -> {
                                listModel.addElement(dl);
                                frame.decDownloadCount();
                            }));
                    frame.btnGo.setEnabled(false);
                    eval.start();
                }).error(ex -> {
                    String strace = ex.getMessage() +
                            Arrays.stream(ex.getStackTrace())
                            .limit(5)
                            .map(StackTraceElement::toString)
                            .collect(Collectors.joining("\n"));

                    JOptionPane.showMessageDialog(frame, "Failed to Connect\n" + strace + ex.getMessage(),
                            "Failed to Connect", JOptionPane.INFORMATION_MESSAGE);
                })
                .start(webpage);
    }

    public static void main(String[] args) {
        LOG.info("Downloader initialised");

        DownloaderClient client = new DownloaderClient();
        client.start();
    }
}
