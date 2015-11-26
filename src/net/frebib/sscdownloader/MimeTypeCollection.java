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
            .add(MimeType.fromDelimitedExts("image/gif",  "gif"))
            .add(MimeType.fromDelimitedExts("image/png",  "png"))
            .add(MimeType.fromDelimitedExts("image/bmp",  "bmp bm"))
            .add(MimeType.fromDelimitedExts("image/tiff", "tiff tif"))
            .add(MimeType.fromDelimitedExts("image/jpeg", "jpg jpe jpeg jps jfif"));

    public static final MimeTypeCollection COMMON_TEXTS = new MimeTypeCollection("Common Text Formats")
            .add(MimeType.fromDelimitedExts("text/plain", "txt text log list"))
            .add(MimeType.fromDelimitedExts("text/html",  "html htm php htmls xhtml htx shtml"))
            .add(MimeType.fromDelimitedExts("text/xml",   "xml"))
            .add(MimeType.fromDelimitedExts("text/richtext",   "rt rtf rtx"))
            .add(MimeType.fromDelimitedExts("text/ecmascript", "js"))
            .add(MimeType.fromDelimitedExts("text/javascript", "js"));
    public static final MimeTypeCollection COMMON_AUDIO = new MimeTypeCollection("Common Audio Formats")
            .add(MimeType.fromDelimitedExts("audio/mpeg",  "mp3 mp2 m2a mpa mpg"))
            .add(MimeType.fromDelimitedExts("audio/aiff",  "aiff aif aifc"))
            .add(MimeType.fromDelimitedExts("audio/midi",  "mid midi"))
            .add(MimeType.fromDelimitedExts("audio/basic", "au snd"))
            .add(MimeType.fromDelimitedExts("audio/mpeg3", "mp3"))
            .add(MimeType.fromDelimitedExts("audio/wav",   "wav"))
            .add(MimeType.fromDelimitedExts("audio/flac",  "flac"))
            .add(MimeType.fromDelimitedExts("audio/ogg",   "ogg spx"));

    static {
        DEF_COLLECTIONS = new MimeTypeCollection[] { WILDCARD, COMMON_TEXTS, COMMON_IMAGES, COMMON_AUDIO };
    }

    private String name;
    private ArrayList<MimeType> mimes;
    private LinkedHashMap<String, MimeType> extMap, mimeMap;

    public MimeTypeCollection() {
        super();
        mimes = new ArrayList<>();
        extMap = new LinkedHashMap<>();
        mimeMap = new LinkedHashMap<>();
    }
    public MimeTypeCollection(String name) {
        this();
        this.name = name;
    }
    public MimeTypeCollection(Collection<? extends MimeType> mimeTypes) {
        this();
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
