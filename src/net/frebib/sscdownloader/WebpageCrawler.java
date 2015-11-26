package net.frebib.sscdownloader;

import net.frebib.sscdownloader.gui.DownloadClient;
import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Downloads a webpage and harvests all of the contained
 * links in <a/> or <img/> HTML tags
 */
public class WebpageCrawler {

    /**
     * Extracts all links from a given {@link URL}
     * @param location webpage to fetch and extract links from
     * @param type types of links to extract
     * @param timeoutMillis
     * @return
     * @throws IOException if the connection fails
     */
    public static List<URL> parse(URL location, LinkType type, int timeoutMillis) throws IOException {
        Connection con = HttpConnection.connect(location);
        con.timeout(timeoutMillis);
        return parse(con.get(), type);
    }

    /**
     * Extracts all links from a given JSoup {@link Document}
     * @param doc the document to extract links from
     * @param type types of links to extract
     * @return a list of links
     */
    public static List<URL> parse(Document doc, LinkType type) {
        Elements els = new Elements();
        if (type == LinkType.Anchor || type == LinkType.Both)
            els.addAll(doc.getElementsByTag("a"));
        if (type == LinkType.Image || type == LinkType.Both)
            els.addAll(doc.getElementsByTag("img"));

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
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Converts a web address into a {@link URL} object
     * @param address address to convert
     * @param parent the sender/parent of the web address
     * @return a {@link URL} object representing the address
     */
    private static URL toURL(String address, String parent) {
        try {
            if (address == null || address.isEmpty()) return null;
            if (address.startsWith("//") && parent != null) // Add missing protocol
                address = parent.split("//")[0] + address;  // if link is relative

            DownloadClient.LOG.finest(address);

            return new URL(address);
        } catch (MalformedURLException e) {
            if (address.isEmpty())
                DownloadClient.LOG.severe("Address is not valid: " + address);
        }
        return null;
    }

    /**
     * Represents <a/> and <img/> tags
     */
    public enum LinkType {
        Anchor,
        Image,
        Both
    }
}
