package net.frebib.sscdownloader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MimeType {
    private String mime;
    private ArrayList<String> exts;

    public MimeType(String mimetype, String... extensions) {
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
    public String getMime() {
        return mime;
    }
    public List<String> getExtensions() {
        return exts;
    }

    public boolean hasExtension(String extension) {
        return exts.contains(extension.toLowerCase());
    }

    public boolean matches(String mimetype) {
        if (!isValid(mimetype))
            return false;

        String[] parts = mimetype.split(";")[0].split("/");
        String[] thisparts = mime.split(";")[0].split("/");

        boolean validA = parts[0].equalsIgnoreCase(thisparts[0]) || parts[0].equals("*");
        boolean validB = parts[1].equalsIgnoreCase(thisparts[1]) || parts[1].equals("*");
        return  validA && validB;
    }

    public String getDefaultExtension(){
        return exts.get(0);
    }

    public static boolean isValid(String mimetype) {
        return mimetype.matches("^[\\w]+/[-\\w-.+*;]+$");
    }
}
