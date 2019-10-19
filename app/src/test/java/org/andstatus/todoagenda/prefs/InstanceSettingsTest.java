package org.andstatus.todoagenda.prefs;

import android.content.Context;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

import static com.google.common.truth.Truth.assertThat;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_ACTIVE_CALENDARS;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_ACTIVE_TASK_LISTS;

@RunWith(RobolectricTestRunner.class)
public class InstanceSettingsTest {
    @Test
    public void toJsonForBackup_doesNotIncludeCalendarAndTaskLists() {
        InstanceSettings settings = new InstanceSettings(Mockito.mock(Context.class), 1, "testForBackup");
        JSONObject json = settings.toJsonForBackup();
        assertThat(json.has(PREF_ACTIVE_CALENDARS)).isFalse();
        assertThat(json.has(PREF_ACTIVE_TASK_LISTS)).isFalse();
    }

    @Test
    public void toJsonComplete_includesCalendarAndTaskLists() {
        InstanceSettings settings = new InstanceSettings(Mockito.mock(Context.class), 1, "testComplete");
        JSONObject json = settings.toJsonComplete();
        assertThat(json.has(PREF_ACTIVE_CALENDARS)).isTrue();
        assertThat(json.has(PREF_ACTIVE_TASK_LISTS)).isTrue();
    }
}
