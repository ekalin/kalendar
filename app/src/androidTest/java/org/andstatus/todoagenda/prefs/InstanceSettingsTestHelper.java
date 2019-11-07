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

    public void setEventsEnded(EndedSomeTimeAgo value) {
        sharedPreferences.edit().putString(InstanceSettings.PREF_EVENTS_ENDED, value.save()).apply();
    }

    public void setEventRange(int range) {
        sharedPreferences.edit().putString(InstanceSettings.PREF_EVENT_RANGE, String.valueOf(range));
    }
}
