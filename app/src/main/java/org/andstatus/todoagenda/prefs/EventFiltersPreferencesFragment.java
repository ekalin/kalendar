package org.andstatus.todoagenda.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.provider.KeywordsFilter;

public class EventFiltersPreferencesFragment extends KalendarPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.preferences_event_filters, rootKey);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        showStatus();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    private void showStatus() {
        showEventsEnded();
        showEventRange();
        showHideBasedOnKeywords();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        showStatus();
    }

    private void showEventsEnded() {
        ListPreference preference = findPreference(InstanceSettings.PREF_EVENTS_ENDED);
        preference.setSummary(preference.getEntry());
    }

    private void showEventRange() {
        ListPreference preference = findPreference(InstanceSettings.PREF_EVENT_RANGE);
        preference.setSummary(preference.getEntry());
    }

    private void showHideBasedOnKeywords() {
        EditTextPreference preference = findPreference(InstanceSettings.PREF_HIDE_BASED_ON_KEYWORDS);
        KeywordsFilter filter = new KeywordsFilter(preference.getText());
        if (filter.isEmpty()) {
            preference.setSummary(R.string.this_option_is_turned_off);
        } else {
            preference.setSummary(filter.toString());
        }
    }
}
