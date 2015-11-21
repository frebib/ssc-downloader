package net.frebib.sscdownloader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MimeType {
    private String mime;
    private ArrayList<String> exts;

    public MimeType(String mimetype, String... extensions) {
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

    public String getDefaultExtension(){
        return exts.get(0);
    }
}
