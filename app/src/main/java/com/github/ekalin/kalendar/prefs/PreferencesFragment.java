package com.github.ekalin.kalendar.prefs;

import android.os.Bundle;

import com.github.ekalin.kalendar.R;

public class PreferencesFragment extends KalendarPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
