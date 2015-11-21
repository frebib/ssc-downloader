package net.frebib.sscdownloader;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class MimeTypeCollection {
    public static final MimeTypeCollection commonImages = new MimeTypeCollection()
            .add(new MimeType("image/gif", "gif"))
            .add(new MimeType("image/png", "png"))
            .add(new MimeType("image/bmp", "bmp", "bm"))
            .add(new MimeType("image/tiff", "tiff", "tif"))
            .add(new MimeType("image/jpeg", "jpg", "jpe", "jpeg", "jps", "jfif"));

    private ArrayList<MimeType> mimes;
    private LinkedHashMap<String, MimeType> extMap, mimeMap;

    public MimeTypeCollection() {
        mimes = new ArrayList<>();
        extMap = new LinkedHashMap<>();
        mimeMap = new LinkedHashMap<>();
    }

    public MimeTypeCollection add(MimeType mime) {
        mimes.add(mime);
        mimeMap.put(mime.getMime(), mime);
        mime.getExtensions()
                .stream()
                .forEach(ext -> extMap.put(ext, mime));
        return this;
    }

    public boolean hasExtension(String extension) {
        return extMap.keySet().contains(extension);
    }

    public boolean hasMime(String mime) {
        return mimeMap.keySet().contains(mime);
    }

    public MimeType getMimeType(String mime) {
        return mimeMap.get(mime);
    }
}
