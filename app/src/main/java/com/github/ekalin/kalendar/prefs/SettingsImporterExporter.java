package com.github.ekalin.kalendar.prefs;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import com.github.ekalin.kalendar.R;
import com.github.ekalin.kalendar.provider.WidgetData;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class SettingsImporterExporter {
    private static final int BUFFER_LENGTH = 4 * 1024;

    public void backupSettings(int widgetId, Uri uri, Context context) {
        if (uri == null) {
            return;
        }

        InstanceSettings settings = AllSettings.instanceFromId(context, widgetId);
        String jsonSettings = WidgetData.fromSettingsForBackup(settings).toJsonString();
        try (OutputStream out = context.getContentResolver().openOutputStream(uri, "w");
             Writer writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            writer.write(jsonSettings);
            Toast.makeText(context, context.getText(R.string.backup_settings_successful), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            String msg = context.getString(R.string.backup_settings_error, uri, e.getMessage());
            Log.e(this.getClass().getSimpleName(), msg, e);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }

    public void restoreSettings(int widgetId, Uri uri, Context context) {
        if (uri == null) {
            return;
        }

        Optional<JSONObject> jsonObject = readJson(uri, context);
        if (!jsonObject.isPresent()) {
            // A toast with the error has already been shown, so exit early
            return;
        }

        if (AllSettings.restoreWidgetSettings(context, jsonObject.get(), widgetId)) {
            Toast.makeText(context, context.getText(R.string.restore_settings_successful), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, context.getText(R.string.restore_settings_unsuccessful), Toast.LENGTH_LONG).show();
        }
    }

    private Optional<JSONObject> readJson(Uri uri, Context context) {
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            return Optional.of(new JSONObject(getContents(in)));
        } catch (IOException | JSONException e) {
            String msg = context.getString(R.string.restore_settings_error, uri, e.getMessage());
            Log.e(this.getClass().getSimpleName(), msg, e);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            return Optional.empty();
        }
    }

    private String getContents(InputStream is) throws IOException {
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
