package org.andstatus.todoagenda.prefs;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * @author yvolk@yurivolkov.com
 */
public class SettingsStorage {
    private static final int BUFFER_LENGTH = 4 * 1024;

    private SettingsStorage() {
        // Not instantiable
    }

    public static void saveJson(Context context, String key, JSONObject json) throws IOException {
        writeStringToFile(json.toString(), jsonFile(context, key));
    }

    @NonNull
    public static JSONObject loadJsonFromFile(Context context, String key) throws IOException {
        return getJSONObject(jsonFile(context, key));
    }

    public static void delete(Context context, String key) {
        File file = jsonFile(context, key);
        if (file.exists()) {
            file.delete();
        }
    }

    private static File jsonFile(Context context, String key) {
        return new File(getExistingPreferencesDirectory(context), key + ".json");
    }

    private static File getExistingPreferencesDirectory(Context context) {
        File dir = new File(context.getApplicationInfo().dataDir, "shared_prefs");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private static void writeStringToFile(String string, File file) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file.getAbsolutePath(), false);
             Writer out = new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8))) {
            out.write(string);
        }
    }

    @NonNull
    private static JSONObject getJSONObject(File file) throws IOException {
        String fileString = getContents(file);
        if (!TextUtils.isEmpty(fileString)) {
            try {
                return new JSONObject(fileString);
            } catch (JSONException e) {
                Log.v("getJSONObject", file.getAbsolutePath(), e);
            }
        }
        return new JSONObject();
    }

    /**
     * Reads the whole file
     */
    private static String getContents(File file) throws IOException {
        if (file != null) {
            return getContents(new FileInputStream(file));
        }
        return "";
    }

    /**
     * Read the stream into an array and close the stream
     **/
    public static String getContents(InputStream is) throws IOException {
        char[] buffer = new char[BUFFER_LENGTH];
        StringBuilder bout = new StringBuilder();
        if (is != null) {
            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                int count;
                while ((count = reader.read(buffer)) != -1) {
                    bout.append(buffer, 0, count);
                }
            }
        }

        return bout.toString();
    }
}
