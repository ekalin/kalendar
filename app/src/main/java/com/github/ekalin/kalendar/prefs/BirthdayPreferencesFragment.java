package com.github.ekalin.kalendar.prefs;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.preference.Preference;

import com.github.ekalin.kalendar.EnvironmentChangedReceiver;
import com.github.ekalin.kalendar.R;
import com.github.ekalin.kalendar.birthday.BirthdayProvider;

public class BirthdayPreferencesFragment extends KalendarPreferenceFragment {
    private static final String KEY_PREF_GRANT_BIRTHDAY_PERMISSION = "grantBirthdayPermission";
    private static final String KEY_PREF_SHOW_BIRTHDAYS = "showBirthdays";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.preferences_birthdays, rootKey);
        updateState();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateState();
    }

    private void updateState() {
        setGrantPermissionVisibility();
    }

    private void setGrantPermissionVisibility() {
        Preference prefEnabled = findPreference(KEY_PREF_SHOW_BIRTHDAYS);
        Preference prefGrant = findPreference(KEY_PREF_GRANT_BIRTHDAY_PERMISSION);

        boolean hasPermission = BirthdayProvider.hasPermission(getActivity());
        prefEnabled.setEnabled(hasPermission);
        prefGrant.setVisible(!hasPermission);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        switch (preference.getKey()) {
            case KEY_PREF_GRANT_BIRTHDAY_PERMISSION:
                requestTaskPermission();
                return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    private void requestTaskPermission() {
        BirthdayProvider.requestPermission(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        setGrantPermissionVisibility();
        EnvironmentChangedReceiver.registerReceivers(getActivity(), true);
    }
}
