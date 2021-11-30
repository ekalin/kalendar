package com.github.ekalin.kalendar.prefs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import com.github.ekalin.kalendar.R;
import com.github.ekalin.kalendar.provider.QueryResultsStorage;

import static android.content.Intent.ACTION_CREATE_DOCUMENT;

public class FeedbackPreferencesFragment extends KalendarPreferenceFragment {
    private static final String KEY_SHARE_EVENTS_FOR_DEBUGGING = "shareEventsForDebugging";
    private static final String KEY_BACKUP_SETTINGS = "backupSettings";
    private static final String KEY_RESTORE_SETTINGS = "restoreSettings";

    ActivityResultLauncher<Intent> backupLauncher;
    ActivityResultLauncher<Intent> restoreLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        backupLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        new SettingsImporterExporter().backupSettings(instanceSettings.getWidgetId(), data.getData(),
                                getActivity());
                    }
                });
        restoreLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        new SettingsImporterExporter().restoreSettings(instanceSettings.getWidgetId(), data.getData(),
                                getActivity());
                    }
                });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.preferences_feedback, rootKey);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        int widgetId = instanceSettings.getWidgetId();

        switch (preference.getKey()) {
            case KEY_SHARE_EVENTS_FOR_DEBUGGING:
                QueryResultsStorage.shareEventsForDebugging(getActivity(), widgetId);
                break;

            case KEY_BACKUP_SETTINGS:
                backupSettings(widgetId);
                break;

            case KEY_RESTORE_SETTINGS:
                restoreSettings();
                break;
        }

        return super.onPreferenceTreeClick(preference);
    }

    private void backupSettings(int widgetId) {
        String fileName = "Kalendar-" + widgetId + ".json";
        Intent intent = new Intent(ACTION_CREATE_DOCUMENT);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        backupLauncher.launch(intent);
    }


    private void restoreSettings() {
        Intent intent = new Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)
                .addCategory(Intent.CATEGORY_OPENABLE);
        Intent withChooser = Intent.createChooser(intent, getActivity().getText(R.string.restore_settings_title));
        restoreLauncher.launch(withChooser);
    }
}
