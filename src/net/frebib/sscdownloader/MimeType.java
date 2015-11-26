package net.frebib.sscdownloader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Represents a file type in terms of it's MimeType
 * and matching file extensions
 */
public class MimeType {
    /**
     * A {@link MimeType} that matches any file type
     */
    public static final MimeType WILDCARD = new MimeType("*/*");
    private String mime;
    private ArrayList<String> exts;

    /**
     * Creates a {@link MimeType} with a type and an array of matching extensions
     * @param mimetype mimetype
     * @param extensions matching extensions
     * @throws IllegalArgumentException thrown if the {@code mimetype} is not valid
     */
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

    /**
     * Creates a {@link MimeType} with a type and a delimited {@link String} of extensions
     * @param mime mimetype
     * @param ext delimited {@link String} of extensions
     * @return a MimeType object
     * @throws IllegalArgumentException thrown if the {@code mimetype} is not valid
     */
    public static MimeType fromDelimitedExts(String mime, String ext)  throws IllegalArgumentException {
        String[] exts = ext.toLowerCase().replaceAll("\\.", "").split("[\\W]+");
        return new MimeType(mime, Arrays.stream(exts).filter(s ->  !s.isEmpty()).toArray(String[]::new));
    }

    /**
     * Gets the mimetype
     */
    public String getMime() {
        return mime;
    }

    /**
     * Gets the extensions that match the {@link MimeType}
     * @return a {@link List} of extensions
     */
    public List<String> getExtensions() {
        return exts;
    }

    /**
     * Gets the extensions that match the mimetype in a delimited {@link String}
     * @param delim delimiter to use
     * @param withDots whether to include dots before the extension
     * @return a delimited {@link String} of extensions
     */
    public String getDelimitedExts(String delim, boolean withDots) {
        if (withDots)
            return exts.stream().map(s -> '.' + s).collect(Collectors.joining(delim));
        else
            return exts.stream().collect(Collectors.joining(delim));
    }

    /**
     * Gets whether the {@link MimeType} matches a particular extension
     * @param extension the extension to check
     * @return true if the extension matches the {@link MimeType}
     */
    public boolean hasExtension(String extension) {
        return exts.contains(extension.toLowerCase()) || mime.equals("*/*");
    }

    /**
     * Compares a mimetype for match equality.
     * Equality includes wildcards so X/* matches X/Y
     * Also *\/Y mathes X/Y
     * *\/* matches anything
     * @param mimetype mimetype to test
     * @return true if there is a match equality
     */
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

    /**
     * Gets the first extension that matches the {@link MimeType}
     * @return the first extension that matches or null if there are none
     */
    public String getDefaultExtension(){
        return exts.size() > 0 ? exts.get(0) : null;
    }

    /**
     * Checks a {@code mimetype} for format correctness,
     * following the pattern of ^[abc|*]/[abc|*]$
     * @param mimetype {@code mimetype} to test for validity
     * @return true if the {@code mimetype} is valid
     */
    public static boolean isValid(String mimetype) {
        return mimetype != null && mimetype.matches("^[a-zA-Z*]+/[a-zA-Z-.+*]+[\\s?;*]?(\\s*?.*)");
    }

    @Override
    public String toString() {
        return mime + " => " + getDelimitedExts(", ", true);
    }
}
