package com.github.ekalin.kalendar.prefs;

import android.os.Bundle;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;

import com.github.ekalin.kalendar.R;
import com.github.ekalin.kalendar.provider.KeywordsFilter;

public class EventFiltersPreferencesFragment extends KalendarPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.preferences_event_filters, rootKey);
        Preference hideBasedOnKeywordsPref = findPreference(InstanceSettings.PREF_HIDE_BASED_ON_KEYWORDS);
        hideBasedOnKeywordsPref.setSummaryProvider(this::getHideBasedOnKeywordsSummary);
    }

    private CharSequence getHideBasedOnKeywordsSummary(Preference preference) {
        KeywordsFilter filter = new KeywordsFilter(((EditTextPreference) preference).getText());
        if (filter.isEmpty()) {
            return getString(R.string.this_option_is_turned_off);
        } else {
            return filter.toString();
        }
    }
}
