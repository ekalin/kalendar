package org.andstatus.todoagenda.prefs;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

import org.andstatus.todoagenda.R;

public class PreferencesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
