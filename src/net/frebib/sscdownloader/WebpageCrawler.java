package net.frebib.sscdownloader;

import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class WebpageCrawler {
    public static List<URL> parse(URL location, LinkType type, int timeoutMillis) throws IOException {
        Connection con = HttpConnection.connect(location);
        con.timeout(timeoutMillis);
        return parse(con.get(), type);
    }

    public static List<URL> parse(Document doc, LinkType type) {
        Elements els = new Elements();
        if (type == LinkType.Anchor || type == LinkType.Both)
            els.addAll(doc.getElementsByTag("a"));
        if (type == LinkType.Image || type == LinkType.Both)
            els.addAll(doc.getElementsByTag("img"));

        // TODO: Remove duplicate links

        return els.stream()
                .map(e -> {
                    // Use `abs:` prefix to attributes to get absolute path
                    if (e.tag().getName().equalsIgnoreCase("a"))
                        return e.attr("abs:href");
                    else if (e.tag().getName().equalsIgnoreCase("img"))
                        return e.attr("abs:src");
                    return null;
                })
                .map(s -> toURL(s, doc.location()))
                .filter(url -> url != null)
                .collect(Collectors.toList());
    }

    private static URL toURL(String address, String parent) {
        try {
            if (address == null || address.isEmpty()) return null;
            if (address.startsWith("//"))                   // Add missing protocol
                address = parent.split("//")[0] + address;  // if link is relative

            DownloaderClient.LOG.finest(address);

            return new URL(address);
        } catch (MalformedURLException e) {
            if (address.isEmpty())
                DownloaderClient.LOG.severe("Address is not valid: " + address);
        }
        return null;
    }

    public enum LinkType {
        Anchor,
        Image,
        Both
    }
}
