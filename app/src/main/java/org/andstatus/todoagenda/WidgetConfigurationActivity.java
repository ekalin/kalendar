package org.andstatus.todoagenda;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.prefs.KalendarPreferenceFragment;
import org.andstatus.todoagenda.prefs.PreferencesFragment;
import org.andstatus.todoagenda.provider.WidgetData;
import org.andstatus.todoagenda.util.Optional;
import org.andstatus.todoagenda.util.PermissionsUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class WidgetConfigurationActivity extends AppCompatActivity
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private static final String TITLE_TAG = "org.andstatus.todoagenda.PREFS_TITLE";
    private static final int BUFFER_LENGTH = 4 * 1024;

    public static final int REQUEST_ID_RESTORE_SETTINGS = 1;
    public static final int REQUEST_ID_BACKUP_SETTINGS = 2;

    private int widgetId = 0;
    private String prefsName;
    private boolean saveOnPause = true;

    @NonNull
    public static Intent intentToStartMe(Context context, int widgetId) {
        Intent intent = new Intent(context, WidgetConfigurationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!openThisActivity(getIntent())) {
            return;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_configuration_actitivy);

        prefsName = InstanceSettings.nameForWidget(widgetId);

        if (savedInstanceState == null) {
            PreferencesFragment fragment = new PreferencesFragment();
            Bundle args = new Bundle();
            setFragmentArguments(args);
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(R.id.preferences, fragment).commit();
            setTitleToWidgetName();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                setTitleToWidgetName();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setFragmentArguments(Bundle args) {
        args.putString(KalendarPreferenceFragment.PREFS_NAME_KEY, prefsName);
        args.putInt(KalendarPreferenceFragment.WIDGET_ID_KEY, widgetId);
    }

    private boolean openThisActivity(Intent newIntent) {
        int newWidgetId = newIntent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        Intent restartIntent = null;
        if (newWidgetId == 0 || !PermissionsUtil.arePermissionsGranted(this)) {
            restartIntent = MainActivity.intentToStartMe(this);
        } else if (widgetId != 0 && widgetId != newWidgetId) {
            restartIntent = MainActivity.intentToConfigure(this, newWidgetId);
        } else if (widgetId == 0) {
            widgetId = newWidgetId;
        }
        if (restartIntent != null) {
            widgetId = 0;
            startActivity(restartIntent);
            finish();
            return false;
        }

        return true;
    }

    private void setTitleToWidgetName() {
        InstanceSettings instanceSettings = AllSettings.instanceFromId(this, widgetId);
        setTitle(instanceSettings.getWidgetInstanceName());
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        Bundle args = pref.getExtras();
        setFragmentArguments(args);
        Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.preferences, fragment)
                .addToBackStack(null)
                .commit();
        setTitle(pref.getTitle());
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (saveOnPause) {
            EnvironmentChangedReceiver.updateWidget(this, widgetId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restartIfNeeded();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    private void restartIfNeeded() {
        if (!PermissionsUtil.arePermissionsGranted(this)) {
            widgetId = 0;
            startActivity(MainActivity.intentToStartMe(this));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        } else {
            finish();
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ID_BACKUP_SETTINGS:
                if (resultCode == RESULT_OK && data != null) {
                    backupSettings(data.getData());
                }
                break;

            case REQUEST_ID_RESTORE_SETTINGS:
                if (resultCode == RESULT_OK && data != null) {
                    restoreSettings(data.getData());
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void backupSettings(Uri uri) {
        if (uri == null) {
            return;
        }

        InstanceSettings settings = AllSettings.instanceFromId(this, widgetId);
        String jsonSettings = WidgetData.fromSettingsForBackup(settings).toJsonString();
        try (OutputStream out = this.getContentResolver().openOutputStream(uri, "w");
             Writer writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            writer.write(jsonSettings);
            Toast.makeText(this, getText(R.string.backup_settings_successful), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            String msg = getString(R.string.backup_settings_error, uri, e.getMessage());
            Log.e(this.getClass().getSimpleName(), msg, e);
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
    }

    private void restoreSettings(Uri uri) {
        if (uri == null) {
            return;
        }

        Optional<JSONObject> jsonObject = readJson(uri);
        if (!jsonObject.isPresent()) {
            // A toast with the error has already been shown, so exit early
            return;
        }

        final WidgetConfigurationActivity context = WidgetConfigurationActivity.this;
        if (AllSettings.restoreWidgetSettings(context, jsonObject.get(), widgetId)) {
            saveOnPause = false;
            int duration = 3000;
            Toast.makeText(context, context.getText(R.string.restore_settings_successful), Toast.LENGTH_LONG).show();
            new Handler().postDelayed(() -> {
                startActivity(intentToStartMe(context, widgetId));
                context.finish();
            }, duration);
        } else {
            Toast.makeText(context, context.getText(R.string.restore_settings_unsuccessful), Toast.LENGTH_LONG).show();
        }
    }

    private Optional<JSONObject> readJson(Uri uri) {
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            return Optional.of(new JSONObject(getContents(in)));
        } catch (IOException | JSONException e) {
            String msg = getString(R.string.restore_settings_error, uri, e.getMessage());
            Log.e(this.getClass().getSimpleName(), msg, e);
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
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
