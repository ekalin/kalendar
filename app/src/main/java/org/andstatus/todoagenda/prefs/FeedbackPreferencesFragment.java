package org.andstatus.todoagenda.prefs;

import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.provider.QueryResultsStorage;

public class FeedbackPreferencesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_feedback, rootKey);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        switch (preference.getKey()) {
            case InstanceSettings.KEY_SHARE_EVENTS_FOR_DEBUGGING:
                QueryResultsStorage.shareEventsForDebugging(getActivity(),
                        ApplicationPreferences.getWidgetId(getActivity()));
            default:
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }
}
