package org.andstatus.todoagenda.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

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
    private final Context context;

    public static final String PREF_WIDGET_ID = "widgetId";
    final int widgetId;
    final SharedPreferences sharedPreferences;

    // Appearance
    static final String PREF_WIDGET_INSTANCE_NAME = "widgetInstanceName";
    private final String widgetInstanceName;
    static final String PREF_TEXT_SIZE_SCALE = "textSizeScale";
    private TextSizeScale textSizeScale = TextSizeScale.MEDIUM;
    static final String PREF_EVENT_ENTRY_LAYOUT = "eventEntryLayout";
    private EventEntryLayout eventEntryLayout = EventEntryLayout.DEFAULT;
    static final String PREF_MULTILINE_TITLE = "multiline_title";
    static final boolean PREF_MULTILINE_TITLE_DEFAULT = false;
    private boolean titleMultiline = PREF_MULTILINE_TITLE_DEFAULT;
    static final String PREF_ABBREVIATE_DATES = "abbreviateDates";
    static final boolean PREF_ABBREVIATE_DATES_DEFAULT = false;
    private boolean abbreviateDates = PREF_ABBREVIATE_DATES_DEFAULT;
    static final String PREF_SHOW_DAY_HEADERS = "showDayHeaders";
    private boolean showDayHeaders = true;
    static final String PREF_DAY_HEADER_ALIGNMENT = "dayHeaderAlignment";
    static final String PREF_DAY_HEADER_ALIGNMENT_DEFAULT = Alignment.LEFT.name();
    private String dayHeaderAlignment = PREF_DAY_HEADER_ALIGNMENT_DEFAULT;
    static final String PREF_SHOW_DAYS_WITHOUT_EVENTS = "showDaysWithoutEvents";
    private boolean showDaysWithoutEvents = false;
    static final String PREF_SHOW_WIDGET_HEADER = "showHeader";
    private boolean showWidgetHeader = true;
    static final String PREF_LOCK_TIME_ZONE = "lockTimeZone";
    static final String PREF_LOCKED_TIME_ZONE_ID = "lockedTimeZoneId";
    private String lockedTimeZoneId = "";

    // Colors
    static final String PREF_HEADER_THEME = "headerTheme";
    static final String PREF_HEADER_THEME_DEFAULT = Theme.LIGHT.name();
    private String headerTheme = PREF_HEADER_THEME_DEFAULT;
    static final String PREF_BACKGROUND_COLOR = "backgroundColor";
    @ColorInt static final int PREF_BACKGROUND_COLOR_DEFAULT = 0x80000000;
    private int backgroundColor = PREF_BACKGROUND_COLOR_DEFAULT;
    static final String PREF_PAST_EVENTS_BACKGROUND_COLOR = "pastEventsBackgroundColor";
    @ColorInt static final int PREF_PAST_EVENTS_BACKGROUND_COLOR_DEFAULT = 0x4affff2b;
    private int pastEventsBackgroundColor = PREF_PAST_EVENTS_BACKGROUND_COLOR_DEFAULT;
    static final String PREF_ENTRY_THEME = "entryTheme";
    public static final String PREF_ENTRY_THEME_DEFAULT = Theme.WHITE.name();
    private String entryTheme = PREF_ENTRY_THEME_DEFAULT;

    private volatile ContextThemeWrapper entryThemeContext = null;
    private volatile ContextThemeWrapper headerThemeContext = null;

    // Event details
    static final String PREF_SHOW_END_TIME = "showEndTime";
    static final boolean PREF_SHOW_END_TIME_DEFAULT = true;
    private boolean showEndTime = PREF_SHOW_END_TIME_DEFAULT;
    static final String PREF_SHOW_LOCATION = "showLocation";
    static final boolean PREF_SHOW_LOCATION_DEFAULT = true;
    private boolean showLocation = PREF_SHOW_LOCATION_DEFAULT;
    static final String PREF_FILL_ALL_DAY = "fillAllDay";
    static final boolean PREF_FILL_ALL_DAY_DEFAULT = true;
    private boolean fillAllDayEvents = PREF_FILL_ALL_DAY_DEFAULT;
    static final String PREF_INDICATE_ALERTS = "indicateAlerts";
    private boolean indicateAlerts = true;
    static final String PREF_INDICATE_RECURRING = "indicateRecurring";
    private boolean indicateRecurring = false;

    // Event filters
    static final String PREF_EVENTS_ENDED = "eventsEnded";
    private EndedSomeTimeAgo eventsEnded = EndedSomeTimeAgo.NONE;
    static final String PREF_EVENT_RANGE = "eventRange";
    static final int PREF_EVENT_RANGE_DEFAULT = 30;
    private int eventRange = PREF_EVENT_RANGE_DEFAULT;
    static final String PREF_HIDE_BASED_ON_KEYWORDS = "hideBasedOnKeywords";
    private String hideBasedOnKeywords = "";
    static final String PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT =
            "showOnlyClosestInstanceOfRecurringEvent";
    private boolean showOnlyClosestInstanceOfRecurringEvent = false;

    // Calendars
    static final String PREF_ACTIVE_CALENDARS = "activeCalendars";
    private Set<String> activeCalendars = Collections.emptySet();

    // Tasks
    static final String PREF_TASK_SOURCE = "taskSource";
    static final String PREF_TASK_SOURCE_DEFAULT = TaskProvider.PROVIDER_NONE;
    static final String KEY_PREF_GRANT_TASK_PERMISSION = "grantTaskPermission";
    private String taskSource = PREF_TASK_SOURCE_DEFAULT;
    static final String PREF_ACTIVE_TASK_LISTS = "activeTaskLists";
    private Set<String> activeTaskLists = Collections.emptySet();

    public static Optional<InstanceSettings> fromJson(Context context, JSONObject json) {
        int widgetId = json.optInt(PREF_WIDGET_ID);
        if (widgetId == 0) {
            return Optional.empty();
        }

        return Optional.of(fromJsonForWidget(context, widgetId, json));
    }

    public static InstanceSettings fromJsonForWidget(Context context, int targetWidgetId, JSONObject json) {
        InstanceSettings settings = new InstanceSettings(context, targetWidgetId, json.optString(PREF_WIDGET_INSTANCE_NAME));
        settings.setFromJson(json);
        return settings;
    }

    private void setFromJson(JSONObject json) {
        try {
            if (json.has(PREF_TEXT_SIZE_SCALE)) {
                textSizeScale = TextSizeScale.fromPreferenceValue(json.getString(PREF_TEXT_SIZE_SCALE));
            }
            if (json.has(PREF_EVENT_ENTRY_LAYOUT)) {
                eventEntryLayout = EventEntryLayout.fromPreferenceValue(json.getString(PREF_EVENT_ENTRY_LAYOUT));
            }
            if (json.has(PREF_MULTILINE_TITLE)) {
                titleMultiline = json.getBoolean(PREF_MULTILINE_TITLE);
            }
            if (json.has(PREF_ABBREVIATE_DATES)) {
                abbreviateDates = json.getBoolean(PREF_ABBREVIATE_DATES);
            }
            if (json.has(PREF_SHOW_DAY_HEADERS)) {
                showDayHeaders = json.getBoolean(PREF_SHOW_DAY_HEADERS);
            }
            if (json.has(PREF_DAY_HEADER_ALIGNMENT)) {
                dayHeaderAlignment = json.getString(PREF_DAY_HEADER_ALIGNMENT);
            }
            if (json.has(PREF_SHOW_DAYS_WITHOUT_EVENTS)) {
                showDaysWithoutEvents = json.getBoolean(PREF_SHOW_DAYS_WITHOUT_EVENTS);
            }
            if (json.has(PREF_SHOW_WIDGET_HEADER)) {
                showWidgetHeader = json.getBoolean(PREF_SHOW_WIDGET_HEADER);
            }
            if (json.has(PREF_LOCKED_TIME_ZONE_ID)) {
                setLockedTimeZoneId(json.getString(PREF_LOCKED_TIME_ZONE_ID));
            }

            if (json.has(PREF_HEADER_THEME)) {
                headerTheme = json.getString(PREF_HEADER_THEME);
            }
            if (json.has(PREF_BACKGROUND_COLOR)) {
                backgroundColor = json.getInt(PREF_BACKGROUND_COLOR);
            }
            if (json.has(PREF_PAST_EVENTS_BACKGROUND_COLOR)) {
                pastEventsBackgroundColor = json.getInt(PREF_PAST_EVENTS_BACKGROUND_COLOR);
            }
            if (json.has(PREF_ENTRY_THEME)) {
                entryTheme = json.getString(PREF_ENTRY_THEME);
            }

            if (json.has(PREF_SHOW_END_TIME)) {
                showEndTime = json.getBoolean(PREF_SHOW_END_TIME);
            }
            if (json.has(PREF_SHOW_LOCATION)) {
                showLocation = json.getBoolean(PREF_SHOW_LOCATION);
            }
            if (json.has(PREF_FILL_ALL_DAY)) {
                fillAllDayEvents = json.getBoolean(PREF_FILL_ALL_DAY);
            }
            if (json.has(PREF_INDICATE_ALERTS)) {
                indicateAlerts = json.getBoolean(PREF_INDICATE_ALERTS);
            }
            if (json.has(PREF_INDICATE_RECURRING)) {
                indicateRecurring = json.getBoolean(PREF_INDICATE_RECURRING);
            }

            if (json.has(PREF_EVENTS_ENDED)) {
                eventsEnded = EndedSomeTimeAgo.fromPreferenceValue(json.getString(PREF_EVENTS_ENDED));
            }
            if (json.has(PREF_EVENT_RANGE)) {
                eventRange = json.getInt(PREF_EVENT_RANGE);
            }
            if (json.has(PREF_HIDE_BASED_ON_KEYWORDS)) {
                hideBasedOnKeywords = json.getString(PREF_HIDE_BASED_ON_KEYWORDS);
            }
            if (json.has(PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT)) {
                showOnlyClosestInstanceOfRecurringEvent = json.getBoolean(
                        PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT);
            }

            if (json.has(PREF_ACTIVE_CALENDARS)) {
                activeCalendars = jsonArray2StringSet(json.getJSONArray(PREF_ACTIVE_CALENDARS));
            }

            if (json.has(PREF_TASK_SOURCE)) {
                taskSource = json.getString(PREF_TASK_SOURCE);
            }
            if (json.has(PREF_ACTIVE_TASK_LISTS)) {
                activeTaskLists = jsonArray2StringSet(json.getJSONArray(PREF_ACTIVE_TASK_LISTS));
            }
        } catch (JSONException e) {
            Log.w(InstanceSettings.class.getSimpleName(), "setFromJson failed, widgetId:" + widgetId + "\n" + json);
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

    static InstanceSettings fromApplicationPreferences(Context context, int widgetId) {
            InstanceSettings settings = new InstanceSettings(context, widgetId,
                    ApplicationPreferences.getString(context, PREF_WIDGET_INSTANCE_NAME,
                            ApplicationPreferences.getString(context, PREF_WIDGET_INSTANCE_NAME, "")));
            return settings;
    }

    @NonNull
    private static String getStorageKey(int widgetId) {
        return "instanceSettings" + widgetId;
    }

    InstanceSettings(Context context, int widgetId, String proposedInstanceName) {
        this.context = context;
        this.widgetId = widgetId;
        this.widgetInstanceName = AllSettings.uniqueInstanceName(context, widgetId, proposedInstanceName);
        this.sharedPreferences = context.getSharedPreferences(nameForWidget(widgetId), Context.MODE_PRIVATE);
    }

    public static String nameForWidget(int widgetId) {
        return "widget" + widgetId;
    }

    void save() {
        // noop
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
            json.put(PREF_WIDGET_ID, widgetId);
            json.put(PREF_WIDGET_INSTANCE_NAME, widgetInstanceName);
            json.put(PREF_TEXT_SIZE_SCALE, textSizeScale.preferenceValue);
            json.put(PREF_EVENT_ENTRY_LAYOUT, eventEntryLayout.value);
            json.put(PREF_MULTILINE_TITLE, titleMultiline);
            json.put(PREF_ABBREVIATE_DATES, abbreviateDates);
            json.put(PREF_SHOW_DAY_HEADERS, showDayHeaders);
            json.put(PREF_DAY_HEADER_ALIGNMENT, dayHeaderAlignment);
            json.put(PREF_SHOW_DAYS_WITHOUT_EVENTS, showDaysWithoutEvents);
            json.put(PREF_SHOW_WIDGET_HEADER, showWidgetHeader);
            json.put(PREF_LOCKED_TIME_ZONE_ID, lockedTimeZoneId);

            json.put(PREF_HEADER_THEME, headerTheme);
            json.put(PREF_BACKGROUND_COLOR, backgroundColor);
            json.put(PREF_PAST_EVENTS_BACKGROUND_COLOR, pastEventsBackgroundColor);
            json.put(PREF_ENTRY_THEME, entryTheme);

            json.put(PREF_SHOW_END_TIME, showEndTime);
            json.put(PREF_SHOW_LOCATION, showLocation);
            json.put(PREF_FILL_ALL_DAY, fillAllDayEvents);
            json.put(PREF_INDICATE_ALERTS, indicateAlerts);
            json.put(PREF_INDICATE_RECURRING, indicateRecurring);

            json.put(PREF_EVENTS_ENDED, eventsEnded.save());
            json.put(PREF_EVENT_RANGE, eventRange);
            json.put(PREF_HIDE_BASED_ON_KEYWORDS, hideBasedOnKeywords);
            json.put(PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT, showOnlyClosestInstanceOfRecurringEvent);

            if (complete) {
                json.put(PREF_ACTIVE_CALENDARS, new JSONArray(activeCalendars));
            }

            json.put(PREF_TASK_SOURCE, taskSource);
            if (complete) {
                json.put(PREF_ACTIVE_TASK_LISTS, new JSONArray(activeTaskLists));
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

    public TextSizeScale getTextSizeScale() {
        return TextSizeScale.fromPreferenceValue(sharedPreferences.getString(PREF_TEXT_SIZE_SCALE, ""));
    }

    public EventEntryLayout getEventEntryLayout() {
        return EventEntryLayout.fromPreferenceValue(sharedPreferences.getString(PREF_EVENT_ENTRY_LAYOUT, ""));
    }

    public boolean isTitleMultiline() {
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
