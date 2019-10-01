package org.andstatus.todoagenda.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import org.andstatus.todoagenda.EndedSomeTimeAgo;
import org.andstatus.todoagenda.widget.EventEntryLayout;

import java.util.Collections;
import java.util.Set;

import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_ABBREVIATE_DATES;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_ABBREVIATE_DATES_DEFAULT;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_ACTIVE_CALENDARS;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_ACTIVE_TASK_LISTS;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_BACKGROUND_COLOR;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_DATE_FORMAT;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_DATE_FORMAT_DEFAULT;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_DAY_HEADER_ALIGNMENT;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_ENTRY_THEME;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_EVENTS_ENDED;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_EVENT_ENTRY_LAYOUT;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_EVENT_RANGE;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_EVENT_RANGE_DEFAULT;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_FILL_ALL_DAY;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_FILL_ALL_DAY_DEFAULT;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_HEADER_THEME;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_HIDE_BASED_ON_KEYWORDS;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_INDICATE_ALERTS;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_INDICATE_RECURRING;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_LOCKED_TIME_ZONE_ID;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_MULTILINE_TITLE;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_MULTILINE_TITLE_DEFAULT;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_PAST_EVENTS_BACKGROUND_COLOR;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_PAST_EVENTS_BACKGROUND_COLOR_DEFAULT;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_SHOW_DAYS_WITHOUT_EVENTS;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_SHOW_DAY_HEADERS;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_SHOW_END_TIME;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_SHOW_END_TIME_DEFAULT;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_SHOW_LOCATION;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_SHOW_LOCATION_DEFAULT;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_SHOW_WIDGET_HEADER;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_TASK_SOURCE;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_TEXT_SIZE_SCALE;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_WIDGET_ID;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_WIDGET_INSTANCE_NAME;

public class ApplicationPreferences {
    private ApplicationPreferences() {
        // prohibit instantiation
    }

    public static void fromInstanceSettings(Context context, Integer widgetId) {
        synchronized (ApplicationPreferences.class) {
            InstanceSettings settings = AllSettings.instanceFromId(context, widgetId);
            setWidgetId(context, widgetId == 0 ? settings.getWidgetId() : widgetId);

            setString(context, PREF_WIDGET_INSTANCE_NAME, settings.getWidgetInstanceName());
            setString(context, PREF_TEXT_SIZE_SCALE, settings.getTextSizeScale().preferenceValue);
            setString(context, PREF_EVENT_ENTRY_LAYOUT, settings.getEventEntryLayout().value);
            setBoolean(context, PREF_MULTILINE_TITLE, settings.isTitleMultiline());
            setString(context, PREF_DATE_FORMAT, settings.getDateFormat());
            setAbbreviateDates(context, settings.getAbbreviateDates());
            setShowDayHeaders(context, settings.getShowDayHeaders());
            setString(context, PREF_DAY_HEADER_ALIGNMENT, settings.getDayHeaderAlignment());
            setShowDaysWithoutEvents(context, settings.getShowDaysWithoutEvents());
            setBoolean(context, PREF_SHOW_WIDGET_HEADER, settings.getShowWidgetHeader());
            setLockedTimeZoneId(context, settings.getLockedTimeZoneId());

            setString(context, PREF_HEADER_THEME, settings.getHeaderTheme());
            setInt(context, PREF_BACKGROUND_COLOR, settings.getBackgroundColor());
            setInt(context, PREF_PAST_EVENTS_BACKGROUND_COLOR, settings.getPastEventsBackgroundColor());
            setString(context, PREF_ENTRY_THEME, settings.getEntryTheme());

            setBoolean(context, PREF_SHOW_END_TIME, settings.getShowEndTime());
            setBoolean(context, PREF_SHOW_LOCATION, settings.getShowLocation());
            setFillAllDayEvents(context, settings.getFillAllDayEvents());
            setBoolean(context, PREF_INDICATE_ALERTS, settings.getIndicateAlerts());
            setBoolean(context, PREF_INDICATE_RECURRING, settings.getIndicateRecurring());

            setEventsEnded(context, settings.getEventsEnded());
            setShowPastEventsWithDefaultColor(context, settings.getShowPastEventsWithDefaultColor());
            setEventRange(context, settings.getEventRange());
            setHideBasedOnKeywords(context, settings.getHideBasedOnKeywords());
            setShowOnlyClosestInstanceOfRecurringEvent(context, settings.getShowOnlyClosestInstanceOfRecurringEvent());

            setActiveCalendars(context, settings.getActiveCalendars());

            setString(context, PREF_TASK_SOURCE, settings.getTaskSource());
            setActiveTaskLists(context, settings.getActiveTaskLists());
        }
    }

    public static void save(Context context, int wigdetId) {
        if (wigdetId != 0 && wigdetId == getWidgetId(context)) {
            AllSettings.saveFromApplicationPreferences(context, wigdetId);
        }
    }

    public static int getWidgetId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(PREF_WIDGET_ID, 0);
    }

    public static void setWidgetId(Context context, int value) {
        setInt(context, PREF_WIDGET_ID, value);
    }

    public static String getWidgetInstanceName(Context context) {
        return getString(context, PREF_WIDGET_INSTANCE_NAME, "");
    }

    public static EventEntryLayout getEventEntryLayout(Context context) {
        return EventEntryLayout.fromValue(PreferenceManager.getDefaultSharedPreferences(context).getString(
                PREF_EVENT_ENTRY_LAYOUT, ""));
    }

    public static boolean isTitleMultiline(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_MULTILINE_TITLE, PREF_MULTILINE_TITLE_DEFAULT);
    }

    public static String getDateFormat(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(
                PREF_DATE_FORMAT, PREF_DATE_FORMAT_DEFAULT);
    }

    public static boolean getAbbreviateDates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_ABBREVIATE_DATES, PREF_ABBREVIATE_DATES_DEFAULT);
    }

    public static void setAbbreviateDates(Context context, boolean value) {
        setBoolean(context, PREF_ABBREVIATE_DATES, value);
    }

    public static boolean getShowDayHeaders(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_SHOW_DAY_HEADERS, true);
    }

    public static void setShowDayHeaders(Context context, boolean value) {
        setBoolean(context, PREF_SHOW_DAY_HEADERS, value);
    }

    public static boolean getShowDaysWithoutEvents(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_SHOW_DAYS_WITHOUT_EVENTS, false);
    }

    public static void setShowDaysWithoutEvents(Context context, boolean value) {
        setBoolean(context, PREF_SHOW_DAYS_WITHOUT_EVENTS, value);
    }

    public static String getLockedTimeZoneId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_LOCKED_TIME_ZONE_ID, "");
    }

    public static boolean isTimeZoneLocked(Context context) {
        return !TextUtils.isEmpty(getLockedTimeZoneId(context));
    }

    public static void setLockedTimeZoneId(Context context, String value) {
        setString(context, PREF_LOCKED_TIME_ZONE_ID, value);
    }

    public static int getPastEventsBackgroundColor(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(
                PREF_PAST_EVENTS_BACKGROUND_COLOR,
                PREF_PAST_EVENTS_BACKGROUND_COLOR_DEFAULT);
    }

    public static boolean getShowEndTime(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_SHOW_END_TIME, PREF_SHOW_END_TIME_DEFAULT);
    }

    public static boolean getShowLocation(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_SHOW_LOCATION, PREF_SHOW_LOCATION_DEFAULT);
    }

    public static boolean getFillAllDayEvents(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_FILL_ALL_DAY, PREF_FILL_ALL_DAY_DEFAULT);
    }

    private static void setFillAllDayEvents(Context context, boolean value) {
        setBoolean(context, PREF_FILL_ALL_DAY, value);
    }

    public static EndedSomeTimeAgo getEventsEnded(Context context) {
        return EndedSomeTimeAgo.fromValue(PreferenceManager.getDefaultSharedPreferences(context).getString(
                PREF_EVENTS_ENDED, ""));
    }

    public static void setEventsEnded(Context context, EndedSomeTimeAgo value) {
        setString(context, PREF_EVENTS_ENDED, value.save());
    }

    public static boolean getShowPastEventsWithDefaultColor(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR, false);
    }

    public static void setShowPastEventsWithDefaultColor(Context context, boolean value) {
        setBoolean(context, PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR, value);
    }

    public static int getEventRange(Context context) {
        return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_EVENT_RANGE, PREF_EVENT_RANGE_DEFAULT));
    }

    public static void setEventRange(Context context, int value) {
        setString(context, PREF_EVENT_RANGE, Integer.toString(value));
    }

    public static String getHideBasedOnKeywords(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_HIDE_BASED_ON_KEYWORDS, "");
    }

    private static void setHideBasedOnKeywords(Context context, String value) {
        setString(context, PREF_HIDE_BASED_ON_KEYWORDS, value);
    }

    public static boolean getShowOnlyClosestInstanceOfRecurringEvent(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT, false);
    }

    public static void setShowOnlyClosestInstanceOfRecurringEvent(Context context, boolean value) {
        setBoolean(context, PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT, value);
    }

    public static Set<String> getActiveCalendars(Context context) {
        Set<String> activeCalendars = PreferenceManager.getDefaultSharedPreferences(context)
                .getStringSet(PREF_ACTIVE_CALENDARS, null);
        if (activeCalendars == null) {
            activeCalendars = Collections.emptySet();
        }
        return activeCalendars;
    }

    public static void setActiveCalendars(Context context, Set<String> calendars) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(PREF_ACTIVE_CALENDARS, calendars);
        editor.apply();
    }

    public static String getTaskSource(Context context) {
        return getString(context, PREF_TASK_SOURCE, PREF_DATE_FORMAT_DEFAULT);
    }

    public static Set<String> getActiveTaskLists(Context context) {
        Set<String> activeTaskLists = PreferenceManager.getDefaultSharedPreferences(context)
                .getStringSet(PREF_ACTIVE_TASK_LISTS, null);
        if (activeTaskLists == null) {
            activeTaskLists = Collections.emptySet();
        }
        return activeTaskLists;
    }

    public static void setActiveTaskLists(Context context, Set<String> taskLists) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(PREF_ACTIVE_TASK_LISTS, taskLists);
        editor.apply();
    }

    public static String getString(Context context, String key, String defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defaultValue);
    }

    private static void setString(Context context, String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defaultValue);
    }

    private static void setBoolean(Context context, String key, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static int getInt(Context context, String key, int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defaultValue);
    }

    public static void setInt(Context context, String key, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }
}
