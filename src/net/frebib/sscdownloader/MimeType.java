package net.frebib.sscdownloader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class MimeType {
    public static final MimeType WILDCARD = new MimeType("*/*");
    private String mime;
    private ArrayList<String> exts;

    public MimeType(String mimetype, String... extensions) throws IllegalArgumentException {
        if (!isValid(mimetype))
            throw new IllegalArgumentException("\"" + mimetype + "\" is not a valid Mime Type");
        mime = mimetype;
        exts = new ArrayList<>();
        exts.addAll(Arrays.asList(extensions)
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList())
        );
    }
    public static MimeType fromDelimitedExts(String mime, String ext)  throws IllegalArgumentException {
        String[] exts = ext.toLowerCase().replaceAll("\\.", "").split("[\\W]+");
        return new MimeType(mime, exts);
    }

    public String getMime() {
        return mime;
    }
    public List<String> getExtensions() {
        return exts;
    }
    public String getDelimitedExts(String delim, boolean withDots) {
        if (withDots)
            return exts.stream().map(s -> '.' + s).collect(Collectors.joining(delim));
        else
            return exts.stream().collect(Collectors.joining(delim));
    }

    public boolean hasExtension(String extension) {
        return exts.contains(extension.toLowerCase()) || mime.equals("*/*");
    }

    public boolean matches(String mimetype) {
        if (!isValid(mimetype))
            return false;

        String[] parts = mimetype.split(";")[0].split("/");
        String[] thisparts = mime.split(";")[0].split("/");

        // TODO: Update this to work with partial wildcard matching such as "text/ht*"
        boolean validA = parts[0].equalsIgnoreCase(thisparts[0]) || parts[0].equals("*") || thisparts[0].equals("*");
        boolean validB = parts[1].equalsIgnoreCase(thisparts[1]) || parts[1].equals("*") || thisparts[1 ].equals("*");
        return  validA && validB;
    }

    public String getDefaultExtension(){
        return exts.size() > 0 ? exts.get(0) : null;
    }

    public static boolean isValid(String mimetype) {
        return mimetype.matches("^[a-zA-Z*]+/[a-zA-Z-.+*]+[\\s?;*]?(\\s*?.*)");
    }

    @Override
    public String toString() {
        return mime + " => " + getDelimitedExts(", ", true);
    }
}
