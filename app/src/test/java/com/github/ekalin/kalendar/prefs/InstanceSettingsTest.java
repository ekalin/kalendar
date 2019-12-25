package com.github.ekalin.kalendar.prefs;

import androidx.test.core.app.ApplicationProvider;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static com.github.ekalin.kalendar.prefs.InstanceSettings.PREF_ACTIVE_CALENDARS;
import static com.github.ekalin.kalendar.prefs.InstanceSettings.PREF_ACTIVE_TASK_LISTS;
import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
public class InstanceSettingsTest {
    @Test
    public void toJsonForBackup_doesNotIncludeCalendarAndTaskLists() {
        InstanceSettings settings = new InstanceSettings(ApplicationProvider.getApplicationContext(), 1);
        JSONObject json = settings.toJsonForBackup();
        assertThat(json.has(PREF_ACTIVE_CALENDARS)).isFalse();
        assertThat(json.has(PREF_ACTIVE_TASK_LISTS)).isFalse();
    }

    @Test
    public void toJsonComplete_includesCalendarAndTaskLists() {
        InstanceSettings settings = new InstanceSettings(ApplicationProvider.getApplicationContext(), 1);
        JSONObject json = settings.toJsonComplete();
        assertThat(json.has(PREF_ACTIVE_CALENDARS)).isTrue();
        assertThat(json.has(PREF_ACTIVE_TASK_LISTS)).isTrue();
    }
}
