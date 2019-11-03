package org.andstatus.todoagenda.prefs;

import android.os.Bundle;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.util.DateUtil;
import org.joda.time.DateTimeZone;

import java.util.TimeZone;

public class LayoutPreferencesFragment extends KalendarPreferenceFragment {
    private static final String PREF_LOCK_TIME_ZONE = "lockTimeZone";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.preferences_layout, rootKey);

        Preference prefWidgetInstanceName = findPreference(InstanceSettings.PREF_WIDGET_INSTANCE_NAME);
        prefWidgetInstanceName.setOnPreferenceChangeListener(this::checkName);
        prefWidgetInstanceName.setSummaryProvider(preference -> getNameAndId());

        showLockTimeZone(true);
    }

    private boolean checkName(Preference preference, Object newValue) {
        return !((String) newValue).isEmpty();
    }

    private String getNameAndId() {
        return instanceSettings.getWidgetInstanceName() + " (id:" + instanceSettings.getWidgetId() + ")";
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        switch (preference.getKey()) {
            case PREF_LOCK_TIME_ZONE:
                if (preference instanceof CheckBoxPreference) {
                    CheckBoxPreference checkPref = (CheckBoxPreference) preference;
                    instanceSettings.setLockedTimeZoneId(checkPref.isChecked() ? TimeZone.getDefault().getID() : "");
                    showLockTimeZone(false);
                }
                break;
            default:
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void showLockTimeZone(boolean setAlso) {
        CheckBoxPreference preference = findPreference(PREF_LOCK_TIME_ZONE);
        if (preference != null) {
            boolean isChecked = setAlso ? instanceSettings.isTimeZoneLocked() : preference.isChecked();
            if (setAlso && preference.isChecked() != isChecked) {
                preference.setChecked(isChecked);
            }
            DateTimeZone timeZone = DateTimeZone.forID(DateUtil.validatedTimeZoneId(isChecked ?
                    instanceSettings.getLockedTimeZoneId() : TimeZone.getDefault().getID()));
            preference.setSummary(String.format(
                    getText(isChecked ? R.string.lock_time_zone_on_desc : R.string.lock_time_zone_off_desc).toString(),
                    timeZone.getName(DateUtil.now(timeZone).getMillis()))
            );
        }
    }
}
