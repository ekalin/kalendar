package org.andstatus.todoagenda.prefs;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

public class KalendarPreferenceFragment extends PreferenceFragmentCompat {
    public static final String WIDGET_ID_KEY = "org.andstatus.todoagenda.WIDGET_ID";
    public static final String PREFS_NAME_KEY = "org.andstatus.todoagenda.PREFS_NAME";

    protected InstanceSettings instanceSettings;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Bundle arguments = getArguments();
        String prefsName = arguments.getString(PREFS_NAME_KEY);
        getPreferenceManager().setSharedPreferencesName(prefsName);

        instanceSettings = AllSettings.instanceFromId(getActivity(), arguments.getInt(WIDGET_ID_KEY));
    }
}
