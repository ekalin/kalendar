package org.andstatus.todoagenda.prefs;

import android.content.Context;
import android.content.SharedPreferences;

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
}
