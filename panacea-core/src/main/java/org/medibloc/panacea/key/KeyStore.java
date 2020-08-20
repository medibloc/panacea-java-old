package org.medibloc.panacea.key;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class KeyStore {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String convertToJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    public static <T> T parseJson(String json, Class<T> type) throws Exception {
        return objectMapper.readValue(json, type);
    }

    public static File saveToDefaultPath(Object obj, String objName) throws Exception {
        return save(obj, objName, getDefaultFilePath());
    }

    public static File save(Object obj, String objName, String destinationPath) throws Exception {
        File destinationDirectory = getOrCreateDir(destinationPath);
        String fileName = getFileName(objName);
        File destination = new File(destinationDirectory, fileName);

        objectMapper.writeValue(destination, obj);
        return destination;
    }

    private static String getDefaultFilePath() {
        String lowerOsName = System.getProperty("os.name").toLowerCase();

        if (lowerOsName.startsWith("mac")) {
            return String.format(
                    "%s%sLibrary%sMedibloc", System.getProperty("user.home"), File.separator,
                    File.separator);
        } else if (lowerOsName.startsWith("win")) {
            return String.format("%s%sMedibloc", System.getenv("APPDATA"), File.separator);
        } else {
            // TODO - test on android
            return String.format("%s%s.Medibloc", System.getProperty("user.home"), File.separator);
        }
    }

    private static File getOrCreateDir(String destinationDir) {
        File destination = new File(destinationDir);

        if (!destination.exists()) {
            if (!destination.mkdirs()) {
                throw new RuntimeException("Unable to create destination directory [" + destinationDir + "]");
            }
        }

        return destination;
    }

    private static String getFileName(String objName) {
        Date today = new Date(Calendar.getInstance().getTimeInMillis());
        SimpleDateFormat form = new SimpleDateFormat("'UTC--'yyyy-MM-dd'T'HH-mm-ss.SSS'Z--'");
        return form.format(today) + objName + ".json";
    }
}
