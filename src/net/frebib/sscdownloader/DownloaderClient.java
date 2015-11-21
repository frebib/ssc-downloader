package net.frebib.sscdownloader;

import net.frebib.sscdownloader.concurrent.BatchExecutor;
import net.frebib.util.Log;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

public class DownloaderClient {
    public static final Log LOG = new Log(Level.FINER)
            .setLogOutput(new SimpleDateFormat("'log/mailclient'yyyy-MM-dd hh-mm-ss'.log'")
                    .format(new Date()));

    public static void main(String[] args) {
        try {
            LOG.info("Downloader initialised");

            URL imgur = new URL("http://imgur.com/");                       // 10s
            List<URL> links = WebpageCrawler.parse(imgur, WebpageCrawler.LinkType.Both, 10000);

            BatchExecutor<DownloadTask, DownloadTask> downloader = new BatchExecutor<>(10);
            FileEvaluator eval = new FileEvaluator(MimeTypeCollection.commonImages, 10, d -> downloader.start());
            links.stream().forEach(url -> eval.add(url, "/home/frebib/Downloads/imgur", downloader::add));

            eval.start();
        } catch (Exception e) {
            LOG.exception(e);
        }
    }
}
