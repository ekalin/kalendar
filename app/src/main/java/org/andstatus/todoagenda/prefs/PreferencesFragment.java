package org.andstatus.todoagenda.prefs;

import android.os.Bundle;

import org.andstatus.todoagenda.R;

public class PreferencesFragment extends KalendarPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
