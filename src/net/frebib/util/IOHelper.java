package net.frebib.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class IOHelper {
    public static Properties loadProperties(String path) throws IOException {
        FileInputStream is = new FileInputStream(path);
        Properties props = new Properties();
        props.load(is);
        is.close();
        return props;
    }
    public static void saveProperties(Properties props, String path) throws IOException {
        if (props.isEmpty()) return;

        // Make directory path if it doesn't exist
        new File(path).getParentFile().mkdirs();

        OutputStream os = new FileOutputStream(new File(path));
        props.store(os, null);
    }
    public static String loadFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, "UTF-8");
    }
    public static String[] loadFileArray(String path) throws IOException {
        FileReader fr = new FileReader(path);
        BufferedReader br = new BufferedReader(fr);
        List<String> lines = new ArrayList<String>();
        String line = null;
        while ((line = br.readLine()) != null)
            lines.add(line);
        br.close();
        return lines.toArray(new String[lines.size()]);
    }
}
