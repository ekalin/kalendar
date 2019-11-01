package org.andstatus.todoagenda.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import androidx.annotation.ColorInt;

import org.andstatus.todoagenda.Alignment;
import org.andstatus.todoagenda.EndedSomeTimeAgo;
import org.andstatus.todoagenda.TextSizeScale;
import org.andstatus.todoagenda.Theme;
import org.andstatus.todoagenda.task.TaskProvider;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.util.Optional;
import org.andstatus.todoagenda.widget.EventEntryLayout;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import static org.andstatus.todoagenda.Theme.themeNameToResId;

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
    static final String PREF_EVENT_ENTRY_LAYOUT = "eventEntryLayout";
    static final String PREF_MULTILINE_TITLE = "multilineTitle";
    static final boolean PREF_MULTILINE_TITLE_DEFAULT = false;
    static final String PREF_ABBREVIATE_DATES = "abbreviateDates";
    static final boolean PREF_ABBREVIATE_DATES_DEFAULT = false;
    static final String PREF_SHOW_DAY_HEADERS = "showDayHeaders";
    static final String PREF_DAY_HEADER_ALIGNMENT = "dayHeaderAlignment";
    static final String PREF_DAY_HEADER_ALIGNMENT_DEFAULT = Alignment.LEFT.name();
    static final String PREF_SHOW_DAYS_WITHOUT_EVENTS = "showDaysWithoutEvents";
    static final String PREF_SHOW_WIDGET_HEADER = "showHeader";
    static final String PREF_LOCKED_TIME_ZONE_ID = "lockedTimeZoneId";

    // Colors
    static final String PREF_HEADER_THEME = "headerTheme";
    static final String PREF_HEADER_THEME_DEFAULT = Theme.LIGHT.name();
    static final String PREF_BACKGROUND_COLOR = "backgroundColor";
    @ColorInt static final int PREF_BACKGROUND_COLOR_DEFAULT = 0x80000000;
    static final String PREF_PAST_EVENTS_BACKGROUND_COLOR = "pastEventsBackgroundColor";
    @ColorInt static final int PREF_PAST_EVENTS_BACKGROUND_COLOR_DEFAULT = 0x4affff2b;
    static final String PREF_ENTRY_THEME = "entryTheme";
    public static final String PREF_ENTRY_THEME_DEFAULT = Theme.WHITE.name();

    private volatile ContextThemeWrapper headerThemeContext = null;
    private volatile ContextThemeWrapper entryThemeContext = null;

    // Event details
    static final String PREF_SHOW_END_TIME = "showEndTime";
    static final boolean PREF_SHOW_END_TIME_DEFAULT = true;
    static final String PREF_SHOW_LOCATION = "showLocation";
    static final boolean PREF_SHOW_LOCATION_DEFAULT = true;
    static final String PREF_FILL_ALL_DAY = "fillAllDay";
    static final boolean PREF_FILL_ALL_DAY_DEFAULT = true;
    static final String PREF_INDICATE_ALERTS = "indicateAlerts";
    static final String PREF_INDICATE_RECURRING = "indicateRecurring";

    // Event filters
    static final String PREF_EVENTS_ENDED = "eventsEnded";
    static final String PREF_EVENT_RANGE = "eventRange";
    static final int PREF_EVENT_RANGE_DEFAULT = 30;
    static final String PREF_HIDE_BASED_ON_KEYWORDS = "hideBasedOnKeywords";
    static final String PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT =
            "showOnlyClosestInstanceOfRecurringEvent";

    // Calendars
    static final String PREF_ACTIVE_CALENDARS = "activeCalendars";

    // Tasks
    static final String PREF_TASK_SOURCE = "taskSource";
    static final String PREF_TASK_SOURCE_DEFAULT = TaskProvider.PROVIDER_NONE;
    static final String PREF_ACTIVE_TASK_LISTS = "activeTaskLists";


    private final Context context;
    private final int widgetId;
    private final SharedPreferences sharedPreferences;

    public static Optional<InstanceSettings> fromJson(Context context, JSONObject json) {
        int widgetId = json.optInt(PREF_WIDGET_ID);
        if (widgetId == 0) {
            return Optional.empty();
        }

        return Optional.of(fromJsonForWidget(context, widgetId, json));
    }

    public static InstanceSettings fromJsonForWidget(Context context, int targetWidgetId, JSONObject json) {
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
            setStringFromJson(editor, json, PREF_LOCKED_TIME_ZONE_ID);

            setStringFromJson(editor, json, PREF_HEADER_THEME);
            setIntFromJson(editor, json, PREF_BACKGROUND_COLOR);
            setIntFromJson(editor, json, PREF_PAST_EVENTS_BACKGROUND_COLOR);
            setStringFromJson(editor, json, PREF_ENTRY_THEME);

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
            json.put(PREF_LOCKED_TIME_ZONE_ID, getLockedTimeZoneId());

            json.put(PREF_HEADER_THEME, getHeaderTheme());
            json.put(PREF_BACKGROUND_COLOR, getBackgroundColor());
            json.put(PREF_PAST_EVENTS_BACKGROUND_COLOR, getPastEventsBackgroundColor());
            json.put(PREF_ENTRY_THEME, getEntryTheme());

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

    public void setWidgetInstanceNameIfNew(String name) {
        if (!sharedPreferences.contains(PREF_WIDGET_INSTANCE_NAME)) {
            sharedPreferences.edit().putString(PREF_WIDGET_INSTANCE_NAME, name).apply();
        }
    }

    public TextSizeScale getTextSizeScale() {
        return TextSizeScale.fromPreferenceValue(sharedPreferences.getString(PREF_TEXT_SIZE_SCALE, ""));
    }

    public EventEntryLayout getEventEntryLayout() {
        return EventEntryLayout.fromPreferenceValue(sharedPreferences.getString(PREF_EVENT_ENTRY_LAYOUT, ""));
    }

    public boolean getTitleMultiline() {
        return sharedPreferences.getBoolean(PREF_MULTILINE_TITLE, PREF_MULTILINE_TITLE_DEFAULT);
    }

    public boolean getAbbreviateDates() {
        return sharedPreferences.getBoolean(PREF_ABBREVIATE_DATES, PREF_ABBREVIATE_DATES_DEFAULT);
    }

    public boolean getShowDayHeaders() {
        return sharedPreferences.getBoolean(PREF_SHOW_DAY_HEADERS, true);
    }

    public String getDayHeaderAlignment() {
        return sharedPreferences.getString(PREF_DAY_HEADER_ALIGNMENT, PREF_DAY_HEADER_ALIGNMENT_DEFAULT);
    }

    public boolean getShowDaysWithoutEvents() {
        return sharedPreferences.getBoolean(PREF_SHOW_DAYS_WITHOUT_EVENTS, false);
    }

    public boolean getShowWidgetHeader() {
        return sharedPreferences.getBoolean(PREF_SHOW_WIDGET_HEADER, true);
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

    public String getHeaderTheme() {
        return sharedPreferences.getString(PREF_HEADER_THEME, PREF_HEADER_THEME_DEFAULT);
    }

    public ContextThemeWrapper getHeaderThemeContext() {
        if (headerThemeContext == null) {
            headerThemeContext = new ContextThemeWrapper(context, themeNameToResId(getHeaderTheme()));
        }
        return headerThemeContext;
    }

    public int getBackgroundColor() {
        return sharedPreferences.getInt(PREF_BACKGROUND_COLOR, PREF_BACKGROUND_COLOR_DEFAULT);
    }

    public void setBackgroundColor(@ColorInt int color) {
        sharedPreferences.edit().putInt(PREF_BACKGROUND_COLOR, color).apply();
    }

    public int getPastEventsBackgroundColor() {
        return sharedPreferences.getInt(PREF_PAST_EVENTS_BACKGROUND_COLOR, PREF_PAST_EVENTS_BACKGROUND_COLOR_DEFAULT);
    }

    public void setPastEventsBackgroundColor(@ColorInt int color) {
        sharedPreferences.edit().putInt(PREF_PAST_EVENTS_BACKGROUND_COLOR, color).apply();
    }

    public String getEntryTheme() {
        return sharedPreferences.getString(PREF_ENTRY_THEME, PREF_ENTRY_THEME_DEFAULT);
    }

    public ContextThemeWrapper getEntryThemeContext() {
        if (entryThemeContext == null) {
            entryThemeContext = new ContextThemeWrapper(context, themeNameToResId(getEntryTheme()));
        }
        return entryThemeContext;
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
        return sharedPreferences.getBoolean(PREF_INDICATE_ALERTS, true);
    }

    public boolean getIndicateRecurring() {
        return sharedPreferences.getBoolean(PREF_INDICATE_RECURRING, false);
    }

    public EndedSomeTimeAgo getEventsEnded() {
        return EndedSomeTimeAgo.fromPreferenceValue(sharedPreferences.getString(PREF_EVENTS_ENDED, ""));
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
        return sharedPreferences.getBoolean(PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT, false);
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

    public void delete() {
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

    public void logMe(Class tag, String message, int widgetId) {
        Log.v(tag.getSimpleName(), message + ", widgetId:" + widgetId + "\n" + toJsonComplete());
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
