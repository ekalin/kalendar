package org.andstatus.todoagenda.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import org.andstatus.todoagenda.EndedSomeTimeAgo;

public class InstanceSettingsTestHelper {
    private SharedPreferences sharedPreferences;

    public InstanceSettingsTestHelper(Context context, int widgetId) {
        this.sharedPreferences = context.getSharedPreferences(InstanceSettings.nameForWidget(widgetId),
                Context.MODE_PRIVATE);
    }

    public void setShowDayHeaders(boolean showDayHeaders) {
        sharedPreferences.edit().putBoolean(InstanceSettings.PREF_SHOW_DAY_HEADERS, showDayHeaders).apply();
    }

    public void setShowDaysWithoutEvents(boolean showDaysWithoutEvents) {
        sharedPreferences.edit().putBoolean(InstanceSettings.PREF_SHOW_DAYS_WITHOUT_EVENTS, showDaysWithoutEvents).apply();
    }

    public void setShowOnlyClosestInstanceOfRecurringEvent(boolean show) {
        sharedPreferences.edit().putBoolean(InstanceSettings.PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT,
                show).apply();
    }

    public void setEventsEnded(EndedSomeTimeAgo value) {
        sharedPreferences.edit().putString(InstanceSettings.PREF_EVENTS_ENDED, value.save()).apply();
    }
}
