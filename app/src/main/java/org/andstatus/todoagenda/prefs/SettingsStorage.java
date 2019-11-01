package org.andstatus.todoagenda.prefs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author yvolk@yurivolkov.com
 */
public class SettingsStorage {
    private static final int BUFFER_LENGTH = 4 * 1024;

    private SettingsStorage() {
        // Not instantiable
    }

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
