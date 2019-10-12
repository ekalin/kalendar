package org.andstatus.todoagenda.prefs;

import android.content.Context;
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
import org.andstatus.todoagenda.widget.EventEntryLayout;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import static org.andstatus.todoagenda.Theme.themeNameToResId;
import static org.andstatus.todoagenda.prefs.SettingsStorage.saveJson;

/**
 * Loaded settings of one Widget
 *
 * @author yvolk@yurivolkov.com
 */
public class InstanceSettings {
    private final Context context;

    public static final String PREF_WIDGET_ID = "widgetId";
    final int widgetId;

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
    static final String PREF_DATE_FORMAT = "dateFormat";
    static final String PREF_DATE_FORMAT_DEFAULT = "auto";
    private String dateFormat = PREF_DATE_FORMAT_DEFAULT;
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

    // Appearance - colors
    static final String PREF_HEADER_THEME = "headerTheme";
    static final String PREF_HEADER_THEME_DEFAULT = Theme.WHITE.name();
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
    static final String PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR = "showPastEventsWithDefaultColor";
    private boolean showPastEventsWithDefaultColor = false;
    static final String PREF_EVENT_RANGE = "eventRange";
    static final String PREF_EVENT_RANGE_DEFAULT = "30";
    private int eventRange = Integer.parseInt(PREF_EVENT_RANGE_DEFAULT);
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

    // Feedback
    static final String KEY_SHARE_EVENTS_FOR_DEBUGGING = "shareEventsForDebugging";

    public static InstanceSettings fromJson(Context context, JSONObject json) throws JSONException {
        InstanceSettings settings = new InstanceSettings(context, json.optInt(PREF_WIDGET_ID),
                json.optString(PREF_WIDGET_INSTANCE_NAME));
        if (settings.widgetId == 0) {
            return settings;
        }

        if (json.has(PREF_TEXT_SIZE_SCALE)) {
            settings.textSizeScale = TextSizeScale.fromPreferenceValue(json.getString(PREF_TEXT_SIZE_SCALE));
        }
        if (json.has(PREF_EVENT_ENTRY_LAYOUT)) {
            settings.eventEntryLayout = EventEntryLayout.fromValue(json.getString(PREF_EVENT_ENTRY_LAYOUT));
        }
        if (json.has(PREF_MULTILINE_TITLE)) {
            settings.titleMultiline = json.getBoolean(PREF_MULTILINE_TITLE);
        }
        if (json.has(PREF_DATE_FORMAT)) {
            settings.dateFormat = json.getString(PREF_DATE_FORMAT);
        }
        if (json.has(PREF_ABBREVIATE_DATES)) {
            settings.abbreviateDates = json.getBoolean(PREF_ABBREVIATE_DATES);
        }
        if (json.has(PREF_SHOW_DAY_HEADERS)) {
            settings.showDayHeaders = json.getBoolean(PREF_SHOW_DAY_HEADERS);
        }
        if (json.has(PREF_DAY_HEADER_ALIGNMENT)) {
            settings.dayHeaderAlignment = json.getString(PREF_DAY_HEADER_ALIGNMENT);
        }
        if (json.has(PREF_SHOW_DAYS_WITHOUT_EVENTS)) {
            settings.showDaysWithoutEvents = json.getBoolean(PREF_SHOW_DAYS_WITHOUT_EVENTS);
        }
        if (json.has(PREF_SHOW_WIDGET_HEADER)) {
            settings.showWidgetHeader = json.getBoolean(PREF_SHOW_WIDGET_HEADER);
        }
        if (json.has(PREF_LOCKED_TIME_ZONE_ID)) {
            settings.setLockedTimeZoneId(json.getString(PREF_LOCKED_TIME_ZONE_ID));
        }

        if (json.has(PREF_HEADER_THEME)) {
            settings.headerTheme = json.getString(PREF_HEADER_THEME);
        }
        if (json.has(PREF_BACKGROUND_COLOR)) {
            settings.backgroundColor = json.getInt(PREF_BACKGROUND_COLOR);
        }
        if (json.has(PREF_PAST_EVENTS_BACKGROUND_COLOR)) {
            settings.pastEventsBackgroundColor = json.getInt(PREF_PAST_EVENTS_BACKGROUND_COLOR);
        }
        if (json.has(PREF_ENTRY_THEME)) {
            settings.entryTheme = json.getString(PREF_ENTRY_THEME);
        }

        if (json.has(PREF_SHOW_END_TIME)) {
            settings.showEndTime = json.getBoolean(PREF_SHOW_END_TIME);
        }
        if (json.has(PREF_SHOW_LOCATION)) {
            settings.showLocation = json.getBoolean(PREF_SHOW_LOCATION);
        }
        if (json.has(PREF_FILL_ALL_DAY)) {
            settings.fillAllDayEvents = json.getBoolean(PREF_FILL_ALL_DAY);
        }
        if (json.has(PREF_INDICATE_ALERTS)) {
            settings.indicateAlerts = json.getBoolean(PREF_INDICATE_ALERTS);
        }
        if (json.has(PREF_INDICATE_RECURRING)) {
            settings.indicateRecurring = json.getBoolean(PREF_INDICATE_RECURRING);
        }

        if (json.has(PREF_EVENTS_ENDED)) {
            settings.eventsEnded = EndedSomeTimeAgo.fromValue(json.getString(PREF_EVENTS_ENDED));
        }
        if (json.has(PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR)) {
            settings.showPastEventsWithDefaultColor = json.getBoolean(PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR);
        }
        if (json.has(PREF_EVENT_RANGE)) {
            settings.eventRange = json.getInt(PREF_EVENT_RANGE);
        }
        if (json.has(PREF_HIDE_BASED_ON_KEYWORDS)) {
            settings.hideBasedOnKeywords = json.getString(PREF_HIDE_BASED_ON_KEYWORDS);
        }
        if (json.has(PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT)) {
            settings.showOnlyClosestInstanceOfRecurringEvent = json.getBoolean(
                    PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT);
        }

        if (json.has(PREF_ACTIVE_CALENDARS)) {
            settings.activeCalendars = jsonArray2StringSet(json.getJSONArray(PREF_ACTIVE_CALENDARS));
        }

        if (json.has(PREF_TASK_SOURCE)) {
            settings.taskSource = json.getString(PREF_TASK_SOURCE);
        }
        if (json.has(PREF_ACTIVE_TASK_LISTS)) {
            settings.activeTaskLists = jsonArray2StringSet(json.getJSONArray(PREF_ACTIVE_TASK_LISTS));
        }

        return settings;
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
        synchronized (ApplicationPreferences.class) {
            InstanceSettings settings = new InstanceSettings(context, widgetId,
                    ApplicationPreferences.getString(context, PREF_WIDGET_INSTANCE_NAME,
                            ApplicationPreferences.getString(context, PREF_WIDGET_INSTANCE_NAME, "")));

            settings.textSizeScale = TextSizeScale.fromPreferenceValue(ApplicationPreferences.getString(context,
                    PREF_TEXT_SIZE_SCALE,
                    ""));
            settings.eventEntryLayout = ApplicationPreferences.getEventEntryLayout(context);
            settings.titleMultiline = ApplicationPreferences.isTitleMultiline(context);
            settings.dateFormat = ApplicationPreferences.getDateFormat(context);
            settings.abbreviateDates = ApplicationPreferences.getAbbreviateDates(context);
            settings.showDayHeaders = ApplicationPreferences.getShowDayHeaders(context);
            settings.dayHeaderAlignment = ApplicationPreferences.getString(context, PREF_DAY_HEADER_ALIGNMENT,
                    PREF_DAY_HEADER_ALIGNMENT_DEFAULT);
            settings.showDaysWithoutEvents = ApplicationPreferences.getShowDaysWithoutEvents(context);
            settings.showWidgetHeader = ApplicationPreferences.getBoolean(context, PREF_SHOW_WIDGET_HEADER, true);
            settings.setLockedTimeZoneId(ApplicationPreferences.getLockedTimeZoneId(context));

            settings.headerTheme = ApplicationPreferences.getString(context, PREF_HEADER_THEME,
                    PREF_HEADER_THEME_DEFAULT);
            settings.backgroundColor = ApplicationPreferences.getInt(context, PREF_BACKGROUND_COLOR,
                    PREF_BACKGROUND_COLOR_DEFAULT);
            settings.pastEventsBackgroundColor = ApplicationPreferences.getPastEventsBackgroundColor(context);
            settings.entryTheme = ApplicationPreferences.getString(context, PREF_ENTRY_THEME, PREF_ENTRY_THEME_DEFAULT);

            settings.showEndTime = ApplicationPreferences.getShowEndTime(context);
            settings.showLocation = ApplicationPreferences.getShowLocation(context);
            settings.fillAllDayEvents = ApplicationPreferences.getFillAllDayEvents(context);
            settings.indicateAlerts = ApplicationPreferences.getBoolean(context, PREF_INDICATE_ALERTS, true);
            settings.indicateRecurring = ApplicationPreferences.getBoolean(context, PREF_INDICATE_RECURRING, false);

            settings.eventsEnded = ApplicationPreferences.getEventsEnded(context);
            settings.showPastEventsWithDefaultColor = ApplicationPreferences.getShowPastEventsWithDefaultColor(context);
            settings.eventRange = ApplicationPreferences.getEventRange(context);
            settings.hideBasedOnKeywords = ApplicationPreferences.getHideBasedOnKeywords(context);
            settings.showOnlyClosestInstanceOfRecurringEvent = ApplicationPreferences
                    .getShowOnlyClosestInstanceOfRecurringEvent(context);

            settings.activeCalendars = ApplicationPreferences.getActiveCalendars(context);

            settings.taskSource = ApplicationPreferences.getTaskSource(context);
            settings.activeTaskLists = ApplicationPreferences.getActiveTaskLists(context);

            return settings;
        }
    }

    @NonNull
    private static String getStorageKey(int widgetId) {
        return "instanceSettings" + widgetId;
    }

    InstanceSettings(Context context, int widgetId, String proposedInstanceName) {
        this.context = context;
        this.widgetId = widgetId;
        this.widgetInstanceName = AllSettings.uniqueInstanceName(context, widgetId, proposedInstanceName);
    }

    void save() {
        if (widgetId == 0) {
            logMe(InstanceSettings.class, "Skipped save", widgetId);
            return;
        }
        logMe(InstanceSettings.class, "save", widgetId);
        try {
            saveJson(context, getStorageKey(widgetId), toJson());
        } catch (IOException e) {
            Log.e("save", toString(), e);
        }
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(PREF_WIDGET_ID, widgetId);
            json.put(PREF_WIDGET_INSTANCE_NAME, widgetInstanceName);
            json.put(PREF_TEXT_SIZE_SCALE, textSizeScale.preferenceValue);
            json.put(PREF_EVENT_ENTRY_LAYOUT, eventEntryLayout.value);
            json.put(PREF_MULTILINE_TITLE, titleMultiline);
            json.put(PREF_DATE_FORMAT, dateFormat);
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
            json.put(PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR, showPastEventsWithDefaultColor);
            json.put(PREF_EVENT_RANGE, eventRange);
            json.put(PREF_HIDE_BASED_ON_KEYWORDS, hideBasedOnKeywords);
            json.put(PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT, showOnlyClosestInstanceOfRecurringEvent);

            json.put(PREF_ACTIVE_CALENDARS, new JSONArray(activeCalendars));

            json.put(PREF_TASK_SOURCE, taskSource);
            json.put(PREF_ACTIVE_TASK_LISTS, new JSONArray(activeTaskLists));
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
        return widgetInstanceName;
    }

    public TextSizeScale getTextSizeScale() {
        return textSizeScale;
    }

    public EventEntryLayout getEventEntryLayout() {
        return eventEntryLayout;
    }

    public boolean isTitleMultiline() {
        return titleMultiline;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public boolean getAbbreviateDates() {
        return abbreviateDates;
    }

    public boolean getShowDayHeaders() {
        return showDayHeaders;
    }

    public String getDayHeaderAlignment() {
        return dayHeaderAlignment;
    }

    public boolean getShowDaysWithoutEvents() {
        return showDaysWithoutEvents;
    }

    public boolean getShowWidgetHeader() {
        return showWidgetHeader;
    }

    public String getLockedTimeZoneId() {
        return lockedTimeZoneId;
    }

    public boolean isTimeZoneLocked() {
        return !TextUtils.isEmpty(lockedTimeZoneId);
    }

    private void setLockedTimeZoneId(String lockedTimeZoneId) {
        this.lockedTimeZoneId = DateUtil.validatedTimeZoneId(lockedTimeZoneId);
    }

    public DateTimeZone getTimeZone() {
        return DateTimeZone.forID(DateUtil.validatedTimeZoneId(
                isTimeZoneLocked() ? lockedTimeZoneId : TimeZone.getDefault().getID()));
    }

    public String getHeaderTheme() {
        return headerTheme;
    }

    public ContextThemeWrapper getHeaderThemeContext() {
        if (headerThemeContext == null) {
            headerThemeContext = new ContextThemeWrapper(context, themeNameToResId(headerTheme));
        }
        return headerThemeContext;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getPastEventsBackgroundColor() {
        return pastEventsBackgroundColor;
    }

    public String getEntryTheme() {
        return entryTheme;
    }

    public ContextThemeWrapper getEntryThemeContext() {
        if (entryThemeContext == null) {
            entryThemeContext = new ContextThemeWrapper(context, themeNameToResId(entryTheme));
        }
        return entryThemeContext;
    }

    public boolean getShowEndTime() {
        return showEndTime;
    }

    public boolean getShowLocation() {
        return showLocation;
    }

    public boolean getFillAllDayEvents() {
        return fillAllDayEvents;
    }

    public boolean getIndicateAlerts() {
        return indicateAlerts;
    }

    public boolean getIndicateRecurring() {
        return indicateRecurring;
    }

    public EndedSomeTimeAgo getEventsEnded() {
        return eventsEnded;
    }

    public boolean getShowPastEventsWithDefaultColor() {
        return showPastEventsWithDefaultColor;
    }

    public int getEventRange() {
        return eventRange;
    }

    public String getHideBasedOnKeywords() {
        return hideBasedOnKeywords;
    }

    public boolean getShowOnlyClosestInstanceOfRecurringEvent() {
        return showOnlyClosestInstanceOfRecurringEvent;
    }

    public Set<String> getActiveCalendars() {
        return activeCalendars;
    }

    public String getTaskSource() {
        return taskSource;
    }

    public Set<String> getActiveTaskLists() {
        return activeTaskLists;
    }

    public boolean noPastEvents() {
        return !getShowPastEventsWithDefaultColor()
                && getEventsEnded() == EndedSomeTimeAgo.NONE
                && noTaskSources();
    }

    private boolean noTaskSources() {
        return getTaskSource().equals(TaskProvider.PROVIDER_NONE);
    }

    public void logMe(Class tag, String message, int widgetId) {
        Log.v(tag.getSimpleName(), message + ", widgetId:" + widgetId + "\n" + toJson());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstanceSettings settings = (InstanceSettings) o;
        return toJson().toString().equals(settings.toJson().toString());
    }

    @Override
    public int hashCode() {
        return toJson().toString().hashCode();
    }
}
