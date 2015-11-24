package net.frebib.sscdownloader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MimeTypeCollection {
    public static final MimeTypeCollection[] DEF_COLLECTIONS;

    public static final MimeTypeCollection WILDCARD = new MimeTypeCollection("Any Type")
            .add(MimeType.WILDCARD);

    public static final MimeTypeCollection COMMON_IMAGES = new MimeTypeCollection("Common Image Formats")
            .add(new MimeType("image/gif",  "gif"))
            .add(new MimeType("image/png",  "png"))
            .add(new MimeType("image/bmp",  "bmp", "bm"))
            .add(new MimeType("image/tiff", "tiff", "tif"))
            .add(new MimeType("image/jpeg", "jpg", "jpe", "jpeg", "jps", "jfif"));

    public static final MimeTypeCollection COMMON_TEXTS = new MimeTypeCollection("Common Text Formats")
            .add(new MimeType("text/plain", "txt", "text", "log", "list"))
            .add(new MimeType("text/html",  "html", "htm", "php", "htmls", "xhtml", "htx", "shtml"))
            .add(new MimeType("text/xml",   "xml"))
            .add(new MimeType("text/richtext",   "rt", "rtf", "rtx"))
            .add(new MimeType("text/ecmascript", "js"))
            .add(new MimeType("text/javascript", "js"));

    static {
        DEF_COLLECTIONS = new MimeTypeCollection[]{WILDCARD, COMMON_IMAGES, COMMON_TEXTS};
    }

    private String name;
    private ArrayList<MimeType> mimes;
    private LinkedHashMap<String, MimeType> extMap, mimeMap;

    public MimeTypeCollection(String name) {
        this.name = name;
        mimes = new ArrayList<>();
        extMap = new LinkedHashMap<>();
        mimeMap = new LinkedHashMap<>();
    }
    public MimeTypeCollection(Collection<? extends MimeType> mimeTypes) {
        super();
        addAll(mimeTypes);
    }

    public MimeTypeCollection add(MimeType mime) {
        if (mime == null) return this;
        mimes.add(mime);
        mimeMap.put(mime.getMime(), mime);
        mime.getExtensions()
                .stream()
                .forEach(ext -> extMap.put(ext, mime));
        return this;
    }
    public MimeTypeCollection addAll(Collection<? extends MimeType> mimeTypes) {
        mimeTypes.stream().forEach(this::add);
        return this;
    }

    public boolean hasExtension(String extension) {
        return hasMime("*/*") || extMap.keySet().contains(extension);
    }

    public boolean hasMime(String mime) {
        return mimeMap.keySet().contains(mime) ||
               mimes.stream().filter(m -> m.matches(mime)).count() > 0;
    }

    public MimeType getMimeType(String mime) {
        // The "null ?? notnull" coalescing operator
        // would be amazing right about now
        MimeType mt = mimeMap.get(mime);
        return (mt != null ? mt : mimes.stream().filter(m -> m.matches(mime)).findFirst().orElse(null));
    }

    public List<MimeType> getMimes() {
        return mimes;
    }

    @Override
    public String toString() {
        return name != null ? name : mimes.stream()
                .map(MimeType::getDefaultExtension)
                .distinct()
                .map(s -> '.' + s)
                .collect(Collectors.joining(", "));
    }
}
