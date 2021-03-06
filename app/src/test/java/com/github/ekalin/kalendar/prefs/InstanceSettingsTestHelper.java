package com.github.ekalin.kalendar.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

import com.github.ekalin.kalendar.EndedSomeTimeAgo;

public class InstanceSettingsTestHelper {
    private SharedPreferences sharedPreferences;
    private InstanceSettings instanceSettings;

    public InstanceSettingsTestHelper(Context context, int widgetId) {
        this.sharedPreferences = context.getSharedPreferences(InstanceSettings.nameForWidget(widgetId),
                Context.MODE_PRIVATE);
        this.instanceSettings = AllSettings.instanceFromId(context, widgetId);
    }

    public void setShowDayHeaders(boolean showDayHeaders) {
        sharedPreferences.edit().putBoolean(InstanceSettings.PREF_SHOW_DAY_HEADERS, showDayHeaders).apply();
    }

    public void setShowDaysWithoutEvents(boolean showDaysWithoutEvents) {
        sharedPreferences.edit().putBoolean(InstanceSettings.PREF_SHOW_DAYS_WITHOUT_EVENTS, showDaysWithoutEvents).apply();
    }

    public void setShowOnlyClosestInstanceOfRecurringEvent(boolean show) {
        sharedPreferences.edit()
                .putBoolean(InstanceSettings.PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT, show).apply();
    }

    public void setEventsEnded(EndedSomeTimeAgo value) {
        sharedPreferences.edit().putString(InstanceSettings.PREF_EVENTS_ENDED, value.save()).apply();
    }

    public void setEventRage(int range) {
        sharedPreferences.edit().putString(InstanceSettings.PREF_EVENT_RANGE, String.valueOf(range)).apply();
    }

    public void setActiveCalendars(Set<String> activeCalendars) {
        sharedPreferences.edit().putStringSet(InstanceSettings.PREF_ACTIVE_CALENDARS, activeCalendars).apply();
    }

    public void setTaskSource(String source) {
        sharedPreferences.edit().putString(InstanceSettings.PREF_TASK_SOURCE, source).apply();
    }

    public void setActiveTaskLists(Set<String> taskLists) {
        sharedPreferences.edit().putStringSet(InstanceSettings.PREF_ACTIVE_TASK_LISTS, taskLists).apply();
    }

    public void setLockedTimeZoneId(String zoneId) {
        instanceSettings.setLockedTimeZoneId(zoneId);
    }

    public void setShowBirthdays(boolean showBirthdays) {
        sharedPreferences.edit().putBoolean(InstanceSettings.PREF_SHOW_BIRTHDAYS, showBirthdays).apply();
    }
}
