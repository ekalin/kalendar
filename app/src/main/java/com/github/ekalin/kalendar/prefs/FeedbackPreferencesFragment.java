package com.github.ekalin.kalendar.prefs;

import android.content.Intent;
import android.os.Bundle;
import androidx.preference.Preference;

import com.github.ekalin.kalendar.R;
import com.github.ekalin.kalendar.provider.QueryResultsStorage;

import static android.content.Intent.ACTION_CREATE_DOCUMENT;
import static com.github.ekalin.kalendar.WidgetConfigurationActivity.REQUEST_ID_BACKUP_SETTINGS;
import static com.github.ekalin.kalendar.WidgetConfigurationActivity.REQUEST_ID_RESTORE_SETTINGS;

public class FeedbackPreferencesFragment extends KalendarPreferenceFragment {
    private static final String KEY_SHARE_EVENTS_FOR_DEBUGGING = "shareEventsForDebugging";
    private static final String KEY_BACKUP_SETTINGS = "backupSettings";
    private static final String KEY_RESTORE_SETTINGS = "restoreSettings";

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
        getActivity().startActivityForResult(intent, REQUEST_ID_BACKUP_SETTINGS);
    }

    private void restoreSettings() {
        Intent intent = new Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)
                .addCategory(Intent.CATEGORY_OPENABLE);
        Intent withChooser = Intent.createChooser(intent, getActivity().getText(R.string.restore_settings_title));
        getActivity().startActivityForResult(withChooser, REQUEST_ID_RESTORE_SETTINGS);
    }
}
