package org.andstatus.todoagenda.prefs;

import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.andstatus.todoagenda.R;

public class ColorsPreferencesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_colors, rootKey);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        switch (preference.getKey()) {
            case InstanceSettings.PREF_BACKGROUND_COLOR:
                new BackgroundTransparencyDialog().show(getFragmentManager(),
                        InstanceSettings.PREF_BACKGROUND_COLOR);
                break;
            case InstanceSettings.PREF_PAST_EVENTS_BACKGROUND_COLOR:
                new BackgroundTransparencyDialog().show(getFragmentManager(),
                        InstanceSettings.PREF_PAST_EVENTS_BACKGROUND_COLOR);
                break;
            default:
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }
}
