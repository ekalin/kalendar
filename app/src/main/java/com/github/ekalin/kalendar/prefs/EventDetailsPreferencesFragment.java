package com.github.ekalin.kalendar.prefs;

import android.os.Bundle;

import com.github.ekalin.kalendar.R;

public class EventDetailsPreferencesFragment extends KalendarPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.preferences_event_details, rootKey);
    }
}
