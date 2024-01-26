package com.github.ekalin.kalendar.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.ColorInt;

import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.ekalin.kalendar.Alignment;
import com.github.ekalin.kalendar.EndedSomeTimeAgo;
import com.github.ekalin.kalendar.TextSizeScale;
import com.github.ekalin.kalendar.task.TaskProvider;
import com.github.ekalin.kalendar.util.DateUtil;
import com.github.ekalin.kalendar.widget.EventEntryLayout;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

/**
 * Loaded settings of one Widget
 *
 * @author yvolk@yurivolkov.com
 */
public class InstanceSettings {
    public static final String PREF_WIDGET_ID = "widgetId";

    // Appearance
    static final String PREF_WIDGET_INSTANCE_NAME = "widgetInstanceName";
    static final String PREF_TEXT_SIZE_SCALE = "textSizeScale";
    static final String PREF_TEXT_SIZE_SCALE_DEFAULT = "";
    static final String PREF_EVENT_ENTRY_LAYOUT = "eventEntryLayout";
    static final String PREF_EVENT_ENTRY_LAYOUT_DEFAULT = "";
    static final String PREF_MULTILINE_TITLE = "multilineTitle";
    static final boolean PREF_MULTILINE_TITLE_DEFAULT = false;
    static final String PREF_ABBREVIATE_DATES = "abbreviateDates";
    static final boolean PREF_ABBREVIATE_DATES_DEFAULT = false;
    static final String PREF_SHOW_DAY_HEADERS = "showDayHeaders";
    static final boolean PREF_SHOW_DAY_HEADERS_DEFAULT = true;
    static final String PREF_DAY_HEADER_ALIGNMENT = "dayHeaderAlignment";
    static final String PREF_DAY_HEADER_ALIGNMENT_DEFAULT = Alignment.LEFT.name();
    static final String PREF_SHOW_DAYS_WITHOUT_EVENTS = "showDaysWithoutEvents";
    static final boolean PREF_SHOW_DAYS_WITHOUT_EVENTS_DEFAULT = false;
    static final String PREF_SHOW_WIDGET_HEADER = "showHeader";
    static final boolean PREF_SHOW_WIDGET_HEADER_DEFAULT = true;
    static final String PREF_SHOW_WIDGET_HEADER_SEPARATOR = "showHeaderSeparator";
    static final boolean PREF_SHOW_WIDGET_HEADER_SEPARATOR_DEFAULT = false;
    static final String PREF_LOCKED_TIME_ZONE_ID = "lockedTimeZoneId";

    // Colors
    static final String PREF_EVENT_COLOR = "eventColor";
    @ColorInt static final int PREF_EVENT_COLOR_DEFAULT = 0xffffffff;
    static final String PREF_CURRENT_EVENT_COLOR = "currentEventColor";
    @ColorInt static final int PREF_CURRENT_EVENT_COLOR_DEFAULT = 0xffffe900;
    static final String PREF_DAY_HEADER_COLOR = "dayHeaderColor";
    @ColorInt static final int PREF_DAY_HEADER_COLOR_DEFAULT = 0xffffffff;
    static final String PREF_WIDGET_HEADER_COLOR = "widgetHeaderColor";
    @ColorInt static final int PREF_WIDGET_HEADER_COLOR_DEFAULT = 0x9affffff;
    static final String PREF_WIDGET_HEADER_BACKGROUND_COLOR = "widgetHeaderBackgroundColor";
    @ColorInt
    static final int PREF_WIDGET_HEADER_BACKGROUND_COLOR_DEFAULT = 0x00000000;
    static final String PREF_BACKGROUND_COLOR = "backgroundColor";
    @ColorInt static final int PREF_BACKGROUND_COLOR_DEFAULT = 0x80000000;
    static final String PREF_PAST_EVENTS_BACKGROUND_COLOR = "pastEventsBackgroundColor";
    @ColorInt static final int PREF_PAST_EVENTS_BACKGROUND_COLOR_DEFAULT = 0x4affff2b;

    // Event details
    static final String PREF_SHOW_END_TIME = "showEndTime";
    static final boolean PREF_SHOW_END_TIME_DEFAULT = true;
    static final String PREF_SHOW_LOCATION = "showLocation";
    static final boolean PREF_SHOW_LOCATION_DEFAULT = true;
    static final String PREF_FILL_ALL_DAY = "fillAllDay";
    static final boolean PREF_FILL_ALL_DAY_DEFAULT = true;
    static final String PREF_INDICATE_ALERTS = "indicateAlerts";
    static final boolean PREF_INDICATE_ALERTS_DEFAULT = true;
    static final String PREF_INDICATE_RECURRING = "indicateRecurring";
    static final boolean PREF_INDICATE_RECURRING_DEFAULT = false;

    // Event filters
    static final String PREF_EVENTS_ENDED = "eventsEnded";
    static final String PREF_EVENTS_ENDED_DEFAULT = "eventsEnded";
    static final String PREF_EVENT_RANGE = "eventRange";
    static final int PREF_EVENT_RANGE_DEFAULT = 30;
    static final String PREF_HIDE_BASED_ON_KEYWORDS = "hideBasedOnKeywords";
    static final String PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT = "showOnlyClosestInstanceOfRecurringEvent";
    static final boolean PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT_DEFAULT = false;

    // Calendars
    static final String PREF_ACTIVE_CALENDARS = "activeCalendars";

    // Tasks
    static final String PREF_TASK_SOURCE = "taskSource";
    static final String PREF_TASK_SOURCE_DEFAULT = TaskProvider.PROVIDER_NONE;
    static final String PREF_ACTIVE_TASK_LISTS = "activeTaskLists";

    // Birthdays
    static final String PREF_SHOW_BIRTHDAYS = "showBirthdays";
    static final String PREF_BIRTHDAY_COLOR = "birthdayColor";
    @ColorInt
    static final int PREF_BIRTHDAY_COLOR_DEFAULT = 0xff00ff00;

    private final Context context;
    private final int widgetId;
    private final SharedPreferences sharedPreferences;

    static InstanceSettings fromJson(Context context, int targetWidgetId, JSONObject json) {
        InstanceSettings settings = new InstanceSettings(context, targetWidgetId);
        settings.setFromJson(json);
        return settings;
    }

    private void setFromJson(JSONObject json) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        try {
            setStringFromJson(editor, json, PREF_WIDGET_INSTANCE_NAME);
            setStringFromJson(editor, json, PREF_TEXT_SIZE_SCALE);
            setStringFromJson(editor, json, PREF_EVENT_ENTRY_LAYOUT);
            setBooleanFromJson(editor, json, PREF_MULTILINE_TITLE);
            setBooleanFromJson(editor, json, PREF_ABBREVIATE_DATES);
            setBooleanFromJson(editor, json, PREF_SHOW_DAY_HEADERS);
            setStringFromJson(editor, json, PREF_DAY_HEADER_ALIGNMENT);
            setBooleanFromJson(editor, json, PREF_SHOW_DAYS_WITHOUT_EVENTS);
            setBooleanFromJson(editor, json, PREF_SHOW_WIDGET_HEADER);
            setBooleanFromJson(editor, json, PREF_SHOW_WIDGET_HEADER_SEPARATOR);
            setStringFromJson(editor, json, PREF_LOCKED_TIME_ZONE_ID);

            setIntFromJson(editor, json, PREF_EVENT_COLOR);
            setIntFromJson(editor, json, PREF_CURRENT_EVENT_COLOR);
            setIntFromJson(editor, json, PREF_DAY_HEADER_COLOR);
            setIntFromJson(editor, json, PREF_WIDGET_HEADER_COLOR);
            setIntFromJson(editor, json, PREF_WIDGET_HEADER_BACKGROUND_COLOR);
            setIntFromJson(editor, json, PREF_BACKGROUND_COLOR);
            setIntFromJson(editor, json, PREF_PAST_EVENTS_BACKGROUND_COLOR);

            setBooleanFromJson(editor, json, PREF_SHOW_END_TIME);
            setBooleanFromJson(editor, json, PREF_SHOW_LOCATION);
            setBooleanFromJson(editor, json, PREF_FILL_ALL_DAY);
            setBooleanFromJson(editor, json, PREF_INDICATE_ALERTS);
            setBooleanFromJson(editor, json, PREF_INDICATE_RECURRING);

            setStringFromJson(editor, json, PREF_EVENTS_ENDED);
            if (json.has(PREF_EVENT_RANGE)) {
                editor.putString(PREF_EVENT_RANGE, String.valueOf(json.getInt(PREF_EVENT_RANGE)));
            }
            setStringFromJson(editor, json, PREF_HIDE_BASED_ON_KEYWORDS);
            setBooleanFromJson(editor, json, PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT);

            setStringSetFromJson(editor, json, PREF_ACTIVE_CALENDARS);

            setStringFromJson(editor, json, PREF_TASK_SOURCE);
            setStringSetFromJson(editor, json, PREF_ACTIVE_TASK_LISTS);

            setBooleanFromJson(editor, json, PREF_SHOW_BIRTHDAYS);
            setIntFromJson(editor, json, PREF_BIRTHDAY_COLOR);

            editor.apply();
        } catch (JSONException e) {
            Log.w(InstanceSettings.class.getSimpleName(), "setFromJson failed, widgetId:" + widgetId + "\n" + json);
        }
    }

    private void setStringFromJson(SharedPreferences.Editor editor, JSONObject json, String prefName) throws JSONException {
        if (json.has(prefName)) {
            editor.putString(prefName, json.getString(prefName));
        }
    }

    private void setBooleanFromJson(SharedPreferences.Editor editor, JSONObject json, String prefName) throws JSONException {
        if (json.has(prefName)) {
            editor.putBoolean(prefName, json.getBoolean(prefName));
        }
    }

    private void setIntFromJson(SharedPreferences.Editor editor, JSONObject json, String prefName) throws JSONException {
        if (json.has(prefName)) {
            editor.putInt(prefName, json.getInt(prefName));
        }
    }

    private void setStringSetFromJson(SharedPreferences.Editor editor, JSONObject json, String prefName) throws JSONException {
        if (json.has(prefName)) {
            editor.putStringSet(prefName, jsonArray2StringSet(json.getJSONArray(prefName)));
        }
    }

    private static Set<String> jsonArray2StringSet(JSONArray jsonArray) {
        Set<String> set = new HashSet<>();
        for (int index = 0; index < jsonArray.length(); index++) {
            String value = jsonArray.optString(index);
            if (value != null) {
                set.add(value);
            }
        }
        return set;
    }

    InstanceSettings(Context context, int widgetId) {
        this.context = context;
        this.widgetId = widgetId;
        this.sharedPreferences = context.getSharedPreferences(nameForWidget(widgetId), Context.MODE_PRIVATE);
    }

    public static String nameForWidget(int widgetId) {
        return "widget" + widgetId;
    }

    public JSONObject toJsonForBackup() {
        return toJson(false);
    }

    public JSONObject toJsonComplete() {
        return toJson(true);
    }

    private JSONObject toJson(boolean complete) {
        JSONObject json = new JSONObject();
        try {
            json.put(PREF_WIDGET_ID, getWidgetId());
            json.put(PREF_WIDGET_INSTANCE_NAME, getWidgetInstanceName());
            json.put(PREF_TEXT_SIZE_SCALE, getTextSizeScale().preferenceValue);
            json.put(PREF_EVENT_ENTRY_LAYOUT, getEventEntryLayout().value);
            json.put(PREF_MULTILINE_TITLE, getTitleMultiline());
            json.put(PREF_ABBREVIATE_DATES, getAbbreviateDates());
            json.put(PREF_SHOW_DAY_HEADERS, getShowDayHeaders());
            json.put(PREF_DAY_HEADER_ALIGNMENT, getDayHeaderAlignment());
            json.put(PREF_SHOW_DAYS_WITHOUT_EVENTS, getShowDaysWithoutEvents());
            json.put(PREF_SHOW_WIDGET_HEADER, getShowWidgetHeader());
            json.put(PREF_SHOW_WIDGET_HEADER_SEPARATOR, getShowWidgetHeaderSeparator());
            json.put(PREF_LOCKED_TIME_ZONE_ID, getLockedTimeZoneId());

            json.put(PREF_EVENT_COLOR, getEventColor());
            json.put(PREF_CURRENT_EVENT_COLOR, getCurrentEventColor());
            json.put(PREF_DAY_HEADER_COLOR, getDayHeaderColor());
            json.put(PREF_WIDGET_HEADER_COLOR, getWidgetHeaderColor());
            json.put(PREF_WIDGET_HEADER_BACKGROUND_COLOR, getWidgetHeaderBackgroundColor());
            json.put(PREF_BACKGROUND_COLOR, getBackgroundColor());
            json.put(PREF_PAST_EVENTS_BACKGROUND_COLOR, getPastEventsBackgroundColor());

            json.put(PREF_SHOW_END_TIME, getShowEndTime());
            json.put(PREF_SHOW_LOCATION, getShowLocation());
            json.put(PREF_FILL_ALL_DAY, getFillAllDayEvents());
            json.put(PREF_INDICATE_ALERTS, getIndicateAlerts());
            json.put(PREF_INDICATE_RECURRING, getIndicateRecurring());

            json.put(PREF_EVENTS_ENDED, getEventsEnded().save());
            json.put(PREF_EVENT_RANGE, getEventRange());
            json.put(PREF_HIDE_BASED_ON_KEYWORDS, getHideBasedOnKeywords());
            json.put(PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT, getShowOnlyClosestInstanceOfRecurringEvent());

            if (complete) {
                json.put(PREF_ACTIVE_CALENDARS, new JSONArray(getActiveCalendars()));
            }

            json.put(PREF_TASK_SOURCE, getTaskSource());
            if (complete) {
                json.put(PREF_ACTIVE_TASK_LISTS, new JSONArray(getActiveTaskLists()));
            }

            json.put(PREF_SHOW_BIRTHDAYS, getShowBirthdays());
            json.put(PREF_BIRTHDAY_COLOR, getBirthdayColor());
        } catch (JSONException e) {
            throw new RuntimeException("Saving settings to JSON", e);
        }
        return json;
    }

    public Context getContext() {
        return context;
    }

    public int getWidgetId() {
        return widgetId;
    }

    public String getWidgetInstanceName() {
        return sharedPreferences.getString(PREF_WIDGET_INSTANCE_NAME, "");
    }

    void setWidgetInstanceNameIfNew(String name) {
        if (!sharedPreferences.contains(PREF_WIDGET_INSTANCE_NAME)) {
            sharedPreferences.edit().putString(PREF_WIDGET_INSTANCE_NAME, name).apply();
        }
    }

    public TextSizeScale getTextSizeScale() {
        return TextSizeScale.fromPreferenceValue(sharedPreferences.getString(PREF_TEXT_SIZE_SCALE,
                PREF_TEXT_SIZE_SCALE_DEFAULT));
    }

    public EventEntryLayout getEventEntryLayout() {
        return EventEntryLayout.fromPreferenceValue(sharedPreferences.getString(PREF_EVENT_ENTRY_LAYOUT,
                PREF_EVENT_ENTRY_LAYOUT_DEFAULT));
    }

    public boolean getTitleMultiline() {
        return sharedPreferences.getBoolean(PREF_MULTILINE_TITLE, PREF_MULTILINE_TITLE_DEFAULT);
    }

    public boolean getAbbreviateDates() {
        return sharedPreferences.getBoolean(PREF_ABBREVIATE_DATES, PREF_ABBREVIATE_DATES_DEFAULT);
    }

    public boolean getShowDayHeaders() {
        return sharedPreferences.getBoolean(PREF_SHOW_DAY_HEADERS, PREF_SHOW_DAY_HEADERS_DEFAULT);
    }

    public String getDayHeaderAlignment() {
        return sharedPreferences.getString(PREF_DAY_HEADER_ALIGNMENT, PREF_DAY_HEADER_ALIGNMENT_DEFAULT);
    }

    public boolean getShowDaysWithoutEvents() {
        return sharedPreferences.getBoolean(PREF_SHOW_DAYS_WITHOUT_EVENTS, PREF_SHOW_DAYS_WITHOUT_EVENTS_DEFAULT);
    }

    public boolean getShowWidgetHeader() {
        return sharedPreferences.getBoolean(PREF_SHOW_WIDGET_HEADER, PREF_SHOW_WIDGET_HEADER_DEFAULT);
    }

    public boolean getShowWidgetHeaderSeparator() {
        return sharedPreferences.getBoolean(PREF_SHOW_WIDGET_HEADER_SEPARATOR, PREF_SHOW_WIDGET_HEADER_SEPARATOR_DEFAULT);
    }

    public String getLockedTimeZoneId() {
        return sharedPreferences.getString(PREF_LOCKED_TIME_ZONE_ID, "");
    }

    public boolean isTimeZoneLocked() {
        return !TextUtils.isEmpty(getLockedTimeZoneId());
    }

    public void setLockedTimeZoneId(String lockedTimeZoneId) {
        sharedPreferences.edit().putString(PREF_LOCKED_TIME_ZONE_ID, lockedTimeZoneId).apply();
    }

    public DateTimeZone getTimeZone() {
        return DateTimeZone.forID(DateUtil.validatedTimeZoneId(
                isTimeZoneLocked() ? getLockedTimeZoneId() : TimeZone.getDefault().getID()));
    }

    public int getEventColor() {
        return sharedPreferences.getInt(PREF_EVENT_COLOR, PREF_EVENT_COLOR_DEFAULT);
    }

    public int getCurrentEventColor() {
        return sharedPreferences.getInt(PREF_CURRENT_EVENT_COLOR, PREF_CURRENT_EVENT_COLOR_DEFAULT);
    }

    public int getDayHeaderColor() {
        return sharedPreferences.getInt(PREF_DAY_HEADER_COLOR, PREF_DAY_HEADER_COLOR_DEFAULT);
    }

    public int getWidgetHeaderColor() {
        return sharedPreferences.getInt(PREF_WIDGET_HEADER_COLOR, PREF_WIDGET_HEADER_COLOR_DEFAULT);
    }

    public int getWidgetHeaderBackgroundColor() {
        return sharedPreferences.getInt(PREF_WIDGET_HEADER_BACKGROUND_COLOR, PREF_WIDGET_HEADER_BACKGROUND_COLOR_DEFAULT);
    }

    public int getBackgroundColor() {
        return sharedPreferences.getInt(PREF_BACKGROUND_COLOR, PREF_BACKGROUND_COLOR_DEFAULT);
    }

    public int getPastEventsBackgroundColor() {
        return sharedPreferences.getInt(PREF_PAST_EVENTS_BACKGROUND_COLOR, PREF_PAST_EVENTS_BACKGROUND_COLOR_DEFAULT);
    }

    public boolean getShowEndTime() {
        return sharedPreferences.getBoolean(PREF_SHOW_END_TIME, PREF_SHOW_END_TIME_DEFAULT);
    }

    public boolean getShowLocation() {
        return sharedPreferences.getBoolean(PREF_SHOW_LOCATION, PREF_SHOW_LOCATION_DEFAULT);
    }

    public boolean getFillAllDayEvents() {
        return sharedPreferences.getBoolean(PREF_FILL_ALL_DAY, PREF_FILL_ALL_DAY_DEFAULT);
    }

    public boolean getIndicateAlerts() {
        return sharedPreferences.getBoolean(PREF_INDICATE_ALERTS, PREF_INDICATE_ALERTS_DEFAULT);
    }

    public boolean getIndicateRecurring() {
        return sharedPreferences.getBoolean(PREF_INDICATE_RECURRING, PREF_INDICATE_RECURRING_DEFAULT);
    }

    public EndedSomeTimeAgo getEventsEnded() {
        return EndedSomeTimeAgo.fromPreferenceValue(sharedPreferences.getString(PREF_EVENTS_ENDED,
                PREF_EVENTS_ENDED_DEFAULT));
    }

    public int getEventRange() {
        try {
            return Integer.parseInt(sharedPreferences.getString(PREF_EVENT_RANGE,
                    String.valueOf(PREF_EVENT_RANGE_DEFAULT)));
        } catch (NumberFormatException e) {
            return PREF_EVENT_RANGE_DEFAULT;
        }
    }

    public String getHideBasedOnKeywords() {
        return sharedPreferences.getString(PREF_HIDE_BASED_ON_KEYWORDS, "");
    }

    public boolean getShowOnlyClosestInstanceOfRecurringEvent() {
        return sharedPreferences.getBoolean(PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT,
                PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT_DEFAULT);
    }

    public Set<String> getActiveCalendars() {
        return sharedPreferences.getStringSet(PREF_ACTIVE_CALENDARS, Collections.emptySet());
    }

    public void setActiveCalendars(Set<String> calendars) {
        sharedPreferences.edit().putStringSet(PREF_ACTIVE_CALENDARS, calendars).apply();
    }

    public String getTaskSource() {
        return sharedPreferences.getString(PREF_TASK_SOURCE, PREF_TASK_SOURCE_DEFAULT);
    }

    public Set<String> getActiveTaskLists() {
        return sharedPreferences.getStringSet(PREF_ACTIVE_TASK_LISTS, Collections.emptySet());
    }

    public void setActiveTaskLists(Set<String> taskLists) {
        sharedPreferences.edit().putStringSet(PREF_ACTIVE_TASK_LISTS, taskLists).apply();
    }

    public boolean noPastEvents() {
        return getEventsEnded() == EndedSomeTimeAgo.NONE && noTaskSources();
    }

    private boolean noTaskSources() {
        return getTaskSource().equals(TaskProvider.PROVIDER_NONE);
    }

    public boolean getShowBirthdays() {
        return sharedPreferences.getBoolean(PREF_SHOW_BIRTHDAYS, false);
    }

    public int getBirthdayColor() {
        return sharedPreferences.getInt(PREF_BIRTHDAY_COLOR, PREF_BIRTHDAY_COLOR_DEFAULT);
    }

    void delete() {
        if (!sharedPreferences.edit().clear().commit()) {
            Log.w(getClass().getSimpleName(), "Could not commit prefs change before deletion");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.deleteSharedPreferences(nameForWidget(widgetId));
        } else {
            File sharedPrefsDir = new File(context.getFilesDir().getParentFile(), "shared_prefs");
            File sharedPrefFile = new File(sharedPrefsDir, nameForWidget(widgetId) + ".xml");
            sharedPrefFile.delete();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstanceSettings settings = (InstanceSettings) o;
        return toJsonComplete().toString().equals(settings.toJsonComplete().toString());
    }

    @Override
    public int hashCode() {
        return toJsonComplete().toString().hashCode();
    }
}
